package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.exception.ValidationException;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public record Agreement(AgreementId agreementId,
                        CompanyId from,
                        CompanyId to,
                        AgreementType type,
                        Configurations configurations,
                        Set<AgreementCondition> conditions,
                        Instant validFrom,
                        Instant validTo) {

    public Agreement {

        if (agreementId == null) {
            throw new ValidationException("Invalid agreementId for Agreement");
        }

        if (from == null) {
            throw new ValidationException("Invalid from for Agreement");
        }

        if (to == null) {
            throw new ValidationException("Invalid to for Agreement");
        }

        if (type == null) {
            throw new ValidationException("Invalid type for Agreement");
        }

        if (configurations == null) {
            throw new ValidationException("Invalid configuration for Agreement");
        }

        if (validFrom == null) {
            throw new ValidationException("Invalid validFrom for Agreement");
        }

        if (validTo != null && validFrom.isAfter(validTo)) {
            throw new ValidationException("validFrom must be before validTo in Agreement");
        }

        conditions = Collections.unmodifiableSet(
                conditions == null ? Set.of() : new HashSet<>(conditions)
        );

    }

    public boolean isActive() {
        final Instant now = Instant.now();
        return !now.isBefore(validFrom) && (validTo == null || !now.isAfter(validTo));
    }

    public boolean isValidOn(final Instant date) {
        return !date.isBefore(validFrom) && (validTo == null || !date.isAfter(validTo));

    }

    public Agreement addCondition(final AgreementCondition condition) {
        if (conditions.contains(condition)) {
            throw new ValidationException("Condition already exists in Agreement");
        }

        final Set<AgreementCondition> updatedConditions = new HashSet<>(conditions);
        updatedConditions.add(condition);
        return new Agreement(agreementId, from, to, type, configurations, updatedConditions, validFrom, validTo);
    }

    public Agreement removeCondition(final AgreementCondition condition) {
        if (!conditions.contains(condition)) {
            throw new ValidationException("Condition not found in Agreement");
        }

        final Set<AgreementCondition> updatedConditions = new java.util.HashSet<>(conditions);
        updatedConditions.remove(condition);
        return new Agreement(agreementId, from, to, type, configurations, updatedConditions, validFrom, validTo);
    }

    public boolean isBetween(CompanyId from, CompanyId to) {
        return this.from.equals(from) && this.to.equals(to);
    }

}