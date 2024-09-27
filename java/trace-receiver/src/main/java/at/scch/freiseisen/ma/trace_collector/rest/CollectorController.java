package at.scch.freiseisen.ma.trace_collector.rest;

import at.scch.freiseisen.ma.commons.TraceDataType;
import at.scch.freiseisen.ma.trace_collector.service.CollectorService;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.ScopeSpans;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@RequestMapping("/v1")
public class CollectorController {
    private final CollectorService collectorService;

    @PostMapping("/traces")
    public void receiveTraces(@RequestBody String trace) {
        log.info("trace:\n{}", trace);
        collectorService.transformAndPipe(trace, TraceDataType.RESOURCE_SPANS);
    }
}
