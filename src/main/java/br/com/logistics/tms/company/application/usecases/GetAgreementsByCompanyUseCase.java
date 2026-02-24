package br.com.logistics.tms.company.application.usecases;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.DomainService;
import br.com.logistics.tms.commons.application.annotation.DatabaseRole;
import br.com.logistics.tms.commons.application.usecases.UseCase;
import br.com.logistics.tms.company.application.repositories.CompanyRepository;
import br.com.logistics.tms.company.domain.Company;
import br.com.logistics.tms.company.domain.CompanyId;
import br.com.logistics.tms.company.domain.exception.CompanyNotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@DomainService
@Cqrs(DatabaseRole.READ)
public class GetAgreementsByCompanyUseCase implements UseCase<GetAgreementsByCompanyUseCase.Input, GetAgreementsByCompanyUseCase.Output> {

    private final CompanyRepository companyRepository;

    public GetAgreementsByCompanyUseCase(final CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public Output execute(final Input input) {
        final Company company = companyRepository.getCompanyById(new CompanyId(input.companyId()))
                .orElseThrow(() -> new CompanyNotFoundException("Company not found"));

        final List<AgreementView> views = company.getAgreements().stream()
                .map(a -> new AgreementView(
                        a.agreementId().value(),
                        a.from().value(),
                        a.to().value(),
                        a.type().name(),
                        a.conditions().size(),
                        a.validFrom(),
                        a.validTo(),
                        a.isActive()
                ))
                .toList();

        return new Output(company.getCompanyId().value(), views);
    }

    public record Input(UUID companyId) {}

    public record Output(UUID companyId, List<AgreementView> agreements) {}

    public record AgreementView(
            UUID agreementId,
            UUID from,
            UUID to,
            String type,
            int conditionCount,
            Instant validFrom,
            Instant validTo,
            boolean isActive
    ) {}
}
