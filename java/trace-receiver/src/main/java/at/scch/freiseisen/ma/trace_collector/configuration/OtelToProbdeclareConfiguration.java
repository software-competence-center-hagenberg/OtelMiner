package at.scch.freiseisen.ma.trace_collector.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class OtelToProbdeclareConfiguration {

    @Value("${otel_to_probd.routing_key.out.resource_spans_queue}")
    private String resourceSpansQueue;
    @Value("${otel_to_probd.routing_key.out.trace_spans_queue}")
    private String traceSpansQueue;
    @Value("${otel_to_probd.routing_key.out.jaeger-trace-queue}")
    private String jaegerTraceQueue;
    @Value("${otel_to_probd.routing_key.out.jaeger_trace_spans_list_queue}")
    private String jaegerTraceSpansListQueue;
}
