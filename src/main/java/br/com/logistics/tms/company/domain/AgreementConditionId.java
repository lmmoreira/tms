package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.Id;
import br.com.logistics.tms.commons.domain.exception.ValidationException;

import java.util.UUID;

public record AgreementConditionId(UUID value) {

    public AgreementConditionId {
        if (value == null) {
            throw new ValidationException("Invalid value for AgreementId");
        }
    }

    public static AgreementConditionId unique() {
        return new AgreementConditionId(Id.unique());
    }

    public static AgreementConditionId with(final String value) {
        return new AgreementConditionId(Id.with(value));
    }

    public static AgreementConditionId with(final UUID value) {
        return new AgreementConditionId(value);
    }

}
