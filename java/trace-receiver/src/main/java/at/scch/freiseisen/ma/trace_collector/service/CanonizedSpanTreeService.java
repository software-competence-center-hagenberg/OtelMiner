package at.scch.freiseisen.ma.trace_collector.service;

import at.scch.freiseisen.ma.commons.TraceDataType;
import at.scch.freiseisen.ma.data_layer.dto.SpanTreeNodeConversionResponse;
import at.scch.freiseisen.ma.data_layer.entity.pre_processing.CanonizedSpanTree;
import at.scch.freiseisen.ma.trace_collector.configuration.OtelToProbdeclareConfiguration;
import at.scch.freiseisen.ma.trace_collector.configuration.RestConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class CanonizedSpanTreeService {
    private final RestTemplate restTemplate;
    private final RestConfig restConfig;
    private final RabbitTemplate rabbitTemplate;
    private final OtelToProbdeclareConfiguration otelToProbdeclareConfiguration;
    private final Map<String, String> traceModels = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = "${otel_to_probd.routing_key.in.span_trees}")
    public void receiveProbDeclare(Message msg) {
        String model = new String(msg.getBody());
        log.info("received probdeclare result: {}", model);
        try {
            SpanTreeNodeConversionResponse spanTreeNode = objectMapper.readValue(model, SpanTreeNodeConversionResponse.class);
            traceModels.put(spanTreeNode.getTraceId(), model);
            CanonizedSpanTree canonizedSpanTree = CanonizedSpanTree.builder()
                    .id(spanTreeNode.getTraceId())
                    .traceId(spanTreeNode.getTraceId())
                    .canonizedSpanTree(model)
                    .build();
            restTemplate.postForLocation(restConfig.canonizedSpanTreeUrl + "/one", canonizedSpanTree);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String retrieveSpanTrees(String traceId) {
        log.info("retrieving model for trace: {}", traceId);
        if (traceModels.containsKey(traceId)) {
            String model = traceModels.get(traceId);
            log.info("retrieved model: {}", model);
            return model;
        }
        log.info("no model present in local storage -> accessing data base");
        return Objects.requireNonNull(
                restTemplate.getForEntity(
                        restConfig.canonizedSpanTreeUrl + "/" + traceId,
                        CanonizedSpanTree.class
                ).getBody()
        ).getCanonizedSpanTree();
    }

    public String generateSpanTreesFromSpanList(String traceId, List<String> spans) {
        log.info("generating canonized span trees for trace {}... deleting existing", traceId);
        restTemplate.delete(restConfig.canonizedSpanTreeUrl + "/" + traceId);
        transformAndPipe(traceId, spans, TraceDataType.JAEGER_SPANS_LIST);
        return traceId;
    }

    private void transformAndPipe(String traceId, List<String> spans, TraceDataType traceDataType) {
        String routingKey = otelToProbdeclareConfiguration.determineRoutingKey(traceDataType);
        traceModels.put(traceId, "");
        rabbitTemplate.convertAndSend(routingKey, "[" + String.join(",", spans) + "]");
    }
}
