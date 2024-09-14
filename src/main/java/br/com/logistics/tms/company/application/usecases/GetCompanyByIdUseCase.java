package br.com.logistics.tms.company.application.usecases;

import br.com.logistics.tms.commons.application.usecases.UseCase;
import br.com.logistics.tms.company.application.repositories.CompanyRepository;
import br.com.logistics.tms.company.domain.Company;
import br.com.logistics.tms.company.domain.CompanyId;
import br.com.logistics.tms.order.infrastructure.spi.OrderSpi;

public class GetCompanyByIdUseCase extends
    UseCase<GetCompanyByIdUseCase.Input, GetCompanyByIdUseCase.Output> {

    private OrderSpi orderSpi;
    private CompanyRepository companyRepository;

    public GetCompanyByIdUseCase(OrderSpi orderSpi, CompanyRepository companyRepository) {
        this.orderSpi = orderSpi;
        this.companyRepository = companyRepository;
    }

    public Output execute(final Input input) {
        //final Company company = new Company(input.companyId(), "Shopee",
        //    orderSpi.getOrderByCompanyId(input.companyId()).size());

        return new Output(companyRepository.getCompanyById(CompanyId.with(input.companyId()))
            .orElseThrow(() -> new RuntimeException("Company not found")));
    }

    public record Input(String companyId) {

    }

    public record Output(Company company) {

    }

}
