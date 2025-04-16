package at.scch.freiseisen.ma.trace_collector.service;

import at.scch.freiseisen.ma.commons.RestPageImpl;
import at.scch.freiseisen.ma.commons.TraceDataType;
import at.scch.freiseisen.ma.data_layer.dto.*;
import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclare;
import at.scch.freiseisen.ma.trace_collector.configuration.OtelToProbdeclareConfiguration;
import at.scch.freiseisen.ma.trace_collector.configuration.RestConfig;
import at.scch.freiseisen.ma.trace_collector.error.ModelGenerationException;
import at.scch.freiseisen.ma.trace_collector.model.ProbDeclareGenerationDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
// TODO split into rabbit-, db-, probDManagerService, and adapt collector service
public class ProbDeclareManagerService {
    private final RestTemplate restTemplate;
    private final RestConfig restConfig;
    private final RabbitTemplate rabbitTemplate;
    private final OtelToProbdeclareConfiguration otelToProbdeclareConfiguration;
    private final ExecutorService declareConstraintGenerationExecutor = Executors.newSingleThreadExecutor(); // TODO evaluate if and how many threads
    private final ExecutorService modelUpdateExecutor = Executors.newSingleThreadExecutor();
    private final ConcurrentMap<String, CompletableFuture<ConversionResponse>> generating = new ConcurrentHashMap<>();
    private final CanonizedSpanTreeService canonizedSpanTreeService;
    private final ObjectMapper objectMapper;
    //FIXME refactor data types to (multiple) state objects
    private final ConcurrentMap<String, ProbDeclareConstraintModelEntry> constraints = new ConcurrentHashMap<>();
    private final DeclareService declareService;
    private final AtomicReference<String> currentId = new AtomicReference<>(null);
    private final AtomicReference<String> currentSourceFile = new AtomicReference<>(null);
    private final AtomicLong currentNrTracesProcessed = new AtomicLong(0);
    private final AtomicLong currentExpectedTraces = new AtomicLong(0);
    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    private final AtomicReference<List<Runnable>> pausedGeneration = new AtomicReference<>(new ArrayList<>());

    // FIXME currently handling only single trace --> adapt to handle multiple traces with extra rabbit listener
    @RabbitListener(queues = "${otel_to_probd.routing_key.in.declare}")
    public void receiveDeclare(Message msg) {
        String model = new String(msg.getBody());
        log.info("received declare result: {}", model);
        try {
            ConversionResponse response = objectMapper.readValue(model, ConversionResponse.class);
            String traceId = response.traceId();
            declareService.add(traceId, response.constraints());
            log.info("received result:\n\ttraceId: {}\n\tconstraints: {}", response.traceId(), response.constraints());
            if (generating.containsKey(traceId)) {
                CompletableFuture<ConversionResponse> future = generating.remove(traceId);
                future.complete(response);
            } else {
                log.info("traceId {}, not present in generation -> ONLY passing it on to declareService", traceId);
            }
        } catch (JsonProcessingException e) {
            throw new ModelGenerationException("Error parsing declare", e);
        }
    }

    // TODO check if necessary or if better done directly in data service
    protected ProbDeclareModel getProbDeclareModel(String id) {
        return id.equals(currentId.get())
                ? getCurrentModel()
                : restTemplate.getForObject(restConfig.probDeclareUrl + "/model/" + id, ProbDeclareModel.class);
    }

    protected ProbDeclareModel generate(SourceDetails sourceDetails, int expectedTraces) {
        if (currentExpectedTraces.get() == expectedTraces && sourceDetails.getSourceFile().equals(currentSourceFile.getAcquire())) {
            return getCurrentModel();
        }
        clearCurrentState();
        currentExpectedTraces.compareAndSet(0, expectedTraces);
        currentSourceFile.compareAndSet(null, sourceDetails.getSourceFile());
        String id = UUID.randomUUID().toString();
        currentId.weakCompareAndSetAcquire(null, id);
        declareConstraintGenerationExecutor.submit(() -> initDeclareGeneration(sourceDetails, id));
        return new ProbDeclareModel(id, new ArrayList<>(), true);
    }

    protected boolean abort() {
        try {
            log.info("aborting prob declare by frontend request");
            finishGeneration(true);
        } catch (ModelGenerationException e) {
            log.error("error aborting prob declare model", e);
            return false;
        }
        return true;
    }

    protected boolean pause(String probDeclareId) {
        log.info("pause of generation of model {} requested...", probDeclareId);
        String cId = currentId.getAcquire();
        if (!cId.equals(probDeclareId)) {
            log.info("but currentId ({}) is unequal --> not pausing", cId);
            return false;
        }
        log.info("pausing generation of model {}", probDeclareId);
        isPaused.compareAndSet(false, true);
        return isPaused.getAcquire();
    }

    protected boolean resume(String probDeclareId) {
        log.info("resume of generation of model {} requested...", probDeclareId);
        String cId = currentId.getAcquire();
        if (!cId.equals(probDeclareId)) {
            log.info("but currentId ({}) is unequal --> not resuming", cId);
            return false;
        }
        if (!isPaused.getAcquire()) {
            log.info("but current model generation is NOT paused --> not resuming");
            return false;
        }
        log.info("resuming generation of model {}", probDeclareId);
        pausedGeneration.getAcquire().forEach(Runnable::run);
        isPaused.compareAndSet(true, false);
        return true;
    }

    private ProbDeclare persist(ProbDeclare probDeclare) {
        return restTemplate.postForObject(restConfig.probDeclareUrl + "/one", probDeclare, ProbDeclare.class);
    }

    private void initDeclareGeneration(SourceDetails sourceDetails, String id) {
        ProbDeclare probDeclare = persist(new ProbDeclare(id));
        log.info("Starting generation task for model ID: {}", id);
        try {
            generateDeclareConstraints(probDeclare.getId(), sourceDetails);
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
            finishGeneration(true);
            throw new ModelGenerationException(String.format("error in generation model (%s)", id), e);
        } finally {
            log.info("Finished initializing generation task for model ID: {}", id);
        }
    }

    private void finishGeneration(boolean abort) {
        log.debug("Finishing generation task for model ID: {}", currentId.get());
        declareService.clear();
        generating.clear();
        restTemplate.delete(restConfig.probDeclareUrl + "/stop-generation/" + currentId); // FIXME change to POST
        if (abort) {
            clearCurrentState();
        }
        log.info("Finished generation task for model ID: {}", currentId.get());
    }

    private void clearCurrentState() {
        currentId.set(null);
        currentSourceFile.set(null);
        currentNrTracesProcessed.set(0L);
        currentExpectedTraces.set(0L);
        constraints.clear();
    }

    private ProbDeclareModel getCurrentModel() {
        List<ProbDeclareConstraint> model = constraints.values()
                .stream()
                .map(declare -> new ProbDeclareConstraint(declare.getProbability(), declare.getConstraintTemplate()))
                .toList();

        return new ProbDeclareModel(currentId.get(), model, currentNrTracesProcessed.get() != currentExpectedTraces.get());
    }

    private void generateDeclareConstraints(@NonNull String probDeclareId, @NonNull SourceDetails sourceDetails) {
        ResponseEntity<RestPageImpl<Trace>> response = restTemplate.exchange(
                restConfig.tracesUrl
                + "?sourceFile=" + sourceDetails.getSourceFile()
                + "&page=" + sourceDetails.getPage()
                + "&size=" + sourceDetails.getSize()
                + "&sort=" + sourceDetails.getSort(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Page<Trace> page = Objects.requireNonNull(response.getBody());
            ProbDeclareGenerationDTO dto = new ProbDeclareGenerationDTO(page);
            createAndPersistProbDeclareToTrace(probDeclareId, page.getContent());
            generateProbDeclareForNextTrace(dto, probDeclareId, sourceDetails);
        } else {
            finishGeneration(true);
            throw new ModelGenerationException("persistance of probdeclare to trace entitities failed, aborting");
        }
    }

    private void generateProbDeclareForNextTrace(ProbDeclareGenerationDTO dto, String probDeclareId, SourceDetails sourceDetails) {
        Trace trace = dto.getNext();
        CompletableFuture<ConversionResponse> future = new CompletableFuture<>();
        future.thenAcceptAsync(conversionResponse -> processResponse(conversionResponse, probDeclareId, dto, sourceDetails))
                .exceptionally(e -> {
                    log.error("exception occurred during response processing", e);
                    return null;
                });
        generating.put(trace.getId(), future);

        if (isPaused.getAcquire()) {
            log.info("model generation is currently paused --> setting out next trace({}) until resume is requested"
                    , trace.getId());
            pausedGeneration.getAcquire()
                    .add(() -> {
                        log.debug("resuming with trace {}", trace.getId());
                        declareService.transformAndPipe(
                                trace.getId(), trace.getSpansAsJson(), TraceDataType.JAEGER_SPANS_LIST);
                    });
        } else {
            declareService.transformAndPipe(trace.getId(), trace.getSpansAsJson(), TraceDataType.JAEGER_SPANS_LIST);
        }
    }

    private void processResponse(ConversionResponse conversionResponse, String probDeclareId, ProbDeclareGenerationDTO dto, SourceDetails sourceDetails) {
        ResponseEntity<List<ProbDeclareConstraintModelEntry>> response = restTemplate.exchange(
                restConfig.declareUrl + "/by-constraint-template/" + probDeclareId,
                HttpMethod.POST,
                new HttpEntity<>(conversionResponse.constraints()),
                new ParameterizedTypeReference<>() {
                });
        if (conversionResponse == null) {
            log.debug("conversion response is null, aborting generation...");
            finishGeneration(true);
            return;
        }
        if (currentId.getAcquire() == null) {
            log.info("Currently no generation running --> aborting");
            return;
        }
        List<ProbDeclareConstraintModelEntry> existingDeclare = Objects.requireNonNull(response.getBody());
        List<String> existingConstraints = existingDeclare.stream().map(ProbDeclareConstraintModelEntry::getConstraintTemplate).toList();
        /*
        List<String> existingConstraints = existingDeclare.stream().map(Declare::getConstraintTemplate).toList();
        String[] newConstraintTemplates = Arrays.stream(conversionResponse.constraints())
                .filter(c -> !existingConstraints.contains(c))
                .toArray(String[]::new);
        HttpEntity<ConversionResponse> requestEntity = new HttpEntity<>(new ConversionResponse(conversionResponse.traceId(), newConstraintTemplates));
        response = postExchange(restConfig.declareUrl + "/add-constraints" + probDeclareId, requestEntity);

        existingDeclare.addAll(Objects.requireNonNull(response.getBody()));
         */
        List<List<String>> newConstraintTemplates = IntStream.range(0, (int) Math.ceil((double) conversionResponse.constraints().length / 10))
                .mapToObj(i -> Arrays.stream(conversionResponse.constraints())
                        .filter(c -> !existingConstraints.contains(c))
                        .skip(i * 10L)
                        .limit(10)
                        .toList())
                .toList();
        newConstraintTemplates.forEach(templates -> {
            ResponseEntity<List<ProbDeclareConstraintModelEntry>> createdDeclare = restTemplate.exchange(
                    restConfig.declareUrl + "/add-constraints/" + probDeclareId,
                    HttpMethod.POST,
                    new HttpEntity<>(new ConversionResponse(conversionResponse.traceId(),
                            templates.toArray(String[]::new))),
                    new ParameterizedTypeReference<>() {
                    }
            );

            existingDeclare.addAll(Objects.requireNonNull(createdDeclare.getBody()));
        });

        updateModel(existingDeclare, probDeclareId);
        if (dto.hasContent()) {
            generateProbDeclareForNextTrace(dto, probDeclareId, sourceDetails);
        } else if (dto.hasMorePages()) {
            sourceDetails.setPage(dto.getCurrentPage() + 1);
            generateDeclareConstraints(probDeclareId, sourceDetails);
        } else if (isGenerationFinished()) {
            log.info("GENERATION COMPLETE! No more traces found for probDeclareId: {}", probDeclareId);
        }
    }

    private void createAndPersistProbDeclareToTrace(String probDeclareId, List<Trace> traces) {
        log.debug("creating association between prob declare model {} and trace ids: {}", probDeclareId, traces.stream().map(Trace::getId).collect(Collectors.joining(", ")));
        restTemplate.postForLocation(restConfig.probDeclareToTraceUrl + "/" + probDeclareId, traces);
    }

    private void updateModel(List<ProbDeclareConstraintModelEntry> declareList, String probDeclareId) {
        long currentNrTraces = currentNrTracesProcessed.getAcquire();
        log.info("updating model");
        log.debug("declareList: {}", declareList);
        if (declareList.isEmpty()) {
            log.error("received empty list of declare constraints");
            throw new ModelGenerationException("Error! received empty list of declare constraints");
        }
        if (constraints.isEmpty() && currentNrTraces > 0) {
            log.error("nr traces > 0 but no constraints exist!");
            throw new ModelGenerationException("Error! nr traces > 0 but no constraints exist!");
        }
        if (constraints.isEmpty()) {
            assert currentNrTraces == 0;
            initModelWithCrisps(declareList);
        } else {
            log.info("existing {} constraints so far --> updating model", constraints.size());
            List<String> visited = new ArrayList<>();
            assert currentNrTraces > 0;
            log.info("updating nr traces ({} -> {})", currentNrTraces, currentNrTraces + 1);
            currentNrTracesProcessed.compareAndSet(currentNrTraces, currentNrTraces + 1);
            declareList.forEach(declare -> visited.add(updateOrAddConstraint(declare)));
            declareList.addAll(updateConstraintsNotContainedInCurrentTrace(visited));
            restTemplate.postForLocation(restConfig.declareUrl + "/" + probDeclareId, declareList);
        }
    }

    private void initModelWithCrisps(List<ProbDeclareConstraintModelEntry> declareList) {
        currentNrTracesProcessed.compareAndSet(0, 1);
        log.info("no constraints so far and no traces --> initializing crisps");
        declareList.forEach(declare -> {
            log.debug("adding crisp constraint: {}", declare.getConstraintTemplate());
            constraints.put(declare.getConstraintTemplate(), declare);
        });
    }

    private String updateOrAddConstraint(ProbDeclareConstraintModelEntry declare) {
        log.debug("updating {}", declare);
        if (constraints.containsKey(declare.getConstraintTemplate())) {
            log.debug("constraint exists --> updating");
            ProbDeclareConstraintModelEntry d = constraints.get(declare.getConstraintTemplate());
            log.debug("removing {}", d);
            assert d.getNr() >= declare.getNr();
            assert d.getProbability() <= declare.getProbability();
            declare.setNr(d.getNr() + 1);
            if (d.getProbability() != 1d) {
                log.debug("constraint not crisp --> updating probability");
                declare.setProbability(((double) declare.getNr()) / currentNrTracesProcessed.getAcquire());
            }
        } else {
            log.debug("constraint does not exist --> updating probability");
            declare.setProbability(((double) declare.getNr()) / currentNrTracesProcessed.getAcquire());
            log.debug("adding to constraints: {}", declare);
            constraints.put(declare.getConstraintTemplate(), declare);
        }
        return declare.getConstraintTemplate();
    }

    private List<ProbDeclareConstraintModelEntry> updateConstraintsNotContainedInCurrentTrace(List<String> visited) {
        List<ProbDeclareConstraintModelEntry> declareList = new ArrayList<>();
        constraints.keySet()
                .stream()
                .filter(c -> !visited.contains(c))
                .forEach(c -> {
                    ProbDeclareConstraintModelEntry declare = constraints.get(c);
                    declare.setNr(declare.getNr() + 1);
                    declare.setProbability(((double) declare.getNr()) / currentNrTracesProcessed.getAcquire());
                    declareList.add(declare);
                });
        return declareList;
    }

    private boolean isGenerationFinished() {
        return currentExpectedTraces.getAcquire() == currentNrTracesProcessed.getAcquire();
    }
}
