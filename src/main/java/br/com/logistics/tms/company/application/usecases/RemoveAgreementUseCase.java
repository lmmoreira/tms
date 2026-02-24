package br.com.logistics.tms.company.application.usecases;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.DomainService;
import br.com.logistics.tms.commons.application.annotation.DatabaseRole;
import br.com.logistics.tms.commons.application.usecases.UseCase;
import br.com.logistics.tms.company.application.repositories.CompanyRepository;
import br.com.logistics.tms.company.domain.AgreementId;
import br.com.logistics.tms.company.domain.Company;
import br.com.logistics.tms.company.domain.exception.AgreementNotFoundException;

import java.util.UUID;

@DomainService
@Cqrs(DatabaseRole.WRITE)
public class RemoveAgreementUseCase implements UseCase<RemoveAgreementUseCase.Input, RemoveAgreementUseCase.Output> {

    private final CompanyRepository companyRepository;

    public RemoveAgreementUseCase(final CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public Output execute(final Input input) {
        final AgreementId agreementId = new AgreementId(input.agreementId());

        final Company company = companyRepository.findCompanyByAgreementId(agreementId)
                .orElseThrow(() -> new AgreementNotFoundException("Agreement not found"));

        final Company updatedCompany = company.removeAgreement(agreementId);
        companyRepository.update(updatedCompany);

        return new Output(agreementId.value(), company.getCompanyId().value());
    }

    public record Input(UUID agreementId) {}

    public record Output(UUID agreementId, UUID companyId) {}
}
