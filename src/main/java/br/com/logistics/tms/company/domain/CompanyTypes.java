package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.exception.ValidationException;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public record CompanyTypes(Set<CompanyType> value) {

    public CompanyTypes {
        if (value == null || value.isEmpty()) {
            throw new ValidationException("Types cannot be null or empty");
        }

        value = Collections.unmodifiableSet(value);
    }

    public static CompanyTypes with(final Set<CompanyType> types) {
        return new CompanyTypes(types);
    }

}