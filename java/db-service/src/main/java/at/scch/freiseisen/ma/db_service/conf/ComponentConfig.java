package at.scch.freiseisen.ma.db_service.conf;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(
        basePackages = {
                "at.scch.freiseisen.ma.db_service.controller",
                "at.scch.freiseisen.ma.db_service.service"
        })
public class ComponentConfig {
}
