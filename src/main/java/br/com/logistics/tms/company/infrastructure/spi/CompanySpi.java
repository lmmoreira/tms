package br.com.logistics.tms.company.infrastructure.spi;

import br.com.logistics.tms.company.infrastructure.spi.dto.CompanyDTO;

import java.util.UUID;

public interface CompanySpi {

    public CompanyDTO getCompanyById(UUID companyId);

}
