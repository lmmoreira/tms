package br.com.logistics.tms.shipmentorder.application.repositories;

import br.com.logistics.tms.shipmentorder.domain.Company;
import br.com.logistics.tms.shipmentorder.domain.CompanyId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FakeCompanyRepository implements CompanyRepository {

    private final Map<CompanyId, Company> storage = new HashMap<>();

    @Override
    public Company save(final Company company) {
        storage.put(company.getCompanyId(), company);
        return company;
    }

    @Override
    public Optional<Company> findById(final CompanyId companyId) {
        return Optional.ofNullable(storage.get(companyId));
    }

    @Override
    public boolean existsById(final CompanyId companyId) {
        return storage.containsKey(companyId);
    }

    public void clear() {
        storage.clear();
    }

    public int count() {
        return storage.size();
    }
}
