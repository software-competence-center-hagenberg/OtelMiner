package at.scch.freiseisen.ma.db_initializer.source_extraction.parsing;

import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import at.scch.freiseisen.ma.db_initializer.error.FileParsingException;
import at.scch.freiseisen.ma.db_initializer.source_extraction.dto_creation.DTOCreator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class JaegerTracesJsonParser implements FileParser {
    private final DTOCreator dtoCreator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void parse(Path path, HashMap<String, Trace> traces) {
        log.info("parsing {}", path);
        try {
            String content = Files.readString(path);
            JsonNode jaegerTrace = objectMapper.readTree(content);
            jaegerTrace.get("data").forEach(trace -> {
                JsonNode spans = trace.get("spans");
                spans.forEach(span -> {
                    String traceId = span.get("traceID").asText();
                    String spanId = span.get("spanID").asText();
                    String parentSpanId = extractParentSpanId(span);
                    dtoCreator.addSpan(traceId, spanId, parentSpanId, path.toString(), span.toString(), traces);
                });
            });
        } catch (IOException e) {
            throw new FileParsingException(path.toString());
        }
        log.info("parsing done");
    }

    private String extractParentSpanId(JsonNode span) {
        AtomicReference<String> parentSpanId = new AtomicReference<>(StringUtils.EMPTY);
        if (span.has("references")) {
            span.get("references").forEach(reference -> {
                if (reference.has("refType")
                    && reference.get("refType").asText().equals("CHILD_OF")
                    && reference.has("spanID")) {
                    parentSpanId.set(reference.get("spanID").asText());
                }
            });
        }
        return parentSpanId.get();
    }
}
