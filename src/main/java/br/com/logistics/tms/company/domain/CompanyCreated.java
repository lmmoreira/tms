package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.DomainEvent;

public record CompanyCreated (String companyId) implements DomainEvent {

}
