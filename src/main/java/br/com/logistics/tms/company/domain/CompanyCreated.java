package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.AbstractDomainEvent;

import java.beans.ConstructorProperties;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public class CompanyCreated extends AbstractDomainEvent {

    private final UUID companyId;
    private final String company;
    private final Set<String> types;

    @ConstructorProperties({"domainEventId", "companyId", "company", "types", "occurredOn"})
    public CompanyCreated(final UUID domainEventId, final UUID companyId, final String company, final Set<String> types, final Instant occurredOn) {
        super(domainEventId, companyId, occurredOn);
        this.companyId = companyId;
        this.company = company;
        this.types = types;
    }

    public CompanyCreated(final UUID companyId, final String company, final Set<String> types) {
        this(null, companyId, company, types, null);
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public String getCompany() {
        return company;
    }

    public Set<String> getTypes() {
        return types;
    }
}

