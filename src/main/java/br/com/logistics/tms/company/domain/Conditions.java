package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.exception.ValidationException;

import java.util.Map;

public record Conditions(Map<String, Object> value) {

    public Conditions {
        if (value == null || value.isEmpty()) {
            throw new ValidationException("Condition cannot be null or empty");
        }
    }

    public static Conditions with(final Map<String, Object> value) {
        return new Conditions(value);
    }

}