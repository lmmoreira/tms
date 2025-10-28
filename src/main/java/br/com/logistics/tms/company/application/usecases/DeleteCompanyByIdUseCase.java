package br.com.logistics.tms.company.application.usecases;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.DomainService;
import br.com.logistics.tms.commons.application.annotation.Role;
import br.com.logistics.tms.commons.application.usecases.UseCase;
import br.com.logistics.tms.company.application.repositories.CompanyRepository;
import br.com.logistics.tms.company.domain.Company;
import br.com.logistics.tms.company.domain.CompanyId;
import br.com.logistics.tms.company.domain.exception.CompanyNotFoundException;

import java.util.UUID;

@DomainService
@Cqrs(Role.WRITE)
public class DeleteCompanyByIdUseCase implements UseCase<DeleteCompanyByIdUseCase.Input, DeleteCompanyByIdUseCase.Output> {

    private final CompanyRepository companyRepository;

    public DeleteCompanyByIdUseCase(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Output execute(final Input input) {
        final Company company = companyRepository.getCompanyById(CompanyId.with(input.companyId()))
                .orElseThrow(() -> new CompanyNotFoundException(String.format("Company not found for id: %s", input.companyId())));

        company.delete();
        companyRepository.delete(company);

        return new Output(true);
    }

    public record Input(UUID companyId) {
    }

    public record Output(boolean deleted) {
    }

}
