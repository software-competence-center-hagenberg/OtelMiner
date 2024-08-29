package at.scch.freiseisen.ma.db_initializer.init;

import at.scch.freiseisen.ma.db_initializer.source_extraction.ArchiveExtractor;
import at.scch.freiseisen.ma.db_initializer.source_extraction.FileParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
@RequiredArgsConstructor
public class Initializer {
    private final ResourceLoader resourceLoader;
    private final ArchiveExtractor archiveExtractor;
    private final FileParser fileParser;

    @EventListener(ApplicationReadyEvent.class)
    public void start() throws IOException {
        Resource archiveResource = resourceLoader.getResource("classpath:test-data/traces.tar.gz");
        Resource extractionDirectoryResource = resourceLoader.getResource("classpath:test-data/");
        Path extractionDirectory = Paths.get(extractionDirectoryResource.getURI());
        archiveExtractor.extractTarGz(archiveResource, extractionDirectory);
        fileParser.parseFiles(extractionDirectory, ".txt");
    }

}
