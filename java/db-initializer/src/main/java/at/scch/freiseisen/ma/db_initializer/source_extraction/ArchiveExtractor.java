package at.scch.freiseisen.ma.db_initializer.source_extraction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArchiveExtractor {

    public void extractTarGz(Path archive, Path destinationDirectory) throws IOException {
        log.info("Extracting tar gz archive from {} to {}", archive, destinationDirectory);
        try (InputStream fileInputStream = Files.newInputStream(archive);
             InputStream gzInput = new GzipCompressorInputStream(fileInputStream);
             TarArchiveInputStream tarInput = new TarArchiveInputStream(gzInput)) {

            TarArchiveEntry entry;
            Path destinationPath;
            while ((entry = tarInput.getNextEntry()) != null) {
                destinationPath = Paths.get(destinationDirectory.toString(), entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(destinationPath);
                } else {
                    if (destinationPath.getParent() != null) {
                        Files.createDirectories(destinationPath.getParent());
                    }
                    Files.copy(tarInput, destinationPath);
                }
            }
        }
        log.info("Finished extracting tar gz archive of {}", archive);
    }
}
