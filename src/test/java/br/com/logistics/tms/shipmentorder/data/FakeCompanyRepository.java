package br.com.logistics.tms.shipmentorder.data;

import br.com.logistics.tms.shipmentorder.application.repositories.CompanyRepository;
import br.com.logistics.tms.shipmentorder.domain.Company;
import br.com.logistics.tms.shipmentorder.domain.CompanyId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FakeCompanyRepository implements CompanyRepository {

    private final Map<UUID, Company> companies = new HashMap<>();

    @Override
    public Company save(final Company company) {
        companies.put(company.getCompanyId().value(), company);
        return company;
    }

    @Override
    public Optional<Company> findById(final CompanyId companyId) {
        return Optional.ofNullable(companies.get(companyId.value()));
    }

    @Override
    public boolean existsById(final UUID companyId) {
        return companies.containsKey(companyId);
    }

    public void clear() {
        companies.clear();
    }

    public int size() {
        return companies.size();
    }
}
