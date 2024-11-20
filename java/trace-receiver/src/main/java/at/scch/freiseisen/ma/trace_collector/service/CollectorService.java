package at.scch.freiseisen.ma.trace_collector.service;

import at.scch.freiseisen.ma.commons.TraceDataType;
import at.scch.freiseisen.ma.trace_collector.configuration.OtelToProbdeclareConfiguration;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectorService {
    private final RabbitTemplate rabbitTemplate;
    private final OtelToProbdeclareConfiguration otelToProbdeclareConfiguration;
    private final Map<String, String> traceModels = new HashMap<>();

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

    @Deprecated(forRemoval = true) // FIXME marker for moving rabbit methods methods to ProbDeclareManagerService
    @RabbitListener(queues = "${otel_to_probd.routing_key.in}")
    public void receiveProbDeclare(Message msg) {
        String model = new String(msg.getBody());
        log.info("received probdeclare result: {}", model);
        traceModels.put("CHANGEME", model);
    }

    @Deprecated(forRemoval = true) // FIXME marker for moving rabbit methods methods to ProbDeclareManagerService
    public String retrieveModel(String traceId) {
        log.info("retrieving model for trace: {}", traceId);
        return traceModels.get("CHANGEME");
    }

    @Deprecated(forRemoval = true) // FIXME marker for moving rabbit methods methods to ProbDeclareManagerService
    public void transformAndPipe(String traceId, List<String> trace, TraceDataType traceDataType) {
        String routingKey = otelToProbdeclareConfiguration.determineRoutingKey(traceDataType);
        //traceModels.put(traceId, "");
        traceModels.put("CHANGEME", "");
        rabbitTemplate.convertAndSend(routingKey, "[" + String.join(",", trace) + "]");
    }

    @Deprecated(forRemoval = true) // FIXME marker for moving rabbit methods methods to ProbDeclareManagerService
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
}
