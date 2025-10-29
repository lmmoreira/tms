package br.com.logistics.tms.shipmentorder.application.usecases;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.DomainService;
import br.com.logistics.tms.commons.application.annotation.DatabaseRole;
import br.com.logistics.tms.commons.application.usecases.UseCase;
import br.com.logistics.tms.commons.domain.exception.ValidationException;
import br.com.logistics.tms.shipmentorder.application.repositories.ShipmentOrderRepository;
import br.com.logistics.tms.shipmentorder.domain.ShipmentOrder;

import java.time.Instant;
import java.util.UUID;

@DomainService
@Cqrs(DatabaseRole.WRITE)
public class CreateShipmentOrderUseCase implements UseCase<CreateShipmentOrderUseCase.Input, CreateShipmentOrderUseCase.Output> {

    private final ShipmentOrderRepository shipmentOrderRepository;

    public CreateShipmentOrderUseCase(ShipmentOrderRepository shipmentOrderRepository) {
        this.shipmentOrderRepository = shipmentOrderRepository;
    }

    public Output execute(final Input input) {
        if (shipmentOrderRepository.getShipmentOrderByExternalId(input.externalId).isPresent()) {
            throw new ValidationException("ShipmentOrder already exists");
        }

        final ShipmentOrder shipmentOrder = shipmentOrderRepository.create(ShipmentOrder.createShipmentOrder(input.companyId, input.externalId));

        return new Output(shipmentOrder.getShipmentOrderId().value(),
                shipmentOrder.getCompany(),
                shipmentOrder.getExternalId(),
                shipmentOrder.getCreatedAt());
    }

    public record Input(UUID companyId, String externalId) {
    }

    public record Output(UUID shipmentOrderId,
                         UUID companyId,
                         String externalId,
                         Instant createdAt) {
    }
}
