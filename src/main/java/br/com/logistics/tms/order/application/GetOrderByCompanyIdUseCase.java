package br.com.logistics.tms.order.application;

import br.com.logistics.tms.company.infrastructure.spi.CompanySpi;
import br.com.logistics.tms.order.domain.Order;
import br.com.logistics.tms.order.infrastructure.spi.OrderSpi;
import java.time.Instant;
import java.util.Set;

public class GetOrderByCompanyIdUseCase {

    private CompanySpi companySpi;

    public GetOrderByCompanyIdUseCase(CompanySpi companySpi) {
        this.companySpi = companySpi;
    }

    public Output execute(final Input input) {

        companySpi.getCompanyById(input.companyId());

        final Set<Order> orders = Set.of(new Order(1L, false, "external_1", Instant.now(), Instant.now()),
            new Order(2L, false,"external_2", Instant.now(), Instant.now()));
        return new Output(orders);
    }

    public record Input(String companyId) {

    }

    public record Output(Set<Order> order) {

    }

}
