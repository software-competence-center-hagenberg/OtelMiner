package at.scch.freiseisen.ma.db_initializer.init;

import at.scch.freiseisen.ma.commons.TraceDataType;
import at.scch.freiseisen.ma.db_initializer.source_extraction.ArchiveExtractor;
import at.scch.freiseisen.ma.db_initializer.source_extraction.FileProcessor;
import at.scch.freiseisen.ma.db_initializer.source_extraction.parsing.DynatraceTracesJsonParser;
import at.scch.freiseisen.ma.db_initializer.source_extraction.parsing.FileParser;
import at.scch.freiseisen.ma.db_initializer.source_extraction.parsing.JaegerTracesJsonParser;
import at.scch.freiseisen.ma.db_initializer.source_extraction.parsing.OtelTxtParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Component
@RequiredArgsConstructor
public class Initializer {
    private final ResourceLoader resourceLoader;
    private final ArchiveExtractor archiveExtractor;
    private final FileProcessor fileProcessor;
    private final OtelTxtParser otelTxtParser;
    private final JaegerTracesJsonParser jaegerTracesJsonParser;
    private final DynatraceTracesJsonParser dynatraceTracesJsonParser;
    @Value("${test-data.file-paths.dynatrace}")
    private String dynatraceData;
    @Value("${test-data.file-paths.jaeger}")
    private String jaegerData;
    @Value("${test-data.file-paths.sample}")
    private String[] sampledData;

    /**
     * <pre>
     *     takes file-path(s) at resourceLocation, extracts archive if present.
     *     Then parses entries.
     *     Currently only supports .json with jaeger traces
     * </pre>
     */
    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        log.info("Starting DB initialization...");
        try {
            if (dynatraceData != null) {
                log.info("Loading Dynatrace traces...");
                unpackDataAndPopulateDatabase(dynatraceData, dynatraceTracesJsonParser, TraceDataType.DYNATRACE_SPANS_LIST);
            }
            if (jaegerData != null) {
                log.info("Loading Jaeger traces...");
                unpackDataAndPopulateDatabase(jaegerData, jaegerTracesJsonParser, TraceDataType.JAEGER_SPANS_LIST);
            }
            if (sampledData != null && sampledData.length > 0) {
                log.info("Creating Sample for train-ticket system...");
                for (int i = 0; i < sampledData.length; i++) {
                    log.info("processing sample archive {} ...", i);
                    unpackDataAndPopulateDatabase(sampledData[i], jaegerTracesJsonParser, TraceDataType.JAEGER_SPANS_LIST, true);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("... finished populating database.");
        log.info("exiting ...");
        System.exit(0);
    }

    private void unpackDataAndPopulateDatabase(String resourceLocation, FileParser parser, TraceDataType traceDataType)
            throws IOException {
        unpackDataAndPopulateDatabase(resourceLocation, parser, traceDataType, false);
    }

    private void unpackDataAndPopulateDatabase(String resourceLocation, FileParser parser, TraceDataType traceDataType,
                                               boolean sample) throws IOException {
        Resource archiveResource = resourceLoader.getResource("classpath:" + resourceLocation);
        Path extractionDirectory = Files.createTempDirectory("extraction");
        if (resourceLocation.endsWith(".tar.gz")) {
            archiveExtractor.extractTarGz(archiveResource, extractionDirectory);
        } else if (resourceLocation.endsWith(".zip")) {
            archiveExtractor.extractZip(archiveResource, extractionDirectory);
        }
        fileProcessor.parseFiles(extractionDirectory, ".json", parser, traceDataType, sample);
    }
}
