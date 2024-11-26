package at.scch.freiseisen.ma.db_initializer.source_extraction.parsing;

import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;

import java.nio.file.Path;
import java.util.HashMap;

@FunctionalInterface
public interface FileParser {
    void parse(Path path, HashMap<String, Trace> traces);
}
