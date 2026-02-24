package br.com.logistics.tms.company.infrastructure.dto;

import br.com.logistics.tms.company.application.usecases.CreateAgreementUseCase;
import br.com.logistics.tms.company.domain.AgreementCondition;
import br.com.logistics.tms.company.domain.AgreementType;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record CreateAgreementDTO(
        UUID destinationCompanyId,
        AgreementType type,
        Map<String, Object> configuration,
        Set<AgreementCondition> conditions,
        Instant validFrom,
        Instant validTo
) {
    public CreateAgreementUseCase.Input toInput(final UUID sourceCompanyId) {
        return new CreateAgreementUseCase.Input(
                sourceCompanyId,
                destinationCompanyId,
                type,
                configuration,
                conditions,
                validFrom,
                validTo
        );
    }
}
