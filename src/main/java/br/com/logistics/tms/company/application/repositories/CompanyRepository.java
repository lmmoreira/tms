package br.com.logistics.tms.company.application.repositories;

import br.com.logistics.tms.company.domain.CompanyId;
import br.com.logistics.tms.company.domain.Company;
import java.util.Optional;

public interface CompanyRepository {

    Optional<Company> getCompanyById(CompanyId id);

}
