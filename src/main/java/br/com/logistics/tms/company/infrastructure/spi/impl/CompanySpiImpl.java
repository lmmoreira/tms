package br.com.logistics.tms.company.infrastructure.spi.impl;

import br.com.logistics.tms.company.application.usecases.GetCompanyByIdUseCase;
import br.com.logistics.tms.company.infrastructure.spi.CompanySpi;
import br.com.logistics.tms.company.infrastructure.spi.dto.CompanyDTO;
import org.springframework.stereotype.Component;

@Component
public class CompanySpiImpl implements CompanySpi {

    private final GetCompanyByIdUseCase getCompanyByIdUseCase;

    public CompanySpiImpl(GetCompanyByIdUseCase getCompanyByIdUseCase) {
        this.getCompanyByIdUseCase = getCompanyByIdUseCase;
    }

    @Override
    public CompanyDTO getCompanyById(String companyId) {
        GetCompanyByIdUseCase.Output output = getCompanyByIdUseCase.execute(new GetCompanyByIdUseCase.Input(companyId));
        return new CompanyDTO(output.companyId(), output.name(), output.cnpj());
    }
}
