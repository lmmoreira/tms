package br.com.logistics.tms.company.infrastructure.config;

import br.com.logistics.tms.commons.application.annotation.DomainService;
import br.com.logistics.tms.commons.infrastructure.cqrs.CqrsTypeScanFilter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(
        basePackages = {"br.com.logistics.tms.company.application"},
        includeFilters = {@ComponentScan.Filter(type = FilterType.ANNOTATION, classes = {DomainService.class})},
        excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = CqrsTypeScanFilter.class))
public class CompanyUseCaseConfig {

}
