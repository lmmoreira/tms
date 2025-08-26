package br.com.logistics.tms.company.application.repositories;

import br.com.logistics.tms.company.domain.Cnpj;
import br.com.logistics.tms.company.domain.CompanyId;
import br.com.logistics.tms.company.domain.Company;
import java.util.Optional;

public interface CompanyRepository {

    Optional<Company> getCompanyById(CompanyId id);

    Optional<Company> getCompanyByCnpj(Cnpj cnpj);

    Company create(Company company);

    Company update(Company company);

    void delete(Company company);

}
