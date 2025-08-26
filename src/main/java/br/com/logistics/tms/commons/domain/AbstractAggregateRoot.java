package br.com.logistics.tms.commons.domain;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractAggregateRoot implements Serializable {
    private final Set<AbstractDomainEvent> domainEvents;

    protected AbstractAggregateRoot(final Set<AbstractDomainEvent> domainEvents) {
        this.domainEvents = domainEvents;
    }

    public Set<AbstractDomainEvent> getDomainEvents() {
        return Collections.unmodifiableSet(domainEvents);
    }

    public void placeDomainEvent(AbstractDomainEvent domainEvent) {
        if (domainEvent == null) {
            throw new IllegalArgumentException("Domain event cannot be null");
        }

        this.domainEvents.add(domainEvent);
    }

}