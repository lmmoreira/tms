package br.com.logistics.tms.commons.domain;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public abstract class AbstractAggregateRoot implements Serializable {
    private final Map<String, Object> persistentMetadata;
    private final Set<AbstractDomainEvent> domainEvents;

    protected AbstractAggregateRoot(final Set<AbstractDomainEvent> domainEvents, final Map<String, Object> persistentMetadata) {
        this.domainEvents = domainEvents;
        this.persistentMetadata = persistentMetadata;
    }

    public Map<String, Object> getPersistentMetadata() {
        return Collections.unmodifiableMap(persistentMetadata);
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

    public void addPersistentMetadata(String key, Object value) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Metadata key cannot be null or blank");
        }

        this.persistentMetadata.put(key, value);
    }

}