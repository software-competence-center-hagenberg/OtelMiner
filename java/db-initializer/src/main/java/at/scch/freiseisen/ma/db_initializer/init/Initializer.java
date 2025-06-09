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

    /**
     * <pre>
     *     takes file-path(s) at resourceLocation, extracts archive if present.
     *     Then parses entries.
     *     Currently only supports .json with jaeger traces
     * </pre>
     */
    @EventListener(ApplicationReadyEvent.class)
    public void start() throws IOException {
        log.info("Starting DB initialization...");
        log.info("Loading Dynatrace traces...");
        unpackDataAndPopupateDatabase(dynatraceData, dynatraceTracesJsonParser, TraceDataType.DYNATRACE_SPANS_LIST);
        log.info("Loading Jaeger traces...");
        unpackDataAndPopupateDatabase(jaegerData, jaegerTracesJsonParser, TraceDataType.JAEGER_SPANS_LIST);
        log.info("... finished populating database.");
    }

    private void unpackDataAndPopupateDatabase(String resourceLocation, FileParser parser, TraceDataType traceDataType) throws IOException{
        Resource archiveResource = resourceLoader.getResource("classpath:" + resourceLocation);
        Path extractionDirectory = Files.createTempDirectory("extraction");
        if (resourceLocation.endsWith(".tar.gz")) {
            archiveExtractor.extractTarGz(archiveResource, extractionDirectory);
        } else if (resourceLocation.endsWith(".zip")) {
            archiveExtractor.extractZip(archiveResource, extractionDirectory);
        }
        fileProcessor.parseFiles(extractionDirectory, ".json", parser, traceDataType);
    }
}
