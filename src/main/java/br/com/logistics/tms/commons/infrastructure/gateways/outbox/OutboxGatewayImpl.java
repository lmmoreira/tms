package br.com.logistics.tms.commons.infrastructure.gateways.outbox;

import br.com.logistics.tms.commons.application.gateways.DomainEventQueueGateway;
import br.com.logistics.tms.commons.domain.AbstractDomainEvent;
import br.com.logistics.tms.commons.infrastructure.jpa.transaction.Transactional;
import br.com.logistics.tms.commons.infrastructure.json.JsonSingleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class OutboxGatewayImpl implements OutboxGateway {

    private static final Logger log = LoggerFactory.getLogger(OutboxGatewayImpl.class);

    private final EntityManager entityManager;
    private final DomainEventQueueGateway domainEventQueueGateway;
    private final Transactional transactional;

    public OutboxGatewayImpl(EntityManager entityManager,
                             Transactional transactional,
                             DomainEventQueueGateway domainEventQueueGateway) {
        this.entityManager = entityManager;
        this.domainEventQueueGateway = domainEventQueueGateway;
        this.transactional = transactional;
    }

    @Override
    public void save(String schemaName, Set<AbstractDomainEvent> events, Class<? extends AbstractOutboxEntity> entityClass) {
        if (events.isEmpty()) return;

        Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("SET search_path TO " + schemaName);
            }
        });


        AbstractOutboxEntity.of(events, entityClass)
                .stream()
                .map(entityClass::cast)
                .forEach(entityManager::persist);
    }

    @Override
    public void process(String schemaName, int batchSize, Class<? extends AbstractOutboxEntity> entityClass) {

        Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("SET search_path TO " + schemaName);
            }
        });

        log.info("Processing outbox batch of {} messages from schema '{}'", batchSize, schemaName);

        String sql = """
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

        Query query = entityManager.createNativeQuery(sql, entityClass);
        query.setParameter(1, batchSize);

        List<AbstractOutboxEntity> result = query.getResultList();
        log.info("Fetched {} outbox messages for processing", result.size());

        for (AbstractOutboxEntity outbox : result) {

            try {
                final String className = "br.com.logistics.tms.company.domain." + outbox.getType();
                final Class<?> eventClass = Class.forName(className);

                final AbstractDomainEvent event = (AbstractDomainEvent) JsonSingleton.getInstance().fromJson(outbox.getContent(), eventClass);
                UUID correlationId = outbox.getId();
                domainEventQueueGateway.publish(
                        event,
                        correlationId,
                        this::onSuccess,
                        this::onFailure
                );
            } catch (Exception e) {
                log.error("Failed to process outbox message with ID {}: {}", outbox.getId(), e.getMessage());
            }

        }

    }

    @Override
    public void onSuccess(final Map<String, Object> metadata) {

        transactional.runWithinTransactionAndReturn(() -> {
            String schemaName = (String) metadata.get("module");
            UUID correlationId = (UUID) metadata.get("correlationId");

            Session session = entityManager.unwrap(Session.class);
            session.doWork(connection -> {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("SET search_path TO " + schemaName);
                }
            });

            String sql = "UPDATE outbox SET status = 'PUBLISHED' WHERE id = ?";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, correlationId);
            int updated = query.executeUpdate();

            log.info("Updated outbox status to PUBLISHED for ID {}. Rows affected: {}", correlationId, updated);
            return updated;
        });

    }

    @Override
    public void onFailure(final Map<String, Object> metadata) {
        String schemaName = (String) metadata.get("module");
        UUID correlationId = (UUID) metadata.get("correlationId");

        Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("SET search_path TO " + schemaName);
            }
        });

        String sql = "UPDATE outbox SET status = 'FAILED' WHERE id = ?";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, correlationId);
        int updated = query.executeUpdate();

        log.info("Updated outbox status to FAILED for ID {}. Rows affected: {}", correlationId, updated);
    }

}