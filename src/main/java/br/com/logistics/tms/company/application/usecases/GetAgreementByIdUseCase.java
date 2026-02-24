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
@Cqrs(DatabaseRole.READ)
public class GetAgreementByIdUseCase implements UseCase<GetAgreementByIdUseCase.Input, GetAgreementByIdUseCase.Output> {

    private final CompanyRepository companyRepository;

    public GetAgreementByIdUseCase(final CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public Output execute(final Input input) {
        final AgreementId agreementId = new AgreementId(input.agreementId());

        final Company company = companyRepository.findCompanyByAgreementId(agreementId)
                .orElseThrow(() -> new AgreementNotFoundException("Agreement not found"));

        final Agreement agreement = company.getAgreements().stream()
                .filter(a -> a.agreementId().equals(agreementId))
                .findFirst()
                .orElseThrow(() -> new AgreementNotFoundException("Agreement not found"));

        return new Output(
                agreement.agreementId().value(),
                agreement.from().value(),
                agreement.to().value(),
                agreement.type().name(),
                agreement.conditions(),
                agreement.validFrom(),
                agreement.validTo(),
                agreement.isActive()
        );
    }

    public record Input(UUID agreementId) {}

    public record Output(
            UUID agreementId,
            UUID sourceCompanyId,
            UUID destinationCompanyId,
            String type,
            Set<AgreementCondition> conditions,
            Instant validFrom,
            Instant validTo,
            boolean isActive
    ) {}
}
