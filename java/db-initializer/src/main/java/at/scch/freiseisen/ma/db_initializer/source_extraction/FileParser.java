package at.scch.freiseisen.ma.db_initializer.source_extraction;

import at.scch.freiseisen.ma.db_initializer.error.FileParsingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Slf4j
@Service
public class FileParser {
    public void parseFiles(String directory, String fileType) throws IOException {
        log.info("parsing all '{}' files from {}", fileType, directory);
        try (Stream<Path> paths = Files.walk(Paths.get(directory))) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(fileType))
                    .forEach(this::parseFile);
        }
    }

    private void parseFile(Path path) {
        log.info("parsing {}", path);
        try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Process each line
                log.info("parsing line: {}}", line);
            }
        } catch (IOException e) {
            throw new FileParsingException(path.toString());
        }
        log.info("parsing done");
    }
}
