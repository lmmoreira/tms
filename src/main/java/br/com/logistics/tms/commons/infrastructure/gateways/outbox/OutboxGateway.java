package br.com.logistics.tms.commons.infrastructure.gateways.outbox;

import br.com.logistics.tms.commons.domain.AbstractDomainEvent;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface OutboxGateway {

    void save(String schemaName, Set<AbstractDomainEvent> events, Class<? extends AbstractOutboxEntity> entityClass);

    void process(String schemaName, int batchSize, Class<? extends AbstractOutboxEntity> entityClass);

    void onSuccess(final Map<String, Object> metadata);

    void onFailure(final Map<String, Object> metadata);
}