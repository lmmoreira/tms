package br.com.logistics.tms.shipmentorder.application;

import br.com.logistics.tms.commons.application.annotation.DomainService;
import br.com.logistics.tms.commons.application.usecases.UseCase;
import br.com.logistics.tms.commons.domain.Id;
import br.com.logistics.tms.commons.infrastructure.gateways.outbox.OutboxGateway;
import br.com.logistics.tms.company.application.usecases.GetCompanyByIdUseCase;
import br.com.logistics.tms.shipmentorder.domain.Order;
import br.com.logistics.tms.shipmentorder.domain.OrderRetrieved;
import br.com.logistics.tms.shipmentorder.infrastructure.jpa.entities.OrderOutboxEntity;

import java.time.Instant;
import java.util.Set;

@DomainService
public class GetOrderByCompanyIdUseCase implements UseCase<GetOrderByCompanyIdUseCase.Input, GetOrderByCompanyIdUseCase.Output> {

    private final OutboxGateway outboxGateway;

    public GetOrderByCompanyIdUseCase(OutboxGateway outboxGateway) {
        this.outboxGateway = outboxGateway;
    }

    public Output execute(final Input input) {

        //companySpi.getCompanyById(input.companyId());

        final Set<Order> orders = Set.of(new Order(Id.unique(), false, "external_1", Instant.now(), Instant.now()),
            new Order(Id.unique(), false,"external_2", Instant.now(), Instant.now()));

        outboxGateway.save("order", Set.of(new OrderRetrieved(Id.unique(), "external_1")), OrderOutboxEntity.class);

        return new Output(orders);
    }

    public record Input(String companyId) {

    }

    public record Output(Set<Order> order) {

    }

}
