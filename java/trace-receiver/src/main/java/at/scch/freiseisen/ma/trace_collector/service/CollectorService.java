package at.scch.freiseisen.ma.trace_collector.service;

import at.scch.freiseisen.ma.commons.TraceDataType;
import at.scch.freiseisen.ma.data_layer.dto.SpanTreeNodeConversionResponse;
import at.scch.freiseisen.ma.data_layer.entity.pre_processing.CanonizedSpanTree;
import at.scch.freiseisen.ma.trace_collector.configuration.OtelToProbdeclareConfiguration;
import at.scch.freiseisen.ma.trace_collector.configuration.RestConfig;
import at.scch.freiseisen.ma.trace_collector.error.MergeJsonNodeToResourceSpansBuilderException;
import at.scch.freiseisen.ma.trace_collector.error.TraceStringConversionException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectorService {
    private final RestTemplate restTemplate;
    private final RestConfig restConfig;
    private final RabbitTemplate rabbitTemplate;
    private final OtelToProbdeclareConfiguration otelToProbdeclareConfiguration;
    private final Map<String, String> traceModels = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * FIXME ignore for now and find way to convert otlp_proto to correct ResourceSpans or to receive otlp_json instead
     * @param msg
     * @throws InvalidProtocolBufferException
     */
    @RabbitListener(queues = "${open_telemetry.exporter.routing_key.traces}")
    public void collectTraces(Message msg) throws InvalidProtocolBufferException {
        log.info("received trace: {}", msg);
        ResourceSpans resourceSpans = ResourceSpans.parseFrom(msg.getBody());
        log.info("content as resource span:\n{}", resourceSpans);
    }

    @RabbitListener(queues = "${otel_to_probd.routing_key.in}")
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

    public String retrieveModel(String traceId) {
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

    public void transformAndPipe(String traceId, List<String> spans, TraceDataType traceDataType) {
        String routingKey = otelToProbdeclareConfiguration.determineRoutingKey(traceDataType);
        traceModels.put(traceId, "");
        rabbitTemplate.convertAndSend(routingKey, "[" + String.join(",", spans) + "]");
    }

    public void transformAndPipe(String trace, TraceDataType traceDataType) {
        String routingKey = otelToProbdeclareConfiguration.determineRoutingKey(traceDataType);
        rabbitTemplate.convertAndSend(routingKey, trace);
    }

    private List<ResourceSpans> transformTraceToResourceSpans(String trace) {
        JsonNode rootNode = convertToJsonTree(trace);
        JsonNode resourceSpansNode = rootNode.get("resourceSpans");
        List<ResourceSpans> resourceSpansList = new ArrayList<>();

        ResourceSpans.Builder builder = ResourceSpans.newBuilder();
        String resourceSpansJson;
        if (resourceSpansNode.isArray()) {
            for (final JsonNode objNode : resourceSpansNode) {
                resourceSpansJson = objNode.toString();
                mergeToBuilder(resourceSpansJson, builder);
                ResourceSpans resourceSpans = builder.build();
                resourceSpansList.add(resourceSpans);
                log.info("resourceSpan:\n{}", resourceSpans);
            }
        }

        return resourceSpansList;
    }

    private JsonNode convertToJsonTree(String trace) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readTree(trace);
        } catch (JsonProcessingException e) {
            throw new TraceStringConversionException(e);
        }
    }

    private void mergeToBuilder(String resourceSpansJson, ResourceSpans.Builder builder) {
        try {
            JsonFormat.parser().merge(resourceSpansJson, builder);
        } catch (InvalidProtocolBufferException e) {
            throw new MergeJsonNodeToResourceSpansBuilderException(e);
        }
    }

    public void addTraceModel(String traceId) {
        traceModels.put(traceId, "");
    }
}
