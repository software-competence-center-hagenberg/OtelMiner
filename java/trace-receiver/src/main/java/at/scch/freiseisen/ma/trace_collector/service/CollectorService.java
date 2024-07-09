package at.scch.freiseisen.ma.trace_collector.service;

import io.micrometer.tracing.Span;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CollectorService {

    @RabbitListener(queues = "${open_telemetry.exporter.routing_key.traces}")
    public void collectTraces(Span span) {
        log.info("received trace: {}", span);
    }

//    @RabbitListener(queues = "${open_telemetry.exporter.routing_key.metrics}")
//    public void collectMetrics() {
//        throw new RuntimeException("metrics not implemented");
//    }
//
//    @RabbitListener(queues = "${open_telemetry.exporter.routing_key.logs}")
//    public void collectSpans() {
//        throw new RuntimeException("spans not implemented");
//    }
}
