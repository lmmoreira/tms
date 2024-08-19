package br.com.logistics.tms.order.application;

import br.com.logistics.tms.order.domain.Order;
import java.time.Instant;
import java.util.Set;

public class GetOrderByCompanyIdUseCase {

    public Output execute(final Input input) {
        final Set<Order> orders = Set.of(new Order(1L, "external_1", Instant.now()),
            new Order(2L, "external_2", Instant.now()));
        return new Output(orders);
    }

    public record Input(Long companyId) {

    }

    public record Output(Set<Order> order) {

    }

}
