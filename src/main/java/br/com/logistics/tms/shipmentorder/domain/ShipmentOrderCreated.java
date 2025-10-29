package br.com.logistics.tms.shipmentorder.domain;

import br.com.logistics.tms.commons.domain.AbstractDomainEvent;

import java.beans.ConstructorProperties;
import java.time.Instant;
import java.util.UUID;

public class ShipmentOrderCreated extends AbstractDomainEvent {

    private final UUID shipmentOrderId;
    private final UUID companyId;
    private final String externalId;

    @ConstructorProperties({"domainEventId", "shipmentOrderId", "companyId", "externalId", "occurredOn"})
    public ShipmentOrderCreated(final UUID domainEventId, final UUID shipmentOrderId, final UUID companyId, final String externalId, final Instant occurredOn) {
        super(domainEventId, shipmentOrderId, occurredOn);
        this.shipmentOrderId = shipmentOrderId;
        this.companyId = companyId;
        this.externalId = externalId;
    }

    public ShipmentOrderCreated(final UUID shipmentOrderId, final UUID companyId, final String externalId) {
        this(null, shipmentOrderId, companyId, externalId, null);
    }

    public UUID getShipmentOrderId() {
        return shipmentOrderId;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public String getExternalId() {
        return externalId;
    }

}

