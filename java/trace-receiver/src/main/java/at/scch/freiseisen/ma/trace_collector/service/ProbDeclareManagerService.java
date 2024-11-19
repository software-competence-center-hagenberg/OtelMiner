package at.scch.freiseisen.ma.trace_collector.service;

import at.scch.freiseisen.ma.commons.TraceDataType;
import at.scch.freiseisen.ma.data_layer.dto.SourceDetails;
import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.Declare;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclare;
import at.scch.freiseisen.ma.trace_collector.configuration.OtelToProbdeclareConfiguration;
import at.scch.freiseisen.ma.trace_collector.configuration.RestConfig;
import at.scch.freiseisen.ma.trace_collector.error.ModelGenerationException;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProbDeclareManagerService {
    private final RestTemplate restTemplate;
    private final RestConfig restConfig;
    private final RabbitTemplate rabbitTemplate;
    private final OtelToProbdeclareConfiguration otelToProbdeclareConfiguration;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final Map<UUID, Future<List<Declare>>> generating = new HashMap<>();
    private final CollectorService collectorService;

    private void persist(ProbDeclare probDeclare) {
        restTemplate.postForLocation(restConfig.probDeclareUrl, probDeclare);
    }

    public String generate(SourceDetails sourceDetails) {
        UUID id = UUID.randomUUID();
        String idString = id.toString();
        ProbDeclare probDeclare = ProbDeclare.builder()
                .id(idString)
                .build();

        persist(probDeclare);
        executorService.submit(() -> generateDeclareConstraints(probDeclare, sourceDetails));

        return idString;
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
        // FIXME impl rest; basic idea manage prob declare model in memory with hash map and generation sequence by
        // FIXME creating a future for every request to the ocaml service that is finished in the response rabbit listener
        // FIXME using an executorService that schedules everytime the future of the current request is fullfiled,
    }

    private void scheduleDeclareGeneration(
            @NonNull ProbDeclare probDeclare,
            @NonNull List<Trace> traces
    ) {
        traces.forEach(
                trace -> {
                    generating.put(UUID.fromString(trace.getId()), new CompletableFuture<>());
                    executorService.submit(
                            () -> collectorService.transformAndPipe(
                                    trace.getId(),
                                    trace.getSpansAsJson(),
                                    TraceDataType.JAEGER_TRACE
                            )
                    );
                }
        );
    }


    @RabbitListener(queues = "${otel_to_probd.routing_key.in}")
    public void receiveProbDeclare(Message msg) {
        String model = new String(msg.getBody());
        log.info("received probdeclare result: {}", model);

        // TODO transform message to DeclareConstraintsPerTrace Object and
//            UUID id = UUID.randomUUID();
//            Declare declare = Declare.builder()
//                    .id(id.toString())
//                    .probability(1f)
//                    .build();
        // TODO persist
        // TODO add association probDeclare to Trace
    }
}
