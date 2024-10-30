package br.com.logistics.tms.order.infrastructure.config;

import br.com.logistics.tms.company.infrastructure.spi.CompanySpi;
import br.com.logistics.tms.order.application.GetOrderByCompanyIdUseCase;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class OrderUseCaseConfig {

    private final CompanySpi companySpi;

    @Bean
    public GetOrderByCompanyIdUseCase getOrderByCompanyIdUseCase() {
        return new GetOrderByCompanyIdUseCase(companySpi);
    }

}
