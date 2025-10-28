package br.com.logistics.tms.commons.infrastructure.gateways.outbox;

import br.com.logistics.tms.commons.application.gateways.DomainEventQueueGateway;
import br.com.logistics.tms.commons.domain.AbstractDomainEvent;
import br.com.logistics.tms.commons.domain.DomainEventRegistry;
import br.com.logistics.tms.commons.infrastructure.database.transaction.Transactional;
import br.com.logistics.tms.commons.infrastructure.json.JsonSingleton;
import br.com.logistics.tms.commons.infrastructure.telemetry.Logable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class OutboxGatewayImpl implements OutboxGateway {

    private final EntityManager entityManager;
    private final DomainEventQueueGateway domainEventQueueGateway;
    private final Transactional transactional;
    private final Logable logable;

    public OutboxGatewayImpl(EntityManager entityManager,
                             Transactional transactional,
                             DomainEventQueueGateway domainEventQueueGateway,
                             Logable logable) {
        this.entityManager = entityManager;
        this.domainEventQueueGateway = domainEventQueueGateway;
        this.transactional = transactional;
        this.logable = logable;
    }

    @Override
    public void save(String schemaName, Set<AbstractDomainEvent> events, Class<? extends AbstractOutboxEntity> entityClass) {
        if (events.isEmpty()) return;

        setSchema(schemaName);

        AbstractOutboxEntity.of(events, entityClass)
                .stream()
                .map(entityClass::cast)
                .forEach(entityManager::persist);
    }

    @Override
    public void process(String schemaName, int batchSize, Class<? extends AbstractOutboxEntity> entityClass) {
        this.logable.info(getClass(), "Processing outbox batch of {} messages from schema '{}'", batchSize, schemaName);

        setSchema(schemaName);

        final String sql = """
                WITH cte AS (
                    SELECT id
                    FROM outbox
                    WHERE status = 'NEW'
                    ORDER BY created_at
                    FOR UPDATE SKIP LOCKED
                    LIMIT ?
                )
                UPDATE outbox o
                SET status = 'PROCESSING'
                FROM cte
                WHERE o.id = cte.id
                RETURNING o.id, o.content, o.aggregate_id, o.created_at, o.type, o.status
                """;

        final Query query = entityManager.createNativeQuery(sql, entityClass);
        query.setParameter(1, batchSize);

        final List<AbstractOutboxEntity> result = query.getResultList();
        logable.info(getClass(), "Fetched {} outbox messages for processing", result.size());

        for (AbstractOutboxEntity outbox : result) {
            try {
                final Class<?> eventClass = DomainEventRegistry.getClass(schemaName, outbox.getType());

                final AbstractDomainEvent event = (AbstractDomainEvent) JsonSingleton.getInstance().fromJson(outbox.getContent(), eventClass);
                UUID correlationId = outbox.getId();
                domainEventQueueGateway.publish(
                        event,
                        correlationId,
                        this::onSuccess,
                        this::onFailure
                );
            } catch (Exception e) {
                logable.error(getClass(), "Failed to process outbox message with ID {}: {}", outbox.getId(), e.getMessage());
            }
        }
    }

    @Override
    public void onSuccess(final Map<String, Object> metadata) {
        transactional.runWithinTransaction(() -> {
            String schemaName = (String) metadata.get("module");
            UUID correlationId = (UUID) metadata.get("correlationId");

            setSchema(schemaName);

            String sql = "UPDATE outbox SET status = 'PUBLISHED' WHERE id = ?";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, correlationId);
            int updated = query.executeUpdate();

            logable.info(getClass(), "Updated outbox status to PUBLISHED for ID {}. Rows affected: {}", correlationId, updated);
        });

    }

    @Override
    public void onFailure(final Map<String, Object> metadata) {
        transactional.runWithinTransaction(() -> {
            String schemaName = (String) metadata.get("module");
            UUID correlationId = (UUID) metadata.get("correlationId");

            setSchema(schemaName);

            String sql = "UPDATE outbox SET status = 'FAILED' WHERE id = ?";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, correlationId);
            int updated = query.executeUpdate();

            logable.info(getClass(), "Updated outbox status to FAILED for ID {}. Rows affected: {}", correlationId, updated);
        });
    }

    private void setSchema(String schemaName) {
        final Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("SET search_path TO \"" + schemaName + "\"");
            }
        });
    }

}