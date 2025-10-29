package br.com.logistics.tms.company.application.usecases;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.DomainService;
import br.com.logistics.tms.commons.application.annotation.DatabaseRole;
import br.com.logistics.tms.commons.application.usecases.UseCase;
import br.com.logistics.tms.commons.domain.exception.ValidationException;
import br.com.logistics.tms.company.application.repositories.CompanyRepository;
import br.com.logistics.tms.company.domain.Company;
import br.com.logistics.tms.company.domain.CompanyId;
import br.com.logistics.tms.company.domain.CompanyType;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@DomainService
@Cqrs(DatabaseRole.WRITE)
public class UpdateCompanyUseCase implements UseCase<UpdateCompanyUseCase.Input, UpdateCompanyUseCase.Output> {

    private final CompanyRepository companyRepository;

    public UpdateCompanyUseCase(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Output execute(final Input input) {
        final Optional<Company> existingCompany = companyRepository.getCompanyById(CompanyId.with(input.companyId));

        if (existingCompany.isEmpty()) {
            throw new ValidationException("Company not found");
        }

        if (!existingCompany.get().getCompanyId().equals(CompanyId.with(input.companyId))) {
            throw new ValidationException("Company already exists");
        }

        final Company updatedCompany = existingCompany.get()
                .updateName(input.name)
                .updateCnpj(input.cnpj)
                .updateTypes(input.types)
                .updateConfigurations(input.configuration);

        final Company company = companyRepository.update(updatedCompany);
        return new Output(company.getCompanyId().value(),
                company.getName(),
                company.getCnpj().value(),
                company.getCompanyTypes().value(),
                company.getConfigurations().value());
    }

    public record Input(UUID companyId,
                        String name,
                        String cnpj,
                        Set<CompanyType> types,
                        Map<String, Object> configuration) {
    }

    public record Output(UUID companyId,
                         String name,
                         String cnpj,
                         Set<CompanyType> types,
                         Map<String, Object> configuration) {
    }
}
