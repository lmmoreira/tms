package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.AbstractDomainEvent;

import java.beans.ConstructorProperties;
import java.time.Instant;
import java.util.UUID;

public class CompanyUpdated extends AbstractDomainEvent {

    private final UUID companyId;
    private final String property;
    private final String oldValue;
    private final String newValue;

    @ConstructorProperties({"domainEventId", "companyId", "property", "oldValue", "newValue", "occurredOn"})
    public CompanyUpdated(final UUID domainEventId, final UUID companyId, final String property, final String oldValue, final String newValue, final Instant occurredOn) {
        super(domainEventId, companyId, occurredOn);
        this.companyId = companyId;
        this.property = property;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public CompanyUpdated(final UUID companyId, final String property, final String oldValue, final String newValue) {
        this(null, companyId, property, oldValue, newValue, null);
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public String getProperty() {
        return property;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

}

