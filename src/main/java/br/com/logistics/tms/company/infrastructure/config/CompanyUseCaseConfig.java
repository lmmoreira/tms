package br.com.logistics.tms.company.infrastructure.config;

import br.com.logistics.tms.company.application.usecases.AddConfigurationToCompanyUseCase;
import br.com.logistics.tms.company.application.usecases.GetCompanyByIdUseCase;
import br.com.logistics.tms.company.infrastructure.gateways.RabbitMQQueueGateway;
import br.com.logistics.tms.order.infrastructure.spi.OrderSpi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CompanyUseCaseConfig {

    private OrderSpi orderSpi;

    public CompanyUseCaseConfig(OrderSpi orderSpi) {
        this.orderSpi = orderSpi;
    }

    @Bean
    public AddConfigurationToCompanyUseCase addSellerToMarketplaceUseCase() {
        return new AddConfigurationToCompanyUseCase(new RabbitMQQueueGateway());
    }

    @Bean
    public GetCompanyByIdUseCase getMarketplaceByIdUseCase() {
        return new GetCompanyByIdUseCase(orderSpi);
    }

}
