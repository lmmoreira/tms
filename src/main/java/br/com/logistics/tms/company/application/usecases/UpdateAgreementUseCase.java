package br.com.logistics.tms.company.application.usecases;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.DomainService;
import br.com.logistics.tms.commons.application.annotation.DatabaseRole;
import br.com.logistics.tms.commons.application.usecases.UseCase;
import br.com.logistics.tms.company.application.repositories.CompanyRepository;
import br.com.logistics.tms.company.domain.Agreement;
import br.com.logistics.tms.company.domain.AgreementCondition;
import br.com.logistics.tms.company.domain.AgreementId;
import br.com.logistics.tms.company.domain.Company;
import br.com.logistics.tms.company.domain.exception.AgreementNotFoundException;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@DomainService
@Cqrs(DatabaseRole.WRITE)
public class UpdateAgreementUseCase implements UseCase<UpdateAgreementUseCase.Input, UpdateAgreementUseCase.Output> {

    private final CompanyRepository companyRepository;

    public UpdateAgreementUseCase(final CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public Output execute(final Input input) {
        final AgreementId agreementId = new AgreementId(input.agreementId());

        final Company company = companyRepository.findCompanyByAgreementId(agreementId)
                .orElseThrow(() -> new AgreementNotFoundException("Agreement not found"));

        final Agreement existingAgreement = company.getAgreements().stream()
                .filter(a -> a.agreementId().equals(agreementId))
                .findFirst()
                .orElseThrow(() -> new AgreementNotFoundException("Agreement not found"));

        Agreement updatedAgreement = existingAgreement;

        if (input.validTo() != null) {
            updatedAgreement = updatedAgreement.updateValidTo(input.validTo());
        }

        if (input.conditions() != null && !input.conditions().isEmpty()) {
            updatedAgreement = updatedAgreement.updateConditions(input.conditions());
        }

        final Company updatedCompany = company.updateAgreement(agreementId, updatedAgreement);
        companyRepository.update(updatedCompany);

        return new Output(agreementId.value(), "Agreement updated successfully");
    }

    public record Input(
            UUID agreementId,
            Instant validTo,
            Set<AgreementCondition> conditions
    ) {}

    public record Output(UUID agreementId, String message) {}
}
