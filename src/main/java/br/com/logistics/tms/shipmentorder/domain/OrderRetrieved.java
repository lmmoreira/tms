package br.com.logistics.tms.shipmentorder.domain;

import br.com.logistics.tms.commons.domain.AbstractDomainEvent;

import java.beans.ConstructorProperties;
import java.util.UUID;

public class OrderRetrieved extends AbstractDomainEvent {

    private final UUID orderId;
    private final String externalId;

    @ConstructorProperties({"orderId", "externalId"})
    public OrderRetrieved(final UUID orderId, final String externalId) {
        super(orderId);
        this.orderId = orderId;
        this.externalId = externalId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public String getExternalId() {
        return externalId;
    }

}

