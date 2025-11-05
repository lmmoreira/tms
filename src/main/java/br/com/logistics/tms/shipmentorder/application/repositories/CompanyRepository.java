package br.com.logistics.tms.shipmentorder.application.repositories;

import br.com.logistics.tms.shipmentorder.domain.Company;
import br.com.logistics.tms.shipmentorder.domain.CompanyId;

import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository {

    Company save(Company company);

    Optional<Company> findById(CompanyId companyId);

    boolean existsById(UUID companyId);
}
