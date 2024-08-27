package at.scch.freiseisen.ma.db_initializer.init;

import at.scch.freiseisen.ma.db_initializer.source_extraction.ArchiveExtractor;
import at.scch.freiseisen.ma.db_initializer.source_extraction.FileParser;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class Initializer {
    private final ArchiveExtractor archiveExtractor;
    private final FileParser fileParser;

    @EventListener(ApplicationReadyEvent.class)
    public void start() throws IOException {
        String extractDir = "src/main/resources/test-data/";
        archiveExtractor.extractTarGz(extractDir + "traces.tar.gz", extractDir);
        fileParser.parseFiles(extractDir, ".txt");
    }

}
