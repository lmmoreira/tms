package br.com.logistics.tms.company.application.usecases;

import br.com.logistics.tms.commons.application.annotation.DomainService;
import br.com.logistics.tms.commons.application.usecases.UseCase;
import br.com.logistics.tms.company.application.repositories.CompanyRepository;
import br.com.logistics.tms.company.domain.Company;
import br.com.logistics.tms.company.domain.CompanyId;
import br.com.logistics.tms.company.domain.CompanyType;
import br.com.logistics.tms.company.domain.exception.CompanyNotFoundException;

import java.util.Map;
import java.util.Set;

@DomainService
public class GetCompanyByIdUseCase implements UseCase<GetCompanyByIdUseCase.Input, GetCompanyByIdUseCase.Output> {

    private final CompanyRepository companyRepository;

    public GetCompanyByIdUseCase(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Output execute(final Input input) {
        final Company company = companyRepository.getCompanyById(CompanyId.with(input.companyId()))
                .orElseThrow(() -> new CompanyNotFoundException(String.format("Company not found for id: %s", input.companyId())));

        return new Output(company.getCompanyId().value().toString(),
                company.getName(),
                company.getCnpj().value(),
                company.getCompanyTypes().value(),
                company.getConfigurations().value());
    }

    public record Input(String companyId) {

    }

    public record Output(String companyId,
                         String name,
                         String cnpj,
                         Set<CompanyType> types,
                         Map<String, Object> configuration) {
    }
}
