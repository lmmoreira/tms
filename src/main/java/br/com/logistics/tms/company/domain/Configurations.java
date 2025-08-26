package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.exception.ValidationException;

import java.util.Collections;
import java.util.Map;

public record Configurations(Map<String, Object> value) {

    public Configurations {
        if (value == null || value.isEmpty()) {
            throw new ValidationException("Configuration cannot be null or empty");
        }

        value = Collections.unmodifiableMap(value);
    }

    public static Configurations with(final Map<String, Object> value) {
        return new Configurations(value);
    }

}