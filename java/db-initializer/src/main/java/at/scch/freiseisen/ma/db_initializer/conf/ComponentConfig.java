package at.scch.freiseisen.ma.db_initializer.conf;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(
        basePackages = {
                "at.scch.freiseisen.ma.data_layer.service",
                "at.scch.freiseisen.ma.db_initializer.init",
                "at.scch.freiseisen.ma.db_initializer.source_extraction",
        })
public class ComponentConfig {
}
