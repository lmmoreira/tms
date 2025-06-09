package br.com.logistics.tms.order.infrastructure.config;

import br.com.logistics.tms.commons.annotation.DomainService;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(
        basePackages = {"br.com.logistics.tms.order.application"},
        includeFilters = {@ComponentScan.Filter(type = FilterType.ANNOTATION, classes = {DomainService.class})})
public class OrderUseCaseConfig {

}
