package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.exception.ValidationException;
import java.util.UUID;

public record Cnpj(String value) {

    private static final String CNPJ = "^\\d{2}\\.\\d{3}\\.\\d{3}\\/\\d{4}\\-\\d{2}$";

    public Cnpj {
        if (value == null || !value.matches(CNPJ)) {
            throw new ValidationException("Invalid value for Cnpj");
        }
    }

    public static Cnpj with(final String value) {
        return new Cnpj(value);
    }

}
