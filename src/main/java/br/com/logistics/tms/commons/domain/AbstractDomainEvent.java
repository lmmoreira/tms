package br.com.logistics.tms.commons.domain;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public abstract class AbstractDomainEvent implements Serializable {
    private final String type;
    private final String module;
    private final String domainEventId;
    private final Instant occurredOn;

    protected AbstractDomainEvent() {
        final String[] ar = getClass().getPackage().getName().split("\\.");
        this.module = ar.length >= 2 ? ar[ar.length - 2] : "";

        this.type = getClass().getSimpleName();
        this.domainEventId = UUID.randomUUID().toString();
        this.occurredOn = Instant.now();
    }

    public String getType() {
        return type;
    }

    public String getModule() {
        return module;
    }

    public String getDomainEventId() {
        return domainEventId;
    }

    public Instant getOccurredOn() {
        return occurredOn;
    }
}