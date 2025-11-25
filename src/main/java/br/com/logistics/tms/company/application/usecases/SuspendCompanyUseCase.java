package br.com.logistics.tms.company.application.usecases;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.DomainService;
import br.com.logistics.tms.commons.application.annotation.DatabaseRole;
import br.com.logistics.tms.commons.application.usecases.UseCase;
import br.com.logistics.tms.company.application.repositories.CompanyRepository;
import br.com.logistics.tms.company.domain.Company;
import br.com.logistics.tms.company.domain.CompanyId;
import br.com.logistics.tms.company.domain.exception.CompanyNotFoundException;

import java.util.UUID;

@DomainService
@Cqrs(DatabaseRole.WRITE)
public class SuspendCompanyUseCase implements UseCase<SuspendCompanyUseCase.Input, SuspendCompanyUseCase.Output> {

    private final CompanyRepository companyRepository;

    public SuspendCompanyUseCase(final CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Output execute(final Input input) {
        final Company company = companyRepository.getCompanyById(CompanyId.with(input.companyId()))
                .orElseThrow(() -> new CompanyNotFoundException(String.format("Company not found for id: %s", input.companyId())));

        final Company suspended = company.suspend();
        companyRepository.update(suspended);

        return new Output(true);
    }

    public record Input(UUID companyId) {
    }

    public record Output(boolean suspended) {
    }
}
