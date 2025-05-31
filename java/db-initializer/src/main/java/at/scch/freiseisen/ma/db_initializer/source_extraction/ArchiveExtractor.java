package at.scch.freiseisen.ma.db_initializer.source_extraction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArchiveExtractor {

    public void extractTarGz(Resource archive, Path destinationDirectory) throws IOException {
        log.info("Extracting tar gz archive from {} to {}", archive, destinationDirectory);
        try (InputStream fileInputStream = archive.getInputStream();
             InputStream gzInput = new GzipCompressorInputStream(fileInputStream);
             TarArchiveInputStream tarInput = new TarArchiveInputStream(gzInput)) {
            extract(tarInput, destinationDirectory);
        }
        log.info("Finished extracting tar gz archive of {}", archive);
    }

    public void extractZip(Resource archive, Path destinationDirectory) throws IOException {
        log.info("Extracting zip archive from {} to {}", archive, destinationDirectory);
        try (InputStream fileInputStream = archive.getInputStream();
             ZipArchiveInputStream zipInput = new ZipArchiveInputStream(fileInputStream)) {
            extract(zipInput, destinationDirectory);
        }
        log.info("Finished extracting zip archive of {}", archive);
    }

    private <T extends ArchiveInputStream<S>, S extends ArchiveEntry> void extract(T tarInput, Path destinationDirectory) throws IOException {
        S entry;
        Path destinationPath;
        while ((entry = tarInput.getNextEntry()) != null) {
            destinationPath = destinationDirectory.resolve(entry.getName()).toAbsolutePath().normalize();
            log.info("extracting entry {} to destinationPath {}", entry.getName(), destinationPath);
            if (entry.isDirectory()) {
                Files.createDirectories(destinationPath);
            } else {
                if (destinationPath.getParent() != null) {
                    Files.createDirectories(destinationPath.getParent());
                }
                Files.copy(tarInput, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
