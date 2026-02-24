package br.com.logistics.tms.company.infrastructure.dto;

public record AgreementResponseDTO(
        String agreementId,
        String sourceCompanyId,
        String destinationCompanyId,
        String agreementType
) {
}
