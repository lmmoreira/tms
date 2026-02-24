package br.com.logistics.tms.company.infrastructure.dto;

import java.time.Instant;
import java.util.List;

public record AgreementsListResponseDTO(
        String companyId,
        List<AgreementViewDTO> agreements
) {
    public record AgreementViewDTO(
            String agreementId,
            String from,
            String to,
            String type,
            int conditionCount,
            Instant validFrom,
            Instant validTo,
            boolean isActive
    ) {}
}
