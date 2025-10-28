package br.com.logistics.tms.shipmentorder.domain;

import br.com.logistics.tms.commons.domain.AbstractDomainEvent;

import java.beans.ConstructorProperties;
import java.util.UUID;

public class ShipmentOrderCreated extends AbstractDomainEvent {

    private final UUID shipmentOrderId;
    private final UUID companyId;
    private final String externalId;

    @ConstructorProperties({"shipmentOrderId", "externalId"})
    public ShipmentOrderCreated(final UUID shipmentOrderId, final UUID companyId, final String externalId) {
        super(shipmentOrderId);
        this.shipmentOrderId = shipmentOrderId;
        this.companyId = companyId;
        this.externalId = externalId;
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

