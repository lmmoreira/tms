package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.AbstractDomainEvent;
import br.com.logistics.tms.commons.domain.Id;

import java.time.Instant;
import java.util.UUID;

public class AgreementUpdated extends AbstractDomainEvent {
    private final UUID sourceCompanyId;
    private final UUID agreementId;
    private final String fieldChanged;
    private final String oldValue;
    private final String newValue;

    public AgreementUpdated(final UUID sourceCompanyId,
                           final UUID agreementId,
                           final String fieldChanged,
                           final String oldValue,
                           final String newValue) {
        super(Id.unique(), sourceCompanyId, Instant.now());
        this.sourceCompanyId = sourceCompanyId;
        this.agreementId = agreementId;
        this.fieldChanged = fieldChanged;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public UUID getSourceCompanyId() {
        return sourceCompanyId;
    }

    public UUID getAgreementId() {
        return agreementId;
    }

    public String getFieldChanged() {
        return fieldChanged;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }
}
