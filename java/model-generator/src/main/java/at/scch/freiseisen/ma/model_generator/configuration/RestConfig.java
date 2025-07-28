package at.scch.freiseisen.ma.model_generator.configuration;

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

    public final String tracesUrl;
    public final String dataOverviewUrl;
    public final String spansUrl;
    public final String sourceDetailsUrl;
    public final String declareUrl;
    public final String probDeclareUrl;
    public final String probDeclareToTraceUrl;
    public final String tracesSourceUrl;
    public final String canonizedSpanTreeUrl;

    public RestConfig(
            @Value("${db-service.base-url}") String dbServiceBaseUrl,
            @Value("${db-service.traces.url}") String tracesUrl,
            @Value("${db-service.traces.endpoints.data-overview}") String dataOverview,
            @Value("${db-service.spans.url}") String spansUrl,
            @Value("${db-service.spans.endpoints.sourceDetails}") String sourceDetails,
            @Value("${db-service.declare.url}") String declareUrl,
            @Value("${db-service.prob-declare.url}") String probDeclareUrl,
            @Value("${db-service.prob-declare-to-trace.url}") String probDeclareToTraceUrl,
            @Value("${db-service.traces.endpoints.source}") String tracesSourceUrl,
            @Value("${db-service.canonized-span-tree.url}") String canonizedSpanTreeUrl
    ) {
        this.tracesUrl = dbServiceBaseUrl + tracesUrl;
        this.dataOverviewUrl = dbServiceBaseUrl + tracesUrl + dataOverview;
        this.spansUrl = dbServiceBaseUrl + spansUrl;
        this.sourceDetailsUrl = dbServiceBaseUrl + tracesUrl + sourceDetails;
        this.declareUrl = dbServiceBaseUrl + declareUrl;
        this.probDeclareUrl = dbServiceBaseUrl + probDeclareUrl;
        this.probDeclareToTraceUrl = dbServiceBaseUrl + probDeclareToTraceUrl;
        this.tracesSourceUrl = dbServiceBaseUrl + tracesUrl + tracesSourceUrl;
        this.canonizedSpanTreeUrl = dbServiceBaseUrl + canonizedSpanTreeUrl;
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
