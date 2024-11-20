package at.scch.freiseisen.ma.trace_collector.service;

import at.scch.freiseisen.ma.commons.TraceDataType;
import at.scch.freiseisen.ma.data_layer.dto.ConversionResponse;
import at.scch.freiseisen.ma.data_layer.dto.SourceDetails;
import at.scch.freiseisen.ma.data_layer.dto.SpansListConversionRequest;
import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.Declare;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclare;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclareToTrace;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclareToTraceId;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
// TODO split into rabbit-, db-, probDManagerService, and adapt collector service
public class ProbDeclareManagerService {
    private final RestTemplate restTemplate;
    private final RestConfig restConfig;
    private final RabbitTemplate rabbitTemplate;
    private final OtelToProbdeclareConfiguration otelToProbdeclareConfiguration;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(); // TODO evaluate if and how many threads
    private final Map<UUID, CompletableFuture<ConversionResponse>> generating = new HashMap<>();
    private final CollectorService collectorService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private void persist(ProbDeclare probDeclare) {
        restTemplate.postForLocation(restConfig.probDeclareUrl, probDeclare);
    }

    public String generate(SourceDetails sourceDetails) {
        String id = UUID.randomUUID().toString();
        ProbDeclare probDeclare = new ProbDeclare(id);
        persist(probDeclare);
        executorService.submit(() -> generateDeclareConstraints(probDeclare, sourceDetails));

        return id;
    }

    private void generateDeclareConstraints(
            @NonNull ProbDeclare probDeclare,
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
            executorService.submit(() -> scheduleDeclareGeneration(probDeclare, page.getContent()));

            if (page.getNumber() < page.getTotalPages()) {
                sourceDetails.setPage(page.getNumber() + 1);
                generateDeclareConstraints(probDeclare, sourceDetails);
            }
            return;
        }

        throw new ModelGenerationException(
                String.format("error fetching traces for model (%s) generation", probDeclare.getId())
        );
    }

    private void scheduleDeclareGeneration(
            @NonNull ProbDeclare probDeclare,
            @NonNull List<Trace> traces
    ) {
        createAndPersistProbDeclareToTrace(probDeclare, traces);
        traces.forEach(
                trace -> {
                    CompletableFuture<ConversionResponse> future = planResponseProcessing(probDeclare);
                    generating.put(UUID.fromString(trace.getId()), future);
                    executorService.submit(
                            () -> transformAndPipe(
                                    trace.getId(),
                                    trace.getSpansAsJson(),
                                    TraceDataType.JAEGER_SPANS_LIST
                            )
                    );
                }
        );
    }

    private void createAndPersistProbDeclareToTrace(ProbDeclare probDeclare, List<Trace> traces) {
        List<ProbDeclareToTrace> probDeclareToTraces = traces.stream()
                .map(trace -> new ProbDeclareToTrace(probDeclare, trace))
                .toList();
        restTemplate.postForLocation(restConfig.probDeclareToTraceUrl, probDeclareToTraces);
    }

    private CompletableFuture<ConversionResponse> planResponseProcessing(ProbDeclare probDeclare) {
        CompletableFuture<ConversionResponse> future = new CompletableFuture<>();
        future.thenApplyAsync(response -> processResponse(response, probDeclare.getId()))
                .thenApplyAsync(this::updateModel);
        return future;
    }

    private void transformAndPipe(String traceId, List<String> spans, TraceDataType traceDataType) {
        String routingKey = otelToProbdeclareConfiguration.determineRoutingKey(traceDataType);
        SpansListConversionRequest request = new SpansListConversionRequest(traceId, spans);
        try {
            rabbitTemplate.convertAndSend(routingKey, objectMapper.writeValueAsString(request));//"[" + String.join(",", trace) + "]");
        } catch (JsonProcessingException e) {
            throw new ModelGenerationException("Error serializing request");
        }
    }


    @RabbitListener(queues = "${otel_to_probd.routing_key.in}")
    public void receiveProbDeclare(Message msg) {
        ConversionResponse response = objectMapper.convertValue(msg.getBody(), ConversionResponse.class);
        log.info("received result:\ntraceId: {}\nconstraints: {}", response.traceId(), response.constraints());
        UUID traceId = UUID.fromString(response.traceId());
        if (generating.containsKey(traceId)) {
            generating.get(traceId).complete(response);
        } else {
            throw new ModelGenerationException("traceId " + traceId + " not found");
        }
    }

    private List<Declare> processResponse(ConversionResponse response, String probDeclareId) {
        List<Declare> constraints = new ArrayList<>();
        ProbDeclareToTraceId probDeclareToTraceId = new ProbDeclareToTraceId(probDeclareId, response.traceId());
        ProbDeclareToTrace probDeclareToTrace = restTemplate.getForEntity(
                restConfig.probDeclareToTraceUrl,
                ProbDeclareToTrace.class,
                probDeclareToTraceId
        ).getBody();

        assert probDeclareToTrace != null;
        for (String constraint : response.constraints()) {
            UUID id = UUID.randomUUID();
            Declare declare = Declare.builder()
                    .id(id.toString())
                    .probability(1f)
                    .constraintTemplate(constraint)
                    .probDeclare(probDeclareToTrace.getProbDeclare())
                    .trace(probDeclareToTrace.getTrace())
                    .updateDate(LocalDateTime.now())
                    .insertDate(LocalDateTime.now())
                    .build();
            constraints.add(declare);
        }

        return constraints;
    }

    private ProbDeclare updateModel(List<Declare> declareList) {
        // TODO implement
        // TODO fetch crisp and probability constraints compare probability and update weight
        restTemplate.postForLocation(restConfig.declareUrl, declareList);
        // TODO fetch updated prob declare
        return declareList.getFirst().getProbDeclare(); // FIXME
    }
}
