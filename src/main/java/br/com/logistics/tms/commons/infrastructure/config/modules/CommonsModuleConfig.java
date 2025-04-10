package br.com.logistics.tms.commons.infrastructure.config.modules;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("br.com.logistics.tms.commons.infrastructure")
@ConditionalOnProperty(name = "modules.commons.enabled", havingValue = "true")
public class CommonsModuleConfig {
}
