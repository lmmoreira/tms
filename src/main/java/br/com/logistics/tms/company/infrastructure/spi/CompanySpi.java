package br.com.logistics.tms.company.infrastructure.spi;

import br.com.logistics.tms.company.infrastructure.spi.dto.CompanyDTO;

public interface CompanySpi {

    public CompanyDTO getCompanyById(String companyId);

}
