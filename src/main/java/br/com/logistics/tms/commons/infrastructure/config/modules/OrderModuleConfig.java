package br.com.logistics.tms.commons.infrastructure.config.modules;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("br.com.logistics.tms.shipmentorder.infrastructure")
@ConditionalOnProperty(name = "modules.order.enabled", havingValue = "true")
public class OrderModuleConfig {
}
