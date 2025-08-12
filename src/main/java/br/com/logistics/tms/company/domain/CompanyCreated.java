package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.AbstractDomainEvent;

public class CompanyCreated extends AbstractDomainEvent {

    private final String companyId;

    public CompanyCreated(final String companyId) {
        super();
        this.companyId = companyId;
    }

    public String getCompanyId() {
        return companyId;
    }
}

