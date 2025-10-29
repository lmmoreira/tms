package br.com.logistics.tms.commons.infrastructure.config.modules;

import br.com.logistics.tms.commons.infrastructure.cqrs.CqrsExclusionSpringScanningFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "br.com.logistics.tms.company.infrastructure.jpa")
@ComponentScan(
        basePackages = "br.com.logistics.tms.company.infrastructure",
        excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = CqrsExclusionSpringScanningFilter.class)
)
@ConditionalOnProperty(name = "modules.company.enabled", havingValue = "true")
public class CompanyModuleConfig {
}
