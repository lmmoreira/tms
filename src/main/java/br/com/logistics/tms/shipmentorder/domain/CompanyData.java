package br.com.logistics.tms.shipmentorder.domain;

import br.com.logistics.tms.commons.domain.exception.ValidationException;

import java.util.Collections;
import java.util.Map;

public record CompanyData(Map<String, Object> value) {

    public CompanyData {
        if (value == null || value.isEmpty()) {
            throw new ValidationException("Company data cannot be null or empty");
        }

        value = Collections.unmodifiableMap(value);
    }

    public static CompanyData with(final Map<String, Object> value) {
        return new CompanyData(value);
    }
}
