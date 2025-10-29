package br.com.logistics.tms.commons.infrastructure.gateways.outbox;

import br.com.logistics.tms.commons.domain.AbstractDomainEvent;
import br.com.logistics.tms.commons.infrastructure.json.JsonSingleton;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@MappedSuperclass
@Data
public abstract class AbstractOutboxEntity {

    @Id
    private UUID id;

    @JdbcTypeCode(SqlTypes.JSON)
    private String content;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false, length = 50)
    private String type;

    public static <T extends AbstractOutboxEntity> T of(AbstractDomainEvent event, Class<T> entityClass) {
        try {
            T entity = entityClass.getDeclaredConstructor().newInstance();
            entity.setId(event.getDomainEventId());
            entity.setContent(JsonSingleton.getInstance().toJson(event));
            entity.setAggregateId(event.getAggregateId());
            entity.setStatus(OutboxStatus.NEW);
            entity.setCreatedAt(event.getOccurredOn().atOffset(ZoneOffset.UTC));
            entity.setType(event.getType());
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create outbox entity", e);
        }
    }

    public static <T extends AbstractOutboxEntity> List<T> of(Set<AbstractDomainEvent> events, Class<T> entityClass) {
        return events.stream()
                .map(event -> of(event, entityClass))
                .toList();
    }

}