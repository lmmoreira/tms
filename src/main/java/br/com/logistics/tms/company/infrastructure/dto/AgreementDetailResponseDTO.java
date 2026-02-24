package br.com.logistics.tms.company.infrastructure.dto;

import br.com.logistics.tms.company.domain.AgreementCondition;

import java.time.Instant;
import java.util.Set;

public record AgreementDetailResponseDTO(
        String agreementId,
        String sourceCompanyId,
        String destinationCompanyId,
        String type,
        Set<AgreementCondition> conditions,
        Instant validFrom,
        Instant validTo,
        boolean isActive
) {
}
