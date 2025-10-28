package br.com.logistics.tms.shipmentorder.infrastructure.jpa.entities;

import br.com.logistics.tms.commons.infrastructure.gateways.outbox.AbstractOutboxEntity;
import br.com.logistics.tms.shipmentorder.infrastructure.config.ShipmentOrderSchema;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "outbox", schema = ShipmentOrderSchema.SHIPMENT_ORDER_SCHEMA)
public class ShipmentOrderOutboxEntity extends AbstractOutboxEntity {

}