package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.AbstractDomainEvent;

import java.beans.ConstructorProperties;
import java.time.Instant;
import java.util.UUID;

public class CompanyCreated extends AbstractDomainEvent {

    private final UUID companyId;
    private final String company;

    @ConstructorProperties({"domainEventId", "companyId", "company", "occurredOn"})
    public CompanyCreated(final UUID domainEventId, final UUID companyId, final String company, final Instant occurredOn) {
        super(domainEventId, companyId, occurredOn);
        this.companyId = companyId;
        this.company = company;
    }

    public CompanyCreated(final UUID companyId, final String company) {
        this(null, companyId, company, null);
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public String getCompany() {
        return company;
    }
}

