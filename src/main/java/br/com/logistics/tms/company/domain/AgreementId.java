package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.Id;
import br.com.logistics.tms.commons.domain.exception.ValidationException;

import java.util.UUID;

public record AgreementId(UUID value) {

    public AgreementId {
        if (value == null) {
            throw new ValidationException("Invalid value for AgreementId");
        }
    }

    public static AgreementId unique() {
        return new AgreementId(Id.unique());
    }

    public static AgreementId with(final String value) {
        return new AgreementId(Id.with(value));
    }

    public static AgreementId with(final UUID value) {
        return new AgreementId(value);
    }

}
