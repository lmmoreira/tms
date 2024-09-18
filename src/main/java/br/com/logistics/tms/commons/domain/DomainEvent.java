package br.com.logistics.tms.commons.domain;

import java.time.Instant;

public interface DomainEvent {

    String domainEventId();

    Instant occurredOn();

    String router();

    String routingKey();

}
