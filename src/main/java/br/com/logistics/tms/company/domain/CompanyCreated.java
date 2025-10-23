package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.AbstractDomainEvent;

import java.beans.ConstructorProperties;
import java.util.UUID;

public class CompanyCreated extends AbstractDomainEvent {

    private final UUID companyId;
    private final String company;

    @ConstructorProperties({"companyId", "company"})
    public CompanyCreated(final UUID companyId, final String company) {
        super(companyId);
        this.companyId = companyId;
        this.company = company;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public String getCompany() {
        return company;
    }
}

