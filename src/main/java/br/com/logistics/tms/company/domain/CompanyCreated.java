package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.AbstractDomainEvent;
import java.util.UUID;

public final class CompanyCreated extends AbstractDomainEvent {

    private final UUID companyId;

    public CompanyCreated(UUID companyId) {
        this.companyId = companyId;
    }

    public UUID getCompanyId() {
        return companyId;
    }

}
