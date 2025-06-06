package at.scch.freiseisen.ma.db_initializer.source_extraction.dto_creation;

import at.scch.freiseisen.ma.commons.TraceDataType;
import at.scch.freiseisen.ma.data_layer.entity.otel.Span;
import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DTOCreator {

    public Trace createTrace(String traceId, String sourceFile, Span span, TraceDataType traceDataType) {
        return Trace.builder()
                .id(traceId)
                .spans(new ArrayList<>(List.of(span)))
                .sourceFile(sourceFile)
                .traceDataType(traceDataType.name())
                .build();
    }

    public void addSpan(String traceId, String spanId, String parentSpanId, String sourceFile, String line,
                        Map<String, Trace> traces, TraceDataType traceDataType) {
        Span span = Span.builder()
                .id(spanId)
                .parentId(parentSpanId)
                .json(line)
                .build();
        if (traces.containsKey(traceId)) {
            span.setTrace(traces.get(traceId));
            traces.get(traceId).getSpans().add(span);
        } else {
            Trace trace = createTrace(traceId, sourceFile, span, traceDataType);
            trace.getSpans().forEach(s -> s.setTrace(trace));
            traces.put(traceId, trace);
        }
    }

}
