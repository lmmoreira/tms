package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.AbstractDomainEvent;

import java.beans.ConstructorProperties;

public class CompanyCreated extends AbstractDomainEvent {

    private final String companyId;
    private final String company;

    @ConstructorProperties({"companyId", "company"})
    public CompanyCreated(final String companyId, final String company) {
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

