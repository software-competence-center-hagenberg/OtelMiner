package at.scch.freiseisen.ma.db_initializer.source_extraction.parsing;

import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import at.scch.freiseisen.ma.db_initializer.error.FileParsingException;
import at.scch.freiseisen.ma.db_initializer.source_extraction.dto_creation.DTOCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtelTxtParser implements FileParser {
    private final DTOCreator dtoCreator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void parse(Path path, HashMap<String, Trace> traces) {
        log.info("parsing {}", path);
        String line;
        String traceId;
        String spanId;
        String parentSpanId;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(path)))) {
            while ((line = reader.readLine()) != null) {
                JsonNode jsonNode = objectMapper.readTree(line);
                traceId = jsonNode.get("traceId").asText();
                spanId = jsonNode.get("spanId").asText();
                parentSpanId = jsonNode.has("parentSpanId")
                        ? jsonNode.get("parentSpanId").asText()
                        : StringUtils.EMPTY;
                dtoCreator.addSpan(traceId, spanId, parentSpanId, path.toString(), line, traces);
            }
        } catch (IOException e) {
            throw new FileParsingException(path.toString(), e);
        }
        log.info("parsing done");
    }
}
