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
                    String parentSpanId = span.has("parentSpanID")
                            ? span.get("parentSpanID").asText()
                            : StringUtils.EMPTY;
                    dtoCreator.addSpan(traceId, spanId, parentSpanId, path.toString(), span.toString(), traces);
                });
            });
        } catch (IOException e) {
            throw new FileParsingException(path.toString());
        }
        log.info("parsing done");
    }
}
