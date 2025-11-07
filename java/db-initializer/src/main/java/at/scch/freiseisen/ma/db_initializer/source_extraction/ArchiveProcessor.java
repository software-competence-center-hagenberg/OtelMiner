package at.scch.freiseisen.ma.db_initializer.source_extraction;

import at.scch.freiseisen.ma.commons.TraceDataType;
import at.scch.freiseisen.ma.db_initializer.source_extraction.parsing.FileParser;
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
public class ArchiveProcessor {
    private final FileProcessor fileProcessor;

    public void processTarGz(Resource archive, Path destinationDirectory, FileParser parser,
                             TraceDataType traceDataType, boolean sample) throws IOException {
        log.info("Extracting tar gz archive from {} to {}", archive, destinationDirectory);
        try (InputStream fileInputStream = archive.getInputStream();
             InputStream gzInput = new GzipCompressorInputStream(fileInputStream);
             TarArchiveInputStream tarInput = new TarArchiveInputStream(gzInput)) {
            process(tarInput, destinationDirectory, parser, traceDataType, sample);
        }
        log.info("Finished extracting tar gz archive of {}", archive);
    }

    public void processZip(Resource archive, Path destinationDirectory, FileParser parser, TraceDataType traceDataType,
                           boolean sample) throws IOException {
        log.info("Extracting zip archive from {} to {}", archive, destinationDirectory);
        try (InputStream fileInputStream = archive.getInputStream();
             ZipArchiveInputStream zipInput = new ZipArchiveInputStream(fileInputStream)) {
            process(zipInput, destinationDirectory, parser, traceDataType, sample);
        }
        log.info("Finished extracting zip archive of {}", archive);
    }

    private <T extends ArchiveInputStream<S>, S extends ArchiveEntry> void process(T input, Path destinationDirectory, FileParser parser, TraceDataType traceDataType, boolean sample) throws IOException {
        S entry;
        Path destinationPath;
        while ((entry = input.getNextEntry()) != null) {
            destinationPath = destinationDirectory.resolve(entry.getName()).toAbsolutePath().normalize();
//            if (entry.isDirectory()) {
//                Files.createDirectories(destinationPath);
//            } else {
//                if (destinationPath.getParent() != null) {
//                    Files.createDirectories(destinationPath.getParent());
//                }
//                Files.copy(input, destinationPath, StandardCopyOption.REPLACE_EXISTING);
//            }
            if (!entry.isDirectory() && entry.getName().endsWith(".json")) {
                Path tempFile = Files.createTempFile("trace-", ".json");
                log.info("extracting entry {} to {}", entry.getName(), tempFile);
                Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
                fileProcessor.parseSingleFile(tempFile, parser, traceDataType, sample);
                Files.deleteIfExists(tempFile);
            }
        }
    }


}
