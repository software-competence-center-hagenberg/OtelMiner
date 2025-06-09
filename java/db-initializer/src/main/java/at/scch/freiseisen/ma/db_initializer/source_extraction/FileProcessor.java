package at.scch.freiseisen.ma.db_initializer.source_extraction;

import at.scch.freiseisen.ma.commons.TraceDataType;
import at.scch.freiseisen.ma.data_layer.entity.otel.Span;
import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import at.scch.freiseisen.ma.data_layer.service.SpanService;
import at.scch.freiseisen.ma.data_layer.service.TraceService;
import at.scch.freiseisen.ma.db_initializer.source_extraction.parsing.FileParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Service
public class FileProcessor {
    private final TraceService traceService;
    private final SpanService spanService;
    private final HashMap<String, Trace> traces = new HashMap<>();
    private final HashMap<Integer, List<Trace>> tracesByNrNodes = new HashMap<>();

    @Value("${db-service.url}")
    private String dbServiceUrl;

    public FileProcessor(TraceService traceService, SpanService spanService) {
        this.traceService = traceService;
        this.spanService = spanService;
    }

    public void parseFiles(Path directory, String fileType, FileParser fileParser, TraceDataType traceDataType, boolean sample) throws IOException {
        log.info("parsing all '{}' files from {}", fileType, directory);
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(fileType))
                    .forEach(path -> fileParser.parse(path, traces, traceDataType));
        }
        traces.values().forEach(t -> {
            t.setSpans(t.getSpans().stream().distinct().toList());
            int nrNodes = t.getSpans().size();
            t.setNrNodes(nrNodes);
            if (tracesByNrNodes.containsKey(nrNodes)) {
                tracesByNrNodes.get(nrNodes).add(t);
            } else {
                tracesByNrNodes.put(nrNodes, new ArrayList<>(List.of(t)));
            }
        });
        log.info("########## traces found with n nodes: ###########");
        tracesByNrNodes.forEach((nrNodes, traces) -> {
            log.info("{} traces with {} nodes found", traces.size(), nrNodes);
            if (!sample) {
                if (nrNodes >= 5) {
                    processNormaly(traces);
                }
            } else {
                processAndSample(traces);
            }
        });
        log.info("#################################################");
        log.info("cleaning up state");
        traces.clear();
        tracesByNrNodes.clear();
        log.info("state cleaned up");
    }

    private void processAndSample(List<Trace> traces) {
        Map<Integer, Trace> sampled = new HashMap<>();
        traces.forEach(trace -> {
            int key = (trace.getSourceFile() + trace.getNrNodes()).hashCode();
            if (!sampled.containsKey(key)) {
                trace.setSourceFile("sampled-train-ticket");
                sampled.put(key, trace);
            }
        });
        processNormaly(new ArrayList<>(sampled.values()));
    }

    private void processNormaly(List<Trace> traces) {
        traces.forEach(trace -> {
            List<Span> spans = trace.getSpans();
            trace.setSpans(Collections.emptyList());
            Trace t = traceService.save(trace);
            spans.forEach(s -> s.setTrace(t));
            spanService.saveAll(spans);
        });
    }
}
