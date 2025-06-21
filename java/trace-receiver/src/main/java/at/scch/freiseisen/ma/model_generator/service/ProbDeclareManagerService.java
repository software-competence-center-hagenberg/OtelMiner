package at.scch.freiseisen.ma.model_generator.service;

import at.scch.freiseisen.ma.commons.TraceDataType;
import at.scch.freiseisen.ma.data_layer.dto.*;
import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclare;
import at.scch.freiseisen.ma.model_generator.configuration.ModelGenerationConfig;
import at.scch.freiseisen.ma.model_generator.configuration.RestConfig;
import at.scch.freiseisen.ma.model_generator.error.ModelGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@DependsOn("modelGenerationConfig")
public class ProbDeclareManagerService implements DisposableBean {
    private final RestTemplate restTemplate;
    private final RestConfig restConfig;
    private final DeclareService declareService;
    private final TraceCacheManager traceCacheManager;
    private final ObjectMapper objectMapper;
    private final ModelGenerationConfig modelGenerationConfig;
    private final PersistenceService persistenceService;

    private final ExecutorService declareConstraintGenerationExecutor;
    private final ExecutorService probDeclareModelUpdateExecutor;
    @Getter
    private final ConcurrentMap<String, CompletableFuture<ConversionResponse>> generating;
    @Getter
    private final ConcurrentMap<String, ProbDeclareConstraintModelEntry> constraints;
    @Getter
    private final AtomicReference<String> currentId;
    @Getter
    private final AtomicReference<String> currentSourceFile;
    @Getter
    private final AtomicLong currentNrTracesProcessed;
    @Getter
    private final AtomicLong currentExpectedTraces;
    private final AtomicLong currentNrTracesTakenFromCache;
    @Getter
    private final AtomicBoolean isPaused;
    //    @Getter
//    private final AtomicBoolean isLive;
    @Getter
    private final AtomicReference<List<Runnable>> pausedGeneration;

    // constants for data set evaluation
    @Value("${evaluation.nr-segments:0}")
    private int nrSegments;
    @Value("${evaluation.segment-size:-1}")
    private int segmentSize;

    public ProbDeclareManagerService(
            RestTemplate restTemplate,
            RestConfig restConfig,
            DeclareService declareService,
            TraceCacheManager traceCacheManager,
            ObjectMapper objectMapper,
            ModelGenerationConfig modelGenerationConfig,
            PersistenceService persistenceService) {
        this.restTemplate = restTemplate;
        this.restConfig = restConfig;
        this.declareService = declareService;
        this.traceCacheManager = traceCacheManager;
        this.objectMapper = objectMapper;
        this.modelGenerationConfig = modelGenerationConfig;
        this.persistenceService = persistenceService;

        declareConstraintGenerationExecutor = Executors.newFixedThreadPool(modelGenerationConfig.getNrThreads());
        probDeclareModelUpdateExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "ModelUpdate-Worker");
            thread.setUncaughtExceptionHandler((t, e) ->
                    log.error("Uncaught exception in model update thread: {}", e.getMessage(), e));
            return thread;
        });
        generating = new ConcurrentHashMap<>();
        constraints = new ConcurrentHashMap<>();
        currentId = new AtomicReference<>(null);
        currentSourceFile = new AtomicReference<>(null);
        currentNrTracesProcessed = new AtomicLong(0);
        currentExpectedTraces = new AtomicLong(0);
        currentNrTracesTakenFromCache = new AtomicLong(0);
        isPaused = new AtomicBoolean(false);
//        isLive = new AtomicBoolean(false);
        pausedGeneration = new AtomicReference<>(new ArrayList<>());
    }

    @Override
    public void destroy() {
        declareConstraintGenerationExecutor.shutdownNow();
        probDeclareModelUpdateExecutor.shutdownNow();
        finishGeneration(true);
    }

//    @RabbitListener(queues = "${otel_to_probd.routing_key.in.trace}")
//    public void receiveLiveTrace(Trace trace) {
//        log.info("trace received: {}", trace);
//        boolean isGenerationActive = this.currentId.get() != null;
//        log.info("model generation active: {}", isGenerationActive);
//        if (isGenerationActive && !isLive.get()) {
//            throw new ModelGenerationException("received live trace during active non-live generation!");
//        }
//        if (!isGenerationActive) {
//            initLiveGeneration();
//        }
//        CompletableFuture<ConversionResponse> future = new CompletableFuture<>();
//        future.thenAcceptAsync(conversionResponse ->
//                probDeclareModelUpdateExecutor.submit(
//                        () -> updateModel(List.of(conversionResponse.constraints()), currentId.get())
//                ),
//                declareConstraintGenerationExecutor
//        ).exceptionally(e -> {
//            log.error("exception occurred during response processing", e);
//            return null;
//        });
//        generating.put(trace.getId(), future);
//        sendTraceToWorker(trace);
//    }
//
//    private void initLiveGeneration() {
//        SourceDetails sourceDetails = new SourceDetails();
//        sourceDetails.setPage(0);
//        sourceDetails.setSize(100);
//        currentExpectedTraces.compareAndSet(0, -1);
//        isLive.set(true);
//        String id = UUID.randomUUID().toString();
//        currentId.set(id);
//        persistenceService.persistProbDeclare(new ProbDeclare(id));
//    }

    @RabbitListener(queues = "${otel_to_probd.routing_key.in.declare}")
    public void receiveDeclare(Message msg) {
        String model = new String(msg.getBody());
        log.info("received declare result: {}", model);
        try {
            ConversionResponse response = objectMapper.readValue(model, ConversionResponse.class);
            String traceId = response.traceId();
            log.info("received result:\n\ttraceId: {}\n\tconstraints: {}", response.traceId(), response.constraints());
            if (generating.containsKey(traceId)) {
                CompletableFuture<ConversionResponse> future = generating.remove(traceId);
                future.complete(response);
            } else {
                declareService.add(traceId, response.constraints());
                log.info("traceId {}, not present in generation -> ONLY passing it on to declareService", traceId);
            }
        } catch (JsonProcessingException e) {
            throw new ModelGenerationException("Error parsing declare", e);
        }
    }

    protected ProbDeclareModel getProbDeclareModel(String id) {
        return id.equals(currentId.get())
                ? getCurrentModel()
                : persistenceService.getProbDeclareModel(id);
    }

    protected ProbDeclareModel generate(SourceDetails sourceDetails, int expectedTraces) {
        if (currentExpectedTraces.get() == expectedTraces
            && sourceDetails.getSourceFile().equals(currentSourceFile.getAcquire())) {
            return getCurrentModel();
        }
        clearCurrentState();
        currentExpectedTraces.compareAndSet(0, expectedTraces);
        currentSourceFile.compareAndSet(null, sourceDetails.getSourceFile());
        String id = UUID.randomUUID().toString();
        currentId.weakCompareAndSetAcquire(null, id);
        initDeclareGeneration(sourceDetails, id);
        return new ProbDeclareModel(id, new ArrayList<>(), true);
    }

    protected boolean abort() {
        try {
            log.info("aborting prob declare by frontend request");
            traceCacheManager.kill();
            finishGeneration(true);
        } catch (ModelGenerationException e) {
            log.error("error aborting prob declare model", e);
            return false;
        }
        return true;
    }

    private boolean pause() {
        return pause(currentId.get());
    }

    protected boolean pause(String probDeclareId) {
        log.info("pause of generation of model {} requested...", probDeclareId);
        String cId = currentId.getAcquire();
        if (!cId.equals(probDeclareId)) {
            log.info("but currentId ({}) is unequal --> not pausing", cId);
            return false;
        }
        if (!isPaused.get()) {
            log.info("pausing generation of model {}", probDeclareId);
            traceCacheManager.pause();
            isPaused.compareAndSet(false, true);
            log.info("updating model in db");
            persistenceService.persistConstraints(constraints.values(), probDeclareId);
        }
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
        traceCacheManager.resume();
        pausedGeneration.getAcquire().forEach(Runnable::run);
        isPaused.compareAndSet(true, false);
        return true;
    }

    private void initDeclareGeneration(SourceDetails sourceDetails, String id) {
        ProbDeclare probDeclare = persistenceService.persistProbDeclare(new ProbDeclare(id));
        log.info("Starting generation task for model ID: {}", id);
        traceCacheManager.start(sourceDetails, probDeclare.getId());
        try {
            for(int i = 0; i < modelGenerationConfig.getNrThreads(); i++) {
                log.info("starting thread {}", i);
                declareConstraintGenerationExecutor.submit(() -> generateProbDeclareForNextTrace(probDeclare.getId()));
            }
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
        String probDeclareId = currentId.get();
        log.debug("Finishing generation task for model ID: {}", probDeclareId);
        declareService.clear();
        generating.clear();
        if (!abort) {
            persistenceService.persistConstraints(constraints.values(), probDeclareId);
            persistenceService.stopProbDeclareGeneration(probDeclareId);
        }
        if (abort) {
            clearCurrentState();
        }
        log.info("Finished generation task for model ID: {}", probDeclareId);
    }

    private void clearCurrentState() {
        currentId.set(null);
        currentSourceFile.set(null);
        currentNrTracesProcessed.set(0L);
        currentExpectedTraces.set(0L);
        currentNrTracesTakenFromCache.set(0L);
        constraints.clear();
        traceCacheManager.kill();
    }

    private void generateProbDeclareForNextTrace(String probDeclareId) {
        Trace trace;
        long nrTracesTaken;
        try {
            trace = traceCacheManager.take();
            nrTracesTaken = currentNrTracesTakenFromCache.incrementAndGet();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ModelGenerationException("exception occurred while accessing trace cache", e);
        }
        CompletableFuture<ConversionResponse> future = new CompletableFuture<>();
        future.thenAcceptAsync(
                conversionResponse -> processResponse(conversionResponse, probDeclareId),
                declareConstraintGenerationExecutor
                )
                .exceptionally(e -> {
                    log.error("exception occurred during response processing", e);
                    return null;
                });
        generating.put(trace.getId(), future);
        sendTraceToWorker(trace);

        // ONLY for evaluation
        if (nrSegments > 0
            && segmentSize != -1
            && nrTracesTaken >= segmentSize
            && nrTracesTaken % segmentSize == 0) {
            log.error("ATTENTION -> PAUSING because nrTracesTaken % segmentSize == 0");
            pause();
        }

    }

    private void sendTraceToWorker(Trace trace) {
        TraceDataType traceDataType = TraceDataType.valueOf(trace.getTraceDataType());
        if (isPaused.getAcquire()) {
            log.info("model generation is currently paused --> setting out next trace({}) until resume is requested"
                    , trace.getId());
            pausedGeneration.getAcquire()
                    .add(() -> {
                        log.debug("resuming with trace {}", trace.getId());
                        declareService.transformAndPipe(
                                trace.getId(), trace.getSpansAsJson(), traceDataType);
                    });
        } else {
            declareService.transformAndPipe(trace.getId(), trace.getSpansAsJson(), traceDataType);
        }
    }

    private void processResponse(ConversionResponse conversionResponse, String probDeclareId) {
        if (conversionResponse == null) {
            log.debug("conversion response is null, aborting generation...");
            finishGeneration(true);
            return;
        }
        if (currentId.getAcquire() == null) {
            log.info("Currently no generation running --> aborting");
            return;
        }
        probDeclareModelUpdateExecutor.submit(
                () -> updateModel(List.of(conversionResponse.constraints()), probDeclareId));
        if (!traceCacheManager.isCacheDone()) {
            declareConstraintGenerationExecutor.submit(() -> generateProbDeclareForNextTrace(probDeclareId));
        }
    }

    private boolean isGenerationFinished() {
        return currentExpectedTraces.get() == currentNrTracesProcessed.get()
               && traceCacheManager.isCacheDone();
    }

    private ProbDeclareModel getCurrentModel() {
        List<ProbDeclareConstraint> model = constraints.values()
                .stream()
                .map(declare -> new ProbDeclareConstraint(declare.getProbability(), declare.getConstraintTemplate()))
                .toList();
        boolean isGenerating = currentNrTracesProcessed.get() != currentExpectedTraces.get();
        return new ProbDeclareModel(currentId.get(), model, isGenerating);
    }

    private void updateModel(List<String> newConstraints, String probDeclareId) {
        long currentNrTraces = currentNrTracesProcessed.getAcquire();
        log.info("updating model");
        log.debug("constraints to update: {}", newConstraints);
        if (newConstraints.isEmpty()) {
            log.error("received empty list of declare constraints");
            throw new ModelGenerationException("Error! received empty list of declare constraints");
        }
        if (constraints.isEmpty() && currentNrTraces > 0) {
            log.error("nr traces > 0 but no constraints exist!");
            throw new ModelGenerationException("Error! nr traces > 0 but no constraints exist!");
        }
        if (constraints.isEmpty()) {
            assert currentNrTraces == 0;
            initModelWithCrisps(newConstraints);
            assert newConstraints.size() == constraints.size();
        } else {
            assert currentNrTraces > 0;
            updateConstraints(newConstraints, currentNrTraces);
        }
        if (isGenerationFinished()) {
            finishGeneration(false);
            log.info("GENERATION COMPLETE! No more traces found for probDeclareId: {}", probDeclareId);
        }
    }

    private void initModelWithCrisps(List<String> newConstraints) {
        currentNrTracesProcessed.compareAndSet(0, 1);
        log.info("no constraints so far and no traces --> initializing crisps");
        newConstraints.forEach(c -> {
            log.debug("adding crisp constraint: {}", c);
            constraints.put(c, new ProbDeclareConstraintModelEntry(c, 1d, 1L));
        });
    }

    private void updateConstraints(List<String> newConstraints, long currentNrTraces) {
        log.info("existing {} constraints so far --> updating model", constraints.size());
        List<String> visited = new ArrayList<>();
        log.info("updating nr traces ({} -> {})", currentNrTraces, currentNrTraces + 1);
        currentNrTracesProcessed.compareAndSet(currentNrTraces, currentNrTraces + 1);
        log.info("updating constraints found in trace...");
        newConstraints.forEach(nc -> {
            updateOrAddConstraint(nc);
            visited.add(nc);
        });
        assert visited.size() == newConstraints.size();
        log.info("updated {} constraints", visited.size());
        log.info("updating constraints NOT found in trace...");
        updateConstraintsNotContainedInCurrentTrace(visited);
        log.info("updated {} constraints", constraints.size() - visited.size());
    }

    private void updateOrAddConstraint(String newConstraint) {
        log.debug("updating {}", newConstraint);
        ProbDeclareConstraintModelEntry declare;
        if (constraints.containsKey(newConstraint)) {
            log.debug("constraint exists --> updating");
            declare = constraints.get(newConstraint);
            declare.increment();
            if (declare.getProbability() != 1d) {
                log.debug("constraint not crisp --> updating probability");
                updateProbability(declare);
            }
        } else {
            log.debug("constraint does not exist --> creating new one");
            declare = new ProbDeclareConstraintModelEntry(newConstraint, 1d, 1L);
            log.debug("updating probability");
            updateProbability(declare);
            log.debug("adding to constraint: {}", newConstraint);
            constraints.put(declare.getConstraintTemplate(), declare);
        }
        log.debug("updating done");
    }

    private void updateConstraintsNotContainedInCurrentTrace(List<String> visited) {
        constraints.keySet()
                .stream()
                .filter(c -> !visited.contains(c))
                .forEach(c -> {
                    log.debug("updating {}", c);
                    ProbDeclareConstraintModelEntry declare = constraints.get(c);
                    updateProbability(declare);
                });
    }

    private void updateProbability(ProbDeclareConstraintModelEntry declare) {
        log.debug("updating probability of {} (old prob: {})", declare.getConstraintTemplate(), declare.getProbability());
        declare.setProbability((double) declare.getNr() / currentNrTracesProcessed.getAcquire());
        log.debug("new probability: {}", declare.getProbability());
    }
}
