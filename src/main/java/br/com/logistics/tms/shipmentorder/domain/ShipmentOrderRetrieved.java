package br.com.logistics.tms.shipmentorder.domain;

import br.com.logistics.tms.commons.domain.AbstractDomainEvent;

import java.beans.ConstructorProperties;
import java.time.Instant;
import java.util.UUID;

public class ShipmentOrderRetrieved extends AbstractDomainEvent {

    private final UUID orderId;
    private final String externalId;

    @ConstructorProperties({"domainEventId", "orderId", "externalId", "occurredOn"})
    public ShipmentOrderRetrieved(final UUID domainEventId, final UUID orderId, final String externalId, final Instant occurredOn) {
        super(domainEventId, orderId, occurredOn);
        this.orderId = orderId;
        this.externalId = externalId;
    }

    public ShipmentOrderRetrieved(final UUID orderId, final String externalId) {
        this(null, orderId, externalId, null);
    }

    public UUID getOrderId() {
        return orderId;
    }

    public String getExternalId() {
        return externalId;
    }

}

