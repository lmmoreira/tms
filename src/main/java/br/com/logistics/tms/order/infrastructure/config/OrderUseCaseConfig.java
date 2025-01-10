package br.com.logistics.tms.order.infrastructure.config;

import br.com.logistics.tms.order.application.GetOrderByCompanyIdUseCase;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class OrderUseCaseConfig {

    @Bean
    public GetOrderByCompanyIdUseCase getOrderByCompanyIdUseCase() {
        return new GetOrderByCompanyIdUseCase();
    }

}
