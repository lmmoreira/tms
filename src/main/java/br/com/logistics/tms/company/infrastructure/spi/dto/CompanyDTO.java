package br.com.logistics.tms.company.infrastructure.spi.dto;

import br.com.logistics.tms.company.application.usecases.GetCompanyByIdUseCase.Output.OutputCompany;

public record CompanyDTO(String companyId,
                         String name,
                         String cnpj) {

    public static CompanyDTO of(final OutputCompany company) {
        return new CompanyDTO(company.companyId(), company.name(),
            company.cnpj());
    }

}