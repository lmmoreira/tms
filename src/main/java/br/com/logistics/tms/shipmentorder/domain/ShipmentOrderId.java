package br.com.logistics.tms.shipmentorder.domain;

import br.com.logistics.tms.commons.domain.Id;
import br.com.logistics.tms.commons.domain.exception.ValidationException;

import java.util.UUID;

public record ShipmentOrderId(UUID value) {

    public ShipmentOrderId {
        if (value == null) {
            throw new ValidationException("Invalid value for ShipmentOrderId");
        }
    }

    public static ShipmentOrderId unique() {
        return new ShipmentOrderId(Id.unique());
    }

    public static ShipmentOrderId with(final String value) {
        return new ShipmentOrderId(Id.with(value));
    }

    public static ShipmentOrderId with(final UUID value) {
        return new ShipmentOrderId(value);
    }

}
