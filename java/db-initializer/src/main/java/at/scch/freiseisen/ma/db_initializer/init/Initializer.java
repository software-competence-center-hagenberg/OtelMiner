package at.scch.freiseisen.ma.db_initializer.init;

import at.scch.freiseisen.ma.db_initializer.source_extraction.ArchiveExtractor;
import at.scch.freiseisen.ma.db_initializer.source_extraction.FileProcessor;
import at.scch.freiseisen.ma.db_initializer.source_extraction.parsing.DynatraceTracesJsonParser;
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
import java.nio.file.Paths;

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
    @Value("${test-data.file-paths}")
    private String resourceLocation;

    /**
     * <pre>
     *     takes file-path(s) at resourceLocation, extracts archive if present.
     *     Then parses entries.
     *     Currently only supports .json with jaeger traces
     * </pre>
     */
    @EventListener(ApplicationReadyEvent.class)
    public void start() throws IOException {
        Resource archiveResource = resourceLoader.getResource("classpath:" + resourceLocation);
        Path extractionDirectory = Files.createTempDirectory("extraction");
        if (resourceLocation.endsWith(".tar.gz")) {
            archiveExtractor.extractTarGz(archiveResource, extractionDirectory);
        } else if (resourceLocation.endsWith(".zip")) {
            archiveExtractor.extractZip(archiveResource, extractionDirectory);
        }
        fileProcessor.parseFiles(extractionDirectory, ".json", dynatraceTracesJsonParser);
        /*
        OLD:
        Resource archiveResource = resourceLoader.getResource("classpath:test-data/2024-05-23-11-26-05-ts-error-F8-generated-with-ts-travel-service.tar.gz");
        Resource extractionDirectoryResource = resourceLoader.getResource("classpath:test-data/");
        Path extractionDirectory = Paths.get(extractionDirectoryResource.getURI());
        archiveExtractor.extractTarGz(archiveResource, extractionDirectory);
        Resource targetDirectoryResource = resourceLoader.getResource("classpath:test-data/traces-jaeger/");
        Path targetDirectory = Paths.get(targetDirectoryResource.getURI());
        fileProcessor.parseFiles(targetDirectory, ".json", jaegerTracesJsonParser);
         */
    }
}
