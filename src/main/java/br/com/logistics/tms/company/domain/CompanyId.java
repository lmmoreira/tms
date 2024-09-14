package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.exception.ValidationException;
import br.com.logistics.tms.commons.domain.Id;
import java.util.UUID;

public record CompanyId(UUID value) {

    public CompanyId {
        if (value == null) {
            throw new ValidationException("Invalid value for CompanyId");
        }
    }

    public static CompanyId unique() {
        return new CompanyId(Id.unique());
    }

    public static CompanyId with(final String value) {
        return new CompanyId(Id.with(value));
    }

}
