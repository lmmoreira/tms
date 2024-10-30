package br.com.logistics.tms.commons.domain;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public abstract class AbstractDomainEvent implements Serializable {

    final UUID domainEventId;
    final Instant occurredOn;

    protected AbstractDomainEvent() {
        this.domainEventId = Id.unique();
        this.occurredOn = Instant.now();
    }

    public String routingKey() {
        return "integration.".concat(this.getClass().getPackage().getName().split("\\.")[4])
            .concat(".").concat(this.getClass().getSimpleName());
    }

    public String router() {
        return "tms.events";
    }

    public String getDomainEventId() {
        return domainEventId.toString();
    }

    public String getOccurredOn() {
        return occurredOn.toString();
    }
}
