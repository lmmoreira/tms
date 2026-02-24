package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.AbstractDomainEvent;
import br.com.logistics.tms.commons.domain.Id;

import java.time.Instant;
import java.util.UUID;

public class AgreementRemoved extends AbstractDomainEvent {
    private final UUID sourceCompanyId;
    private final UUID agreementId;
    private final UUID destinationCompanyId;

    public AgreementRemoved(final UUID sourceCompanyId,
                           final UUID agreementId,
                           final UUID destinationCompanyId) {
        super(Id.unique(), sourceCompanyId, Instant.now());
        this.sourceCompanyId = sourceCompanyId;
        this.agreementId = agreementId;
        this.destinationCompanyId = destinationCompanyId;
    }

    public UUID getSourceCompanyId() {
        return sourceCompanyId;
    }

    public UUID getAgreementId() {
        return agreementId;
    }

    public UUID getDestinationCompanyId() {
        return destinationCompanyId;
    }
}
