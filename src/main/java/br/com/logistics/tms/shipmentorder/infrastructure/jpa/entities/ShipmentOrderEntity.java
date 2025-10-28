package br.com.logistics.tms.shipmentorder.infrastructure.jpa.entities;

import br.com.logistics.tms.commons.infrastructure.gateways.outbox.AbstractOutboxEntity;
import br.com.logistics.tms.shipmentorder.domain.ShipmentOrder;
import br.com.logistics.tms.shipmentorder.domain.ShipmentOrderId;
import br.com.logistics.tms.shipmentorder.infrastructure.config.ShipmentOrderSchema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

@Entity
@Table(name = "shipment_order", schema = ShipmentOrderSchema.SHIPMENT_ORDER_SCHEMA)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentOrderEntity implements Serializable {

    @Id
    private UUID id;

    @Column(name = "is_archived", nullable = false, columnDefinition = "boolean default false")
    private boolean isArchived;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "external_id", nullable = false, length = 255)
    private String externalId;

    @Column(name = "created_at", nullable = false, columnDefinition = "timestamp with time zone")
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamp with time zone")
    private Instant updatedAt;

    public static ShipmentOrderEntity of(final ShipmentOrder shipmentOrder) {
        return new ShipmentOrderEntity(
                shipmentOrder.getShipmentOrderId().value(),
                false,//shipmentOrder.isArchived(),
                shipmentOrder.getCompany(),
                shipmentOrder.getExternalId(),
                shipmentOrder.getCreatedAt(),
                shipmentOrder.getUpdatedAt());
    }

    public ShipmentOrder toShipmentOrder() {
        return new ShipmentOrder(
                ShipmentOrderId.with(this.id),
                this.isArchived,
                this.companyId,
                this.externalId,
                this.createdAt,
                this.updatedAt,
                Collections.emptySet()
        );
    }

}