package br.com.logistics.tms.company.infrastructure.jpa.repositories;

import br.com.logistics.tms.commons.infrastructure.jpa.repositories.CustomJpaRepository;
import br.com.logistics.tms.company.infrastructure.jpa.entities.CompanyOutboxEntity;
import org.springframework.stereotype.Repository;

import java.util.UUID;


@Repository
public interface CompanyOutboxJpaRepository extends CustomJpaRepository<CompanyOutboxEntity, UUID> {

}
