package at.scch.freiseisen.ma.model_generator.service;

import at.scch.freiseisen.ma.commons.TraceDataType;
import at.scch.freiseisen.ma.model_generator.configuration.OtelToProbdeclareConfiguration;
import at.scch.freiseisen.ma.model_generator.error.MergeJsonNodeToResourceSpansBuilderException;
import at.scch.freiseisen.ma.model_generator.error.TraceStringConversionException;
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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceSpansService {
    private final RabbitTemplate rabbitTemplate;
    private final OtelToProbdeclareConfiguration otelToProbdeclareConfiguration;

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

    public void transformAndPipe(String trace, TraceDataType traceDataType) {
        String routingKey = otelToProbdeclareConfiguration.determineRoutingKey(traceDataType);
        rabbitTemplate.convertAndSend(routingKey, trace);
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
