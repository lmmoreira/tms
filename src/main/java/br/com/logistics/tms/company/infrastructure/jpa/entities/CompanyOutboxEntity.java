package br.com.logistics.tms.company.infrastructure.jpa.entities;

import br.com.logistics.tms.commons.domain.AbstractDomainEvent;
import br.com.logistics.tms.commons.infrastructure.json.JsonSingleton;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "outbox", schema = "company")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyOutboxEntity {

    @Id
    private UUID id;

    @JdbcTypeCode(SqlTypes.JSON)
    private String content;

    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @Column(nullable = false)
    private boolean published = false;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false, length = 50)
    private String type;

    public static CompanyOutboxEntity of(final AbstractDomainEvent event) {
        return new CompanyOutboxEntity(
                UUID.fromString(event.getDomainEventId()),
                JsonSingleton.getInstance().toJson(event),
                event.getAggregateId(),
                false,
                event.getOccurredOn().atOffset(java.time.ZoneOffset.UTC),
                event.getType()
        );
    }

    public static List<CompanyOutboxEntity> of(final Set<AbstractDomainEvent> events) {
        return events.stream().map(CompanyOutboxEntity::of).toList();
    }

}