package at.scch.freiseisen.ma.db_initializer.source_extraction;

import at.scch.freiseisen.ma.data_layer.entity.otel.Span;
import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import at.scch.freiseisen.ma.db_initializer.error.FileParsingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileParser {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HashMap<String, Trace> traces = new HashMap<>();
    private final HashMap<Integer, List<Trace>> tracesByNrNodes = new HashMap<>();

    public void parseFiles(Path directory, String fileType) throws IOException {
        log.info("parsing all '{}' files from {}", fileType, directory);
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(fileType))
                    .forEach(this::parseFile);
        }
        traces.values().forEach(t -> {
            int nrNodes = t.getSpans().size();
            t.setNrNodes(nrNodes);
            if (tracesByNrNodes.containsKey(nrNodes)) {
                tracesByNrNodes.get(nrNodes).add(t);
            } else {
                tracesByNrNodes.put(nrNodes, new ArrayList<>(List.of(t)));
            }
        });
        log.info("########## traces found with mor than 5 nodes: ###########");
        tracesByNrNodes.entrySet().forEach(entry -> {
            log.info("{} traces with {} nodes found", entry.getValue().size(), entry.getKey());
            if (entry.getKey() >= 5) {
                restTemplate.postForLocation("http://localhost:4242/v1/traces", entry.getValue());
            }
        });
        log.info("##########################################################");
    }

    private void parseFile(Path path) {
        log.info("parsing {}", path);
        try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                addSpan(path, line);
            }
        } catch (IOException e) {
            throw new FileParsingException(path.toString());
        }
        log.info("parsing done");
    }

    private Trace createTrace(String traceId, Path path, Span span) {
        return Trace.builder()
                .id(traceId)
                .spans(new ArrayList<>(List.of(span)))
                .sourceFile(path.toString())
                .build();
    }

    private void addSpan(Path path, String line) {
        String traceId;
        String spanId;
        String parentSpanId;
        try {
            JsonNode jsonNode = objectMapper.readTree(line);
            traceId = jsonNode.get("traceId").asText();
            spanId = jsonNode.get("spanId").asText();
            parentSpanId = jsonNode.has("parentSpanId")
                    ? jsonNode.get("parentSpanId").asText()
                    : StringUtils.EMPTY;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Span span = Span.builder()
                .id(spanId)
                .parentId(parentSpanId)
                .json(line)
                .build();
        if (!traces.containsKey(traceId)) {
            traces.put(traceId, createTrace(traceId, path, span));
        } else {
            traces.get(traceId).getSpans().add(span);
        }
    }
}
