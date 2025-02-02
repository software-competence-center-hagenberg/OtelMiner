package at.scch.freiseisen.ma.db_initializer.conf;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(
        basePackages = {
                "at.scch.freiseisen.ma.db_initializer.init",
                "at.scch.freiseisen.ma.db_initializer.source_extraction",
                "at.scch.freiseisen.ma.db_service.service"
        })
@EnableJpaRepositories(
        basePackages = {"at.scch.freiseisen.ma.data_layer.repository"}
)

@EntityScan(basePackages = "at.scch.freiseisen.ma.data_layer.entity")
public class ComponentConfig {
}
