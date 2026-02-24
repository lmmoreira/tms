package br.com.logistics.tms.company.application.repositories;

import br.com.logistics.tms.company.domain.AgreementId;
import br.com.logistics.tms.company.domain.Cnpj;
import br.com.logistics.tms.company.domain.CompanyId;
import br.com.logistics.tms.company.domain.Company;
import java.util.Optional;

public interface CompanyRepository {

    Optional<Company> getCompanyById(CompanyId id);

    Optional<Company> getCompanyByCnpj(Cnpj cnpj);

    /**
     * Find company that owns the specified agreement.
     * Used by RemoveAgreementUseCase to locate source company.
     */
    Optional<Company> findCompanyByAgreementId(AgreementId agreementId);

    Company create(Company company);

    Company update(Company company);

    void delete(Company company);

}
