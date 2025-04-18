package at.scch.freiseisen.ma.trace_collector.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class ModelGenerationConfig {
    @Value("${model_generation.nr_threads}")
    private int nrThreads;
    @Value("${model_generation.batch_size}")
    private int batchSize;

}
