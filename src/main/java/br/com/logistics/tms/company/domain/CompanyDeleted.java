package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.AbstractDomainEvent;

public class CompanyDeleted extends AbstractDomainEvent {

    private final String companyId;
    private final String company;

    public CompanyDeleted(final String companyId, final String company) {
        super(companyId);
        this.companyId = companyId;
        this.company = company;
    }

    public String getCompanyId() {
        return companyId;
    }

    public String getCompany() {
        return company;
    }
}

