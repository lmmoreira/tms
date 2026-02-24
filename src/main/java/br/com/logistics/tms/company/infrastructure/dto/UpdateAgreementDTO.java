package br.com.logistics.tms.company.infrastructure.dto;

import br.com.logistics.tms.company.application.usecases.UpdateAgreementUseCase;
import br.com.logistics.tms.company.domain.AgreementCondition;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UpdateAgreementDTO(
        Instant validTo,
        Set<AgreementCondition> conditions
) {
    public UpdateAgreementUseCase.Input toInput(final UUID agreementId) {
        return new UpdateAgreementUseCase.Input(
                agreementId,
                validTo,
                conditions
        );
    }
}
