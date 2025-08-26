package br.com.logistics.tms.company.application.usecases;

import br.com.logistics.tms.commons.application.annotation.DomainService;
import br.com.logistics.tms.commons.application.usecases.UseCase;
import br.com.logistics.tms.commons.domain.exception.ValidationException;
import br.com.logistics.tms.company.application.repositories.CompanyRepository;
import br.com.logistics.tms.company.domain.Cnpj;
import br.com.logistics.tms.company.domain.Company;
import br.com.logistics.tms.company.domain.CompanyType;

import java.util.Map;
import java.util.Set;

@DomainService
public class CreateCompanyUseCase implements UseCase<CreateCompanyUseCase.Input, CreateCompanyUseCase.Output> {

    private final CompanyRepository companyRepository;

    public CreateCompanyUseCase(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Output execute(final Input input) {
        if (companyRepository.getCompanyByCnpj(new Cnpj(input.cnpj)).isPresent()) {
            throw new ValidationException("Company already exists");
        }

        final Company company = companyRepository.create(Company.createCompany(input.name, input.cnpj, input.types, input.configuration));
        return Output.ofCompany(company);
    }

    public record Input(String name, String cnpj, Set<CompanyType> types, Map<String, Object> configuration) {

    }

    public record Output(String companyId,
                         String name, String cnpj,
                         Set<CompanyType> types,
                         Map<String, Object> configuration) {

        public static CreateCompanyUseCase.Output ofCompany(Company company) {
            return new CreateCompanyUseCase.Output(company.getCompanyId().value().toString(),
                    company.getName(),
                    company.getCnpj().value(),
                    company.getCompanyTypes().value(),
                    company.getConfigurations().value());
        }
    }
}
