package at.scch.freiseisen.ma.db_initializer.source_extraction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArchiveExtractor {

    public void extractTarGz(Resource archive, Path destinationDirectory) throws IOException {
        log.info("Extracting tar gz archive from {} to {}", archive, destinationDirectory);
        try (InputStream fileInputStream = archive.getInputStream();
             InputStream gzInput = new GzipCompressorInputStream(fileInputStream);
             TarArchiveInputStream tarInput = new TarArchiveInputStream(gzInput)) {

            TarArchiveEntry entry;
            Path destinationPath;
            while ((entry = tarInput.getNextEntry()) != null) {
                log.info("extracting entry {}", entry.getName());
                destinationPath = destinationDirectory.resolve(entry.getName());
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
