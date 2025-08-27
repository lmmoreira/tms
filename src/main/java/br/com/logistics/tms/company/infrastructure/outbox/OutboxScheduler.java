package br.com.logistics.tms.company.infrastructure.outbox;

import br.com.logistics.tms.commons.infrastructure.gateways.outbox.OutboxGateway;
import br.com.logistics.tms.company.infrastructure.config.CompanySchema;
import br.com.logistics.tms.company.infrastructure.jpa.entities.CompanyOutboxEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxScheduler {

    private final OutboxGateway outboxGateway;

    public OutboxScheduler(OutboxGateway outboxGateway) {
        this.outboxGateway = outboxGateway;
    }

    @Scheduled(fixedDelay = 5000)
    public void runCompanyOutbox() {
        outboxGateway.process(CompanySchema.COMPANY_SCHEMA, 1, CompanyOutboxEntity.class);
    }

}