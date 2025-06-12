package br.com.logistics.tms.company.infrastructure.jpa.pg;

import br.com.logistics.tms.commons.infrastructure.jpa.repositories.CustomJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;


@Repository
public interface CompanyPgJpaRepository extends CustomJpaRepository<CompanyPgEntity, UUID> {


}
