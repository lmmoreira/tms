package br.com.logistics.tms.shipmentorder.application.usecases;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.DatabaseRole;
import br.com.logistics.tms.commons.application.annotation.DomainService;
import br.com.logistics.tms.commons.application.usecases.UseCase;
import br.com.logistics.tms.commons.domain.exception.ValidationException;
import br.com.logistics.tms.shipmentorder.application.repositories.CompanyRepository;
import br.com.logistics.tms.shipmentorder.application.repositories.ShipmentOrderRepository;
import br.com.logistics.tms.shipmentorder.domain.Company;
import br.com.logistics.tms.shipmentorder.domain.CompanyId;
import br.com.logistics.tms.shipmentorder.domain.ShipmentOrder;

import java.time.Instant;
import java.util.UUID;

@DomainService
@Cqrs(DatabaseRole.WRITE)
public class CreateShipmentOrderUseCase implements UseCase<CreateShipmentOrderUseCase.Input, CreateShipmentOrderUseCase.Output> {

    private final ShipmentOrderRepository shipmentOrderRepository;
    private final CompanyRepository companyRepository;

    public CreateShipmentOrderUseCase(final ShipmentOrderRepository shipmentOrderRepository,
                                      final CompanyRepository companyRepository) {
        this.shipmentOrderRepository = shipmentOrderRepository;
        this.companyRepository = companyRepository;
    }

    public Output execute(final Input input) {
        if (shipmentOrderRepository.getShipmentOrderByExternalId(input.externalId).isPresent()) {
            throw new ValidationException("ShipmentOrder already exists");
        }

        final Company company = companyRepository.findById(CompanyId.with(input.companyId))
                .orElseThrow(() -> new ValidationException("Company not found: " + input.companyId));

        if (company.getStatus().isInactive()) {
            throw new ValidationException("Cannot create shipment order for an inactive company: " + input.companyId);
        }

        if (input.shipperId != null && !companyRepository.existsById(CompanyId.with(input.shipperId))) {
            throw new ValidationException("Shipper not found: " + input.shipperId);
        }

        final Company shipper = input.shipperId != null ? companyRepository.findById(CompanyId.with(input.shipperId))
                .orElseThrow(() -> new ValidationException("Shipper not found: " + input.shipperId)) : null;

        if (shipper != null && !shipper.isLogisticsProvider()) {
            throw new ValidationException("Shipper must be a logistics provider: " + input.shipperId);
        }

        final ShipmentOrder shipmentOrder = shipmentOrderRepository.create(ShipmentOrder.createShipmentOrder(input.companyId, input.shipperId, input.externalId));

        return new Output(shipmentOrder.getShipmentOrderId().value(),
                shipmentOrder.getCompany(),
                shipmentOrder.getShipper(),
                shipmentOrder.getExternalId(),
                shipmentOrder.getCreatedAt());
    }

    public record Input(UUID companyId, UUID shipperId, String externalId) {
    }

    public record Output(UUID shipmentOrderId,
                         UUID companyId,
                         UUID shipperId,
                         String externalId,
                         Instant createdAt) {
    }
}
