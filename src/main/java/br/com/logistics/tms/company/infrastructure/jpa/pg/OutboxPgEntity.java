package br.com.logistics.tms.company.infrastructure.jpa.pg;

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

import java.util.UUID;

@Entity
@Table(name = "outbox", schema = "company")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboxPgEntity {

    @Id
    private UUID id;

    @JdbcTypeCode(SqlTypes.JSON)
    private String content;

    @Column(nullable = false)
    private boolean published = false;

    @Column(nullable = false, length = 50)
    private String type;

    public static OutboxPgEntity of(final AbstractDomainEvent event) {
        return new OutboxPgEntity(UUID.fromString(event.getDomainEventId()), JsonSingleton.getInstance().toJson(event), false, event.getType());
    }

}