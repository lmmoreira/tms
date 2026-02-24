package br.com.logistics.tms.company.application.usecases;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.DomainService;
import br.com.logistics.tms.commons.application.annotation.DatabaseRole;
import br.com.logistics.tms.commons.application.usecases.UseCase;
import br.com.logistics.tms.company.application.repositories.CompanyRepository;
import br.com.logistics.tms.company.domain.Agreement;
import br.com.logistics.tms.company.domain.AgreementCondition;
import br.com.logistics.tms.company.domain.AgreementType;
import br.com.logistics.tms.company.domain.Company;
import br.com.logistics.tms.company.domain.CompanyId;
import br.com.logistics.tms.company.domain.exception.CompanyNotFoundException;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@DomainService
@Cqrs(DatabaseRole.WRITE)
public class CreateAgreementUseCase implements UseCase<CreateAgreementUseCase.Input, CreateAgreementUseCase.Output> {

    private final CompanyRepository companyRepository;

    public CreateAgreementUseCase(final CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public Output execute(final Input input) {
        final Company sourceCompany = companyRepository.getCompanyById(new CompanyId(input.sourceCompanyId()))
                .orElseThrow(() -> new CompanyNotFoundException("Source company not found"));

        final Company destinationCompany = companyRepository.getCompanyById(new CompanyId(input.destinationCompanyId()))
                .orElseThrow(() -> new CompanyNotFoundException("Destination company not found"));

        final Agreement agreement = Agreement.createAgreement(
                sourceCompany.getCompanyId(),
                destinationCompany.getCompanyId(),
                input.type(),
                input.configuration(),
                input.conditions(),
                input.validFrom(),
                input.validTo()
        );

        final Company updatedCompany = sourceCompany.addAgreement(agreement);
        companyRepository.update(updatedCompany);

        return new Output(
                agreement.agreementId().value(),
                sourceCompany.getCompanyId().value(),
                destinationCompany.getCompanyId().value(),
                agreement.type().name()
        );
    }

    public record Input(
            UUID sourceCompanyId,
            UUID destinationCompanyId,
            AgreementType type,
            Map<String, Object> configuration,
            Set<AgreementCondition> conditions,
            Instant validFrom,
            Instant validTo
    ) {}

    public record Output(
            UUID agreementId,
            UUID sourceCompanyId,
            UUID destinationCompanyId,
            String agreementType
    ) {}
}
