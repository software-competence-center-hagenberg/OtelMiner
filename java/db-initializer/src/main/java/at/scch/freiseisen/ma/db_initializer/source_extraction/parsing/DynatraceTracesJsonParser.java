package at.scch.freiseisen.ma.db_initializer.source_extraction.parsing;

import at.scch.freiseisen.ma.commons.TraceDataType;
import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import at.scch.freiseisen.ma.db_initializer.error.FileParsingException;
import at.scch.freiseisen.ma.db_initializer.source_extraction.dto_creation.DTOCreator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynatraceTracesJsonParser implements FileParser {
    private final DTOCreator dtoCreator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void parse(Path path, HashMap<String, Trace> traces, TraceDataType traceDataType) {
        log.info("parsing {}", path);
        try {
            String content = Files.readString(path);
            JsonNode jaegerTrace = objectMapper.readTree(content);
            jaegerTrace.get("records").forEach(span -> {
                String traceId = span.get("trace.id").asText();
                String spanId = span.get("span.id").asText();
                String parentSpanId = extractParentSpanId(span);
                dtoCreator.addSpan(traceId, spanId, parentSpanId, path.toString(), span.toString(), traces,
                        traceDataType);
            });
        } catch (IOException e) {
            throw new FileParsingException(path.toString());
        }
        log.info("parsing done");
    }

    private String extractParentSpanId(JsonNode span) {
        if (!isRoot(span) && span.has("span.parent_id")) {
            return span.get("span.parent_id").asText();
        }

        return "";
    }

    private boolean isRoot(JsonNode span) {
        return span.has("request.is_root_span")
                && span.get("request.is_root_span").asBoolean();
    }
}
