package br.com.logistics.tms.company.infrastructure.config;

import br.com.logistics.tms.commons.application.gateways.DomainEventQueueGateway;
import br.com.logistics.tms.commons.application.mapper.Mapper;
import br.com.logistics.tms.company.application.repositories.CompanyRepository;
import br.com.logistics.tms.company.application.usecases.AddConfigurationToCompanyUseCase;
import br.com.logistics.tms.company.application.usecases.CreateCompanyUseCase;
import br.com.logistics.tms.company.application.usecases.GetCompanyByIdUseCase;
import br.com.logistics.tms.company.domain.CompanyCreated;
import br.com.logistics.tms.order.infrastructure.spi.OrderSpi;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class CompanyUseCaseConfig {

    private final DomainEventQueueGateway<CompanyCreated> companyCreatedRabbitMQDomainEventQueueGateway;
    private final CompanyRepository companyRepository;
    private final Mapper mapper;
    private final OrderSpi orderSpi;

    @Bean
    public AddConfigurationToCompanyUseCase addSellerToMarketplaceUseCase() {
        return new AddConfigurationToCompanyUseCase(null);
    }

    @Bean
    public GetCompanyByIdUseCase getMarketplaceByIdUseCase() {
        return new GetCompanyByIdUseCase(companyRepository);
    }

    @Bean
    public CreateCompanyUseCase createCompanyUseCase() {
        return new CreateCompanyUseCase(companyRepository, companyCreatedRabbitMQDomainEventQueueGateway, orderSpi);
    }

}
