package br.com.logistics.tms.commons.domain;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public interface DomainEvent extends Serializable {

    default String type() {
        return getClass().getSimpleName();
    }

    default String module() {
        final String[] ar = this.getClass().getPackage().getName().split("\\.");
        return ar[ar.length - 2];
    }

    default String domainEventId() {
        return UUID.randomUUID().toString();
    }

    default Instant occurredOn() {
        return Instant.now();
    }
}