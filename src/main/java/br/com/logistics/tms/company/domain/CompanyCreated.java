package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.DomainEvent;
import br.com.logistics.tms.commons.domain.Id;
import java.time.Instant;

public record CompanyCreated(String domainEventId,
                             Instant occurredOn,
                             String router,
                             String routingKey,
                             String companyId) implements DomainEvent {

    public CompanyCreated(String companyId) {
        this(Id.unique().toString(), Instant.now(), "company.events", "CompanyCreated", companyId);
    }
}
