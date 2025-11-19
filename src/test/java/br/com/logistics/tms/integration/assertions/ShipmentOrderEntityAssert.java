package br.com.logistics.tms.integration.assertions;

import br.com.logistics.tms.shipmentorder.infrastructure.jpa.entities.ShipmentOrderEntity;
import org.assertj.core.api.AbstractAssert;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ShipmentOrderEntityAssert extends AbstractAssert<ShipmentOrderEntityAssert, ShipmentOrderEntity> {

    private ShipmentOrderEntityAssert(final ShipmentOrderEntity actual) {
        super(actual, ShipmentOrderEntityAssert.class);
    }

    public static ShipmentOrderEntityAssert assertThatShipmentOrder(final ShipmentOrderEntity actual) {
        return new ShipmentOrderEntityAssert(actual);
    }

    public ShipmentOrderEntityAssert hasCompanyId(final UUID companyId) {
        isNotNull();
        assertThat(actual.getCompanyId())
                .as("ShipmentOrder companyId")
                .isEqualTo(companyId);
        return this;
    }

    public ShipmentOrderEntityAssert hasShipperId(final UUID shipperId) {
        isNotNull();
        assertThat(actual.getShipper())
                .as("ShipmentOrder shipperId")
                .isEqualTo(shipperId);
        return this;
    }

    public ShipmentOrderEntityAssert hasExternalId(final String externalId) {
        isNotNull();
        assertThat(actual.getExternalId())
                .as("ShipmentOrder externalId")
                .isEqualTo(externalId);
        return this;
    }

    public ShipmentOrderEntityAssert isNotArchived() {
        isNotNull();
        assertThat(actual.isArchived())
                .as("ShipmentOrder archived status")
                .isFalse();
        return this;
    }

    public ShipmentOrderEntityAssert isArchived() {
        isNotNull();
        assertThat(actual.isArchived())
                .as("ShipmentOrder archived status")
                .isTrue();
        return this;
    }

    public ShipmentOrderEntityAssert wasCreatedAt(final Instant createdAt) {
        isNotNull();
        assertThat(actual.getCreatedAt())
                .as("ShipmentOrder createdAt")
                .isEqualTo(createdAt);
        return this;
    }

    public ShipmentOrderEntityAssert wasCreatedAfter(final Instant instant) {
        isNotNull();
        assertThat(actual.getCreatedAt())
                .as("ShipmentOrder createdAt")
                .isAfter(instant);
        return this;
    }

    public ShipmentOrderEntityAssert wasCreatedBefore(final Instant instant) {
        isNotNull();
        assertThat(actual.getCreatedAt())
                .as("ShipmentOrder createdAt")
                .isBefore(instant);
        return this;
    }
}
