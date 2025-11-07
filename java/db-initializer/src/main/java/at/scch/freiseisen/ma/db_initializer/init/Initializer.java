package at.scch.freiseisen.ma.db_initializer.init;

import at.scch.freiseisen.ma.commons.TraceDataType;
import at.scch.freiseisen.ma.db_initializer.source_extraction.ArchiveProcessor;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Component
@RequiredArgsConstructor
public class Initializer {
    private final ResourceLoader resourceLoader;
    private final ArchiveProcessor archiveProcessor;
    private final FileProcessor fileProcessor;
    private final OtelTxtParser otelTxtParser;
    private final JaegerTracesJsonParser jaegerTracesJsonParser;
    private final DynatraceTracesJsonParser dynatraceTracesJsonParser;
    @Value("${test-data.file-paths.dynatrace}")
    private String dynatraceData;
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
            if (dynatraceData != null && !dynatraceData.isBlank()) {
                log.info("Loading Dynatrace traces...");
                unpackDataAndPopulateDatabase(dynatraceData, dynatraceTracesJsonParser, TraceDataType.DYNATRACE_SPANS_LIST, false);
            }
            if (sampledData != null && sampledData.length > 0) {
                log.info("Creating Sample for train-ticket system...");
                for (int i = 0; i < sampledData.length; i++) {
                    log.info("processing sample archive {} ...", i);
                    unpackDataAndPopulateDatabase(sampledData[i], jaegerTracesJsonParser, TraceDataType.JAEGER_SPANS_LIST, true);
                }
            }
        } catch (IOException e) {
            log.error("error initializing db {}", e.getMessage());
        }
        log.info("... finished populating database.");
        log.info("exiting ...");
        System.exit(0);
    }

    private void unpackDataAndPopulateDatabase(String resourceLocation, FileParser parser, TraceDataType traceDataType,
                                               boolean sample) throws IOException {
        Resource archiveResource = resourceLoader.getResource("file:" + resolvePath(resourceLocation));
        Path extractionDirectory = Files.createTempDirectory("extraction");
        if (resourceLocation.endsWith(".tar.gz")) {
            archiveProcessor.processTarGz(archiveResource, extractionDirectory, parser, traceDataType, sample);
        } else if (resourceLocation.endsWith(".zip")) {
            archiveProcessor.processZip(archiveResource, extractionDirectory, parser, traceDataType, sample);
        }
    }

    /**
     * <pre>
     *     Helper method for resolving relative paths.
     *     If the given path is already an absolute path -> keep it.
     *     Else, resolve relative to project root (current working dir)
     * </pre>
     * @param path to resolve
     * @return resolved absolute path
     */
    private String resolvePath(String path) {
        File file = new File(path);
        if (file.isAbsolute()) {
            return file.getAbsolutePath();
        }

        return new File(System.getProperty("user.dir"), path).getAbsolutePath();
    }
}
