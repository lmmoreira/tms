package br.com.logistics.tms.company.application.repositories;

import br.com.logistics.tms.company.domain.AgreementId;
import br.com.logistics.tms.company.domain.Cnpj;
import br.com.logistics.tms.company.domain.Company;
import br.com.logistics.tms.company.domain.CompanyId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FakeCompanyRepository implements CompanyRepository {

    private final Map<CompanyId, Company> storage = new HashMap<>();

    @Override
    public Optional<Company> getCompanyById(final CompanyId id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<Company> getCompanyByCnpj(final Cnpj cnpj) {
        return storage.values().stream()
                .filter(company -> company.getCnpj().equals(cnpj))
                .findFirst();
    }

    @Override
    public Optional<Company> findCompanyByAgreementId(final AgreementId agreementId) {
        return storage.values().stream()
                .filter(company -> company.getAgreements().stream()
                        .anyMatch(agreement -> agreement.agreementId().equals(agreementId)))
                .findFirst();
    }

    @Override
    public Company create(final Company company) {
        storage.put(company.getCompanyId(), company);
        return company;
    }

    @Override
    public Company update(final Company company) {
        storage.put(company.getCompanyId(), company);
        return company;
    }

    @Override
    public void delete(final Company company) {
        storage.remove(company.getCompanyId());
    }

    public void clear() {
        storage.clear();
    }

    public int count() {
        return storage.size();
    }

    public boolean existsById(final CompanyId companyId) {
        return storage.containsKey(companyId);
    }
}
