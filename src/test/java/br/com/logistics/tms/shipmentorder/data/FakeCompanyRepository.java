package br.com.logistics.tms.shipmentorder.data;

import br.com.logistics.tms.shipmentorder.application.repositories.CompanyRepository;
import br.com.logistics.tms.shipmentorder.domain.Company;
import br.com.logistics.tms.shipmentorder.domain.CompanyId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Fake implementation of CompanyRepository for testing purposes.
 * Uses an in-memory Map as storage instead of a real database.
 */
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

    /**
     * Clears all companies from the storage.
     * Useful for cleaning up between tests.
     */
    public void clear() {
        storage.clear();
    }

    /**
     * Returns the total count of companies in storage.
     * Useful for test assertions.
     */
    public int count() {
        return storage.size();
    }
}
