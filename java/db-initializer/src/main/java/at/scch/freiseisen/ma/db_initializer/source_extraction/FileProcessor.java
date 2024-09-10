package at.scch.freiseisen.ma.db_initializer.source_extraction;

import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import at.scch.freiseisen.ma.db_initializer.source_extraction.parsing.FileParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
public class FileProcessor {
    private final RestTemplate restTemplate;
    private final HashMap<String, Trace> traces = new HashMap<>();
    private final HashMap<Integer, List<Trace>> tracesByNrNodes = new HashMap<>();

    @Value("${db-service.url}")
    private String dbServiceUrl;

    public void parseFiles(Path directory, String fileType, FileParser fileParser) throws IOException {
        log.info("parsing all '{}' files from {}", fileType, directory);
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(fileType))
                    .forEach(path -> fileParser.parse(path, traces));
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
        log.info("########## traces found with n nodes: ###########");
        tracesByNrNodes.forEach((key, value) -> {
            log.info("{} traces with {} nodes found", value.size(), key);
            if (key >= 5) {
                value.forEach(t -> t.setSpans(t.getSpans().stream().distinct().toList()));
                restTemplate.postForLocation(dbServiceUrl + "/v1/traces", value.stream().distinct().toList());
            }
        });
        log.info("#################################################");
    }
}
