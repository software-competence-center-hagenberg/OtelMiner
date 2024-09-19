package at.scch.freiseisen.ma.trace_collector.configuration;

import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestConfig {

    public final String dataOverviewUrl;
    public final String spansUrl;

    public RestConfig(
            @Value("${db-service.base-url}") String dbServiceBaseUrl,
            @Value("${db-service.traces.url}") String tracesUrl,
            @Value("${db-service.traces.endpoints.data-overview}") String dataOverview,
            @Value("${db-service.spans.url}") String spansUrl) {
        this.dataOverviewUrl = dbServiceBaseUrl + tracesUrl + dataOverview;
        this.spansUrl = dbServiceBaseUrl + spansUrl;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    @Bean
    public StreamReadConstraints streamReadConstraints() {
        return StreamReadConstraints.builder().maxStringLength(Integer.MAX_VALUE).build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        JsonFactoryBuilder builder = new JsonFactoryBuilder();
        builder.streamReadConstraints(streamReadConstraints());
        ObjectMapper objectMapper = new ObjectMapper(builder.build());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}
