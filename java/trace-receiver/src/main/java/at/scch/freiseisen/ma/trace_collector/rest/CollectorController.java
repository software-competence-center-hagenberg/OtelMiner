package at.scch.freiseisen.ma.trace_collector.rest;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.ScopeSpans;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1")
public class CollectorController {

    @PostMapping("/traces")
    public void receiveTraces(@RequestBody String traces) throws IOException {
        log.info("traces:\n{}", traces);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(traces);
        JsonNode resourceSpansNode = rootNode.get("resourceSpans");
        List<ResourceSpans> resourceSpansList = new ArrayList<>();

        if (resourceSpansNode.isArray()) {
            for (final JsonNode objNode : resourceSpansNode) {
                String resourceSpansJson = objNode.toString();
                ResourceSpans.Builder builder = ResourceSpans.newBuilder();
                JsonFormat.parser().merge(resourceSpansJson, builder);
                ResourceSpans resourceSpans = builder.build();
                resourceSpansList.add(resourceSpans);
                log.info("resourceSpan:\n{}", resourceSpans);
            }
        }
    }
}
