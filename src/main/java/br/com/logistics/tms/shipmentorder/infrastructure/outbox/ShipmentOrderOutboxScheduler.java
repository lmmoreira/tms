package br.com.logistics.tms.shipmentorder.infrastructure.outbox;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.DatabaseRole;
import br.com.logistics.tms.commons.infrastructure.gateways.outbox.OutboxGateway;
import br.com.logistics.tms.shipmentorder.infrastructure.config.ShipmentOrderSchema;
import br.com.logistics.tms.shipmentorder.infrastructure.jpa.entities.ShipmentOrderOutboxEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Cqrs(DatabaseRole.WRITE)
public class ShipmentOrderOutboxScheduler {

    private final OutboxGateway outboxGateway;

    public ShipmentOrderOutboxScheduler(OutboxGateway outboxGateway) {
        this.outboxGateway = outboxGateway;
    }

    @Scheduled(fixedDelay = 1000)
    public void runOutbox() {
        outboxGateway.process(ShipmentOrderSchema.SHIPMENT_ORDER_SCHEMA, 10, ShipmentOrderOutboxEntity.class);
    }

}