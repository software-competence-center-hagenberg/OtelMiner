package at.scch.freiseisen.ma.trace_collector.service;

import com.google.protobuf.InvalidProtocolBufferException;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CollectorService {

    @RabbitListener(queues = "${open_telemetry.exporter.routing_key.traces}")
    public void collectTraces(Message msg) throws InvalidProtocolBufferException {
        log.info("received trace: {}", msg);
        ResourceSpans resourceSpans = ResourceSpans.parseFrom(msg.getBody());
        log.info("content as resource span:\n{}", resourceSpans);
    }
}
