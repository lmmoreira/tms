package br.com.logistics.tms.shipmentorder.infrastructure.repositories;

import br.com.logistics.tms.commons.domain.pagination.Page;
import br.com.logistics.tms.commons.domain.pagination.PageRequest;
import br.com.logistics.tms.commons.infrastructure.gateways.outbox.OutboxGateway;
import br.com.logistics.tms.shipmentorder.application.repositories.ShipmentOrderRepository;
import br.com.logistics.tms.shipmentorder.domain.ShipmentOrder;
import br.com.logistics.tms.shipmentorder.infrastructure.config.ShipmentOrderSchema;
import br.com.logistics.tms.shipmentorder.infrastructure.jpa.entities.ShipmentOrderEntity;
import br.com.logistics.tms.shipmentorder.infrastructure.jpa.entities.ShipmentOrderOutboxEntity;
import br.com.logistics.tms.shipmentorder.infrastructure.jpa.repositories.ShipmentOrderJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
public class ShipmentOrderRepositoryImpl implements ShipmentOrderRepository {

    private final ShipmentOrderJpaRepository shipmentOrderJpaRepository;
    private final OutboxGateway outboxGateway;

    @Override
    public Optional<ShipmentOrder> getShipmentOrderByExternalId(String externalId) {
        return shipmentOrderJpaRepository.findByExternalId(externalId)
                .map(ShipmentOrderEntity::toShipmentOrder);
    }

    @Override
    public Page<ShipmentOrder> getShipmentOrderByCompanyId(UUID companyId, PageRequest pageRequest) {
        org.springframework.data.domain.Page<ShipmentOrderEntity> page = shipmentOrderJpaRepository.findByCompanyId(companyId, org.springframework.data.domain.PageRequest.of(pageRequest.page(), pageRequest.size()));
        return new Page<>(page
                .getContent()
                .stream()
                .map(ShipmentOrderEntity::toShipmentOrder).toList(), page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Override
    public ShipmentOrder create(ShipmentOrder shipmentOrder) {
        final ShipmentOrderEntity shipmentOrderEntity = ShipmentOrderEntity.of(shipmentOrder);
        shipmentOrderJpaRepository.save(shipmentOrderEntity);
        outboxGateway.save(ShipmentOrderSchema.SHIPMENT_ORDER_SCHEMA, shipmentOrder.getDomainEvents(), ShipmentOrderOutboxEntity.class);
        return shipmentOrder;
    }
}
