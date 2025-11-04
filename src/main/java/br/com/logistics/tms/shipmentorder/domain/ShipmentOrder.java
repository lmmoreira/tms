package br.com.logistics.tms.shipmentorder.domain;

import br.com.logistics.tms.commons.domain.AbstractAggregateRoot;
import br.com.logistics.tms.commons.domain.AbstractDomainEvent;
import br.com.logistics.tms.commons.domain.exception.ValidationException;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ShipmentOrder extends AbstractAggregateRoot {

    private final ShipmentOrderId shipmentOrderId;
    private final boolean archived;
    private final UUID company;
    private final String externalId;
    private final Instant createdAt;
    private final Instant updatedAt;

    public ShipmentOrder(ShipmentOrderId shipmentOrderId,
                         boolean archived,
                         UUID company,
                         String externalId,
                         Instant createdAt,
                         Instant updatedAt,
                         final Set<AbstractDomainEvent> domainEvents) {
        super(new HashSet<>(domainEvents), new HashMap<>());

        if (shipmentOrderId == null) throw new ValidationException("Invalid shipmentOrderId for ShipmentOrder");
        if (company == null) throw new ValidationException("Invalid companyId for ShipmentOrder");

        this.shipmentOrderId = shipmentOrderId;
        this.archived = archived;
        this.company = company;
        this.externalId = externalId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ShipmentOrder createShipmentOrder(final UUID company,
                                                    final String externalId) {
        final ShipmentOrder shipmentOrder = new ShipmentOrder(ShipmentOrderId.unique(),
                false,
                company,
                externalId,
                Instant.now(),
                Instant.now(),
                new HashSet<>());
        shipmentOrder.placeDomainEvent(new ShipmentOrderCreated(shipmentOrder.shipmentOrderId.value(), company, externalId));
        return shipmentOrder;
    }

    public ShipmentOrderId getShipmentOrderId() {
        return shipmentOrderId;
    }

    public UUID getCompany() {
        return company;
    }

    public String getExternalId() {
        return externalId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
