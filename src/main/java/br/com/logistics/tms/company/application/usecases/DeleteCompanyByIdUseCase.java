package br.com.logistics.tms.company.application.usecases;

import br.com.logistics.tms.commons.application.annotation.DomainService;
import br.com.logistics.tms.commons.application.usecases.VoidUseCase;
import br.com.logistics.tms.company.application.repositories.CompanyRepository;
import br.com.logistics.tms.company.domain.Company;
import br.com.logistics.tms.company.domain.CompanyId;
import br.com.logistics.tms.company.domain.exception.CompanyNotFoundException;

@DomainService
public class DeleteCompanyByIdUseCase implements VoidUseCase<DeleteCompanyByIdUseCase.Input> {

    private final CompanyRepository companyRepository;

    public DeleteCompanyByIdUseCase(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public void execute(final Input input) {
        final Company company = companyRepository.getCompanyById(CompanyId.with(input.companyId()))
                .orElseThrow(() -> new CompanyNotFoundException(String.format("Company not found for id: %s", input.companyId())));

        company.delete();

        companyRepository.delete(company);
    }

    public record Input(String companyId) {

    }

}
