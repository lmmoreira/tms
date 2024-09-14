package br.com.logistics.tms.company.infrastructure.jpa.repositories;

import br.com.logistics.tms.commons.infrastructure.jpa.repositories.CustomRepository;
import br.com.logistics.tms.company.infrastructure.jpa.entities.CompanyEntity;
import java.util.UUID;

public interface CompanyJpaRepository extends CustomRepository<CompanyEntity, UUID> {


}
