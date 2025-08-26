package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.exception.ValidationException;

public record AgreementCondition(
        AgreementConditionId agreementConditionId,
        AgreementConditionType conditionType,
        Conditions conditions
) {

    public AgreementCondition {

        if (agreementConditionId == null) {
            throw new ValidationException("Invalid agreementConditionId for AgreementCondition");
        }

        if (conditionType == null) {
            throw new ValidationException("Invalid conditionType for AgreementCondition");
        }

        if (conditions == null) {
            throw new ValidationException("Invalid conditions for AgreementCondition");
        }

    }

}