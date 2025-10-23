package br.com.logistics.tms.shipmentorder.infrastructure.outbox;

import br.com.logistics.tms.commons.infrastructure.gateways.outbox.OutboxGateway;
import br.com.logistics.tms.shipmentorder.infrastructure.config.OrderSchema;
import br.com.logistics.tms.shipmentorder.infrastructure.jpa.entities.OrderOutboxEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderOutboxScheduler {

    private final OutboxGateway outboxGateway;

    public OrderOutboxScheduler(OutboxGateway outboxGateway) {
        this.outboxGateway = outboxGateway;
    }

    @Scheduled(fixedDelay = 5000)
    public void runOutbox() {
        outboxGateway.process(OrderSchema.ORDER_SCHEMA, 1, OrderOutboxEntity.class);
    }

}