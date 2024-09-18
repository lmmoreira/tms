package br.com.logistics.tms.company.infrastructure.config;

import br.com.logistics.tms.company.application.repositories.CompanyRepository;
import br.com.logistics.tms.company.application.usecases.AddConfigurationToCompanyUseCase;
import br.com.logistics.tms.company.application.usecases.CreateCompanyUseCase;
import br.com.logistics.tms.company.application.usecases.GetCompanyByIdUseCase;
import br.com.logistics.tms.company.infrastructure.gateways.RabbitMQQueueGateway;
import br.com.logistics.tms.order.infrastructure.spi.OrderSpi;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class CompanyUseCaseConfig {

    private final OrderSpi orderSpi;
    private final RabbitMQQueueGateway rabbitMQQueueGateway;
    private final CompanyRepository companyRepository;

    @Bean
    public AddConfigurationToCompanyUseCase addSellerToMarketplaceUseCase() {
        return new AddConfigurationToCompanyUseCase(rabbitMQQueueGateway);
    }

    @Bean
    public GetCompanyByIdUseCase getMarketplaceByIdUseCase() {
        return new GetCompanyByIdUseCase(orderSpi, companyRepository);
    }

    @Bean
    public CreateCompanyUseCase createCompanyUseCase() {
        return new CreateCompanyUseCase(companyRepository, rabbitMQQueueGateway);
    }

}
