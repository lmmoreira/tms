package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.AbstractDomainEvent;

import java.beans.ConstructorProperties;
import java.util.UUID;

public class CompanyUpdated extends AbstractDomainEvent {

    private final UUID companyId;
    private final String property;
    private final String oldValue;
    private final String newValue;

    @ConstructorProperties({"companyId", "property", "oldValue", "newValue"})
    public CompanyUpdated(final UUID companyId, final String property, final String oldValue, final String newValue) {
        super(companyId);
        this.companyId = companyId;
        this.property = property;
        this.oldValue = oldValue;
        this.newValue = newValue;
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

