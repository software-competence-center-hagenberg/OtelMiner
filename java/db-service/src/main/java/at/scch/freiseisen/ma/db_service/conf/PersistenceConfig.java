package at.scch.freiseisen.ma.db_service.conf;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "at.scch.freiseisen.ma.data_layer.entity")
@EnableJpaRepositories(basePackages = "at.scch.freiseisen.ma.data_layer.repository")
public class PersistenceConfig {
}
