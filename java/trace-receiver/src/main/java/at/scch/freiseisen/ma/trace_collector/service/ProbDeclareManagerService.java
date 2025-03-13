package at.scch.freiseisen.ma.trace_collector.service;

import at.scch.freiseisen.ma.commons.TraceDataType;
import at.scch.freiseisen.ma.data_layer.dto.ConversionResponse;
import at.scch.freiseisen.ma.data_layer.dto.ProbDeclareModel;
import at.scch.freiseisen.ma.data_layer.dto.SourceDetails;
import at.scch.freiseisen.ma.data_layer.entity.BaseEntity;
import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.Declare;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclare;
import at.scch.freiseisen.ma.trace_collector.configuration.OtelToProbdeclareConfiguration;
import at.scch.freiseisen.ma.trace_collector.configuration.RestConfig;
import at.scch.freiseisen.ma.trace_collector.error.ModelGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    private final Map<String, CompletableFuture<ConversionResponse>> generating = new ConcurrentHashMap<>();
    private final CanonizedSpanTreeService canonizedSpanTreeService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Float> probabilityConstraints = new ConcurrentHashMap<>();
    private final List<String> crispConstraints = new CopyOnWriteArrayList<>();
    private final DeclareService declareService;

    private void persist(ProbDeclare probDeclare) {
        restTemplate.postForLocation(restConfig.probDeclareUrl + "/one", probDeclare);
    }

    public String generate(SourceDetails sourceDetails) {
        String id = UUID.randomUUID().toString();
        ProbDeclare probDeclare = new ProbDeclare(id);
        persist(probDeclare);
        declareConstraintGenerationExecutor.submit(() -> {
            log.info("Starting generation task for model ID: {}", id);
            try {
                generateDeclareConstraints(probDeclare.getId(), sourceDetails);
            } catch (Exception e) {
                throw new ModelGenerationException(
                        String.format("error fetching traces for model (%s) generation", id)
                );
            } finally {
                log.info("Finished generation task for model ID: {}", id);
            }
        });

        return id;
    }

    private void generateDeclareConstraints(
            @NonNull String probDeclareId,
            @NonNull SourceDetails sourceDetails
    ) {
        HttpEntity<SourceDetails> requestEntity = new HttpEntity<>(sourceDetails);
        ResponseEntity<Page<Trace>> response = restTemplate.exchange(
                restConfig.tracesSourceUrl,
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<>() {
                }
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Page<Trace> page = Objects.requireNonNull(response.getBody());
            declareConstraintGenerationExecutor.submit(() -> scheduleDeclareGeneration(probDeclareId, page.getContent()));

            if (page.getNumber() < page.getTotalPages()) {
                sourceDetails.setPage(page.getNumber() + 1);
                generateDeclareConstraints(probDeclareId, sourceDetails);
            }
        }
    }

    private void scheduleDeclareGeneration(
            @NonNull String probDeclareId,
            @NonNull List<Trace> traces
    ) {
        createAndPersistProbDeclareToTrace(probDeclareId, traces);
        // FIXME move transform and pipe to listener location to assure that rabbitMQ queue does not overflow!
        traces.forEach(
                trace -> {
                    CompletableFuture<ConversionResponse> future = planResponseProcessing(probDeclareId);
                    generating.put(trace.getId(), future);
                    declareConstraintGenerationExecutor.submit(
                            () -> declareService.transformAndPipe(
                                    trace.getId(),
                                    trace.getSpansAsJson(),
                                    TraceDataType.JAEGER_SPANS_LIST
                            )
                    );
                }
        );
    }

    private void createAndPersistProbDeclareToTrace(String probDeclareId, List<Trace> traces) {
        restTemplate.postForLocation(restConfig.probDeclareToTraceUrl + "/" + probDeclareId, traces);
    }

    private CompletableFuture<ConversionResponse> planResponseProcessing(String probDeclareId) {
        CompletableFuture<ConversionResponse> future = new CompletableFuture<>();
        future.thenAcceptAsync(response -> processResponse(response, probDeclareId));
        return future;
    }

// FIXME currently handling only single trace --> adapt to handle multiple traces with extra rabbit listener
    @RabbitListener(queues = "${otel_to_probd.routing_key.in.declare}")
    public void receiveDeclare(Message msg) {
        String model = new String(msg.getBody());
        log.info("received probdeclare result: {}", model);
        try {
            ConversionResponse response = objectMapper.readValue(model, ConversionResponse.class);
            String traceId = response.traceId();
            declareService.add(traceId, response.constraints());
            log.info("received result:\ntraceId: {}\nconstraints: {}", response.traceId(), response.constraints()[0]);
            if (generating.containsKey(traceId)) {
                generating.get(traceId).complete(response);
                generating.remove(traceId);
            } else {
                log.info("traceId {}, not present in generation -> passing it on to declareService", traceId);
            }
        } catch (JsonProcessingException e) {
            throw new ModelGenerationException("Error parsing declare", e);
        }
    }

    private void processResponse(ConversionResponse conversionResponse, String probDeclareId) {
        HttpEntity<ConversionResponse> requestEntity = new HttpEntity<>(conversionResponse);
        ResponseEntity<List<Declare>> response = postExchange(
                restConfig.declareUrl + "/by-constraint-template" + probDeclareId,
                requestEntity
        );
        List<Declare> existingDeclare = Objects.requireNonNull(response.getBody());
        List<String> existingConstraints = existingDeclare.stream().map(Declare::getConstraintTemplate).toList();
        String[] newConstraintTemplates = Arrays.stream(conversionResponse.constraints())
                .filter(c -> !existingConstraints.contains(c))
                .toArray(String[]::new);
        requestEntity = new HttpEntity<>(new ConversionResponse(conversionResponse.traceId(), newConstraintTemplates));
        response = postExchange(
                restConfig.declareUrl + "/add-constraints" + probDeclareId,
                requestEntity
        );

        existingDeclare.addAll(Objects.requireNonNull(response.getBody()));
        modelUpdateExecutor.submit(() -> updateModel(existingDeclare));
    }

    private void updateModel(List<Declare> declareList) {
        //List<String>
        // TODO implement
        // TODO fetch crisp and probability constraints compare probability and update weight
        restTemplate.postForLocation(restConfig.declareUrl, declareList);
        // TODO fetch updated prob declare
    }

    private <S extends BaseEntity<String>> ResponseEntity<List<S>> postExchange(String url, HttpEntity<?> requestEntity) {
        return restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<>() {
                });
    }

    // TODO check if necessary or if better done directly in data service
    public ProbDeclareModel getProbDeclareModel(String id) {
        return restTemplate.getForObject(restConfig.probDeclareUrl + "/model/" + id, ProbDeclareModel.class);
    }

    private boolean checkIfGenerationFinished(String probDeclareId) {
        if (generating.isEmpty()) {
            restTemplate.delete(restConfig.probDeclareUrl + "/stop-generation/" + probDeclareId);
            return true;
        }

        return false;
    }
}
