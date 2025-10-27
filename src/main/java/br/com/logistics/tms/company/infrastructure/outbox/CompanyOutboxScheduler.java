package br.com.logistics.tms.company.infrastructure.outbox;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.Role;
import br.com.logistics.tms.commons.infrastructure.gateways.outbox.OutboxGateway;
import br.com.logistics.tms.company.infrastructure.config.CompanySchema;
import br.com.logistics.tms.company.infrastructure.jpa.entities.CompanyOutboxEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Cqrs(Role.WRITE)
public class CompanyOutboxScheduler {

    private final OutboxGateway outboxGateway;

    public CompanyOutboxScheduler(OutboxGateway outboxGateway) {
        this.outboxGateway = outboxGateway;
    }

    @Scheduled(fixedDelay = 5000)
    public void runCompanyOutbox() {
        outboxGateway.process(CompanySchema.COMPANY_SCHEMA, 1, CompanyOutboxEntity.class);
    }

}