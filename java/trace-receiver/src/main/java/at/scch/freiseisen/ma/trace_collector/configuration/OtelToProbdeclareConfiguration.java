package at.scch.freiseisen.ma.trace_collector.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class OtelToProbdeclareConfiguration {

    @Value("${otel_to_probd.routing_key.out}")
    private String otelToProbdeclareRoutingKey;
}
