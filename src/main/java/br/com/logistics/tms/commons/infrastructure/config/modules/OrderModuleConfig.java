package br.com.logistics.tms.commons.infrastructure.config.modules;

import br.com.logistics.tms.commons.infrastructure.cqrs.CqrsTypeFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(
        basePackages = "br.com.logistics.tms.shipmentorder.infrastructure",
        excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = CqrsTypeFilter.class),
        useDefaultFilters = true
)
@ConditionalOnProperty(name = "modules.order.enabled", havingValue = "true")
public class OrderModuleConfig {
}
