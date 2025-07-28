package at.scch.freiseisen.ma.model_generator.configuration;

import io.opentelemetry.sdk.trace.SdkTracerProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenTelemetryConfiguration {

    @Bean
    public SdkTracerProvider tracerProvider() {
        return SdkTracerProvider.builder().build();
    }
}
