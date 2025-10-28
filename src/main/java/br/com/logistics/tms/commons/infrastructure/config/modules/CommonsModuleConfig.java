package br.com.logistics.tms.commons.infrastructure.config.modules;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan("br.com.logistics.tms.commons.infrastructure")
@EnableJpaRepositories(basePackages = "br.com.logistics.tms.commons.infrastructure.jpa")
@ConditionalOnProperty(name = "modules.commons.enabled", havingValue = "true")
public class CommonsModuleConfig {
}
