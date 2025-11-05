package br.com.logistics.tms.shipmentorder.application.usecases;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.DomainService;
import br.com.logistics.tms.commons.application.annotation.DatabaseRole;
import br.com.logistics.tms.commons.application.usecases.UseCase;
import br.com.logistics.tms.commons.domain.pagination.Page;
import br.com.logistics.tms.commons.domain.pagination.PageRequest;
import br.com.logistics.tms.shipmentorder.application.repositories.ShipmentOrderRepository;
import br.com.logistics.tms.shipmentorder.domain.ShipmentOrder;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@DomainService
@Cqrs(DatabaseRole.READ)
public class GetShipmentOrderByCompanyIdUseCase implements UseCase<GetShipmentOrderByCompanyIdUseCase.Input, GetShipmentOrderByCompanyIdUseCase.Output> {

    private final ShipmentOrderRepository shipmentOrderRepository;

    public GetShipmentOrderByCompanyIdUseCase(final ShipmentOrderRepository shipmentOrderRepository) {
        this.shipmentOrderRepository = shipmentOrderRepository;
    }

    public Output execute(final Input input) {
        final Page<ShipmentOrder> shipmentOrders = shipmentOrderRepository.getShipmentOrderByCompanyId(input.companyId(), PageRequest.of(input.page, input.size));
        final Set<Output.ShipmentOrder> shipmentOrderOutputs = shipmentOrders.content().stream()
                .map(so -> new Output.ShipmentOrder(
                        so.getShipmentOrderId().value(),
                        so.getCompany(),
                        so.getShipper(),
                        so.getExternalId(),
                        so.getCreatedAt()))
                .collect(java.util.stream.Collectors.toSet());

        return new Output(shipmentOrderOutputs, shipmentOrders.page(), shipmentOrders.size(), shipmentOrders.totalElements(), shipmentOrders.totalPages());
    }

    public record Input(UUID companyId, int page, int size) {

    }

    public record Output(Set<ShipmentOrder> shipmentOrders,
                         int page,
                         int size,
                         long totalElements, long totalPages) {
        public record ShipmentOrder(UUID shipmentOrderId, UUID companyId, UUID shipperId, String externalId, Instant createdAt) {
        }
    }

}
