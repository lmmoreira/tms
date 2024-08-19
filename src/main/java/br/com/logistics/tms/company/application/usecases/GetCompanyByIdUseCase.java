package br.com.logistics.tms.company.application.usecases;

import br.com.logistics.tms.commons.application.usecases.UseCase;
import br.com.logistics.tms.company.domain.Company;
import br.com.logistics.tms.order.infrastructure.spi.OrderSpi;

public class GetCompanyByIdUseCase extends
    UseCase<GetCompanyByIdUseCase.Input, GetCompanyByIdUseCase.Output> {

    private OrderSpi orderSpi;

    public GetCompanyByIdUseCase(OrderSpi orderSpi) {
        this.orderSpi = orderSpi;
    }

    public Output execute(final Input input) {
        final Company company = new Company(input.companyId(), "Shopee",
            orderSpi.getOrderByCompanyId(input.companyId()).size());
        return new Output(company);
    }

    public record Input(Long companyId) {

    }

    public record Output(Company company) {

    }

}
