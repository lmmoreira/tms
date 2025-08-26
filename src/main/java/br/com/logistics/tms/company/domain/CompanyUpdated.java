package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.AbstractDomainEvent;

public class CompanyUpdated extends AbstractDomainEvent {

    private final String companyId;
    private final String property;
    private final String oldValue;
    private final String newValue;

    public CompanyUpdated(final String companyId, final String property, final String oldValue, final String newValue) {
        super(companyId);
        this.companyId = companyId;
        this.property = property;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getCompanyId() {
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

