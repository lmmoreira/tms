package br.com.logistics.tms.company.infrastructure.jpa.repositories;

import br.com.logistics.tms.commons.infrastructure.jpa.repositories.CustomJpaRepository;
import br.com.logistics.tms.company.infrastructure.jpa.entities.CompanyEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface CompanyJpaRepository extends CustomJpaRepository<CompanyEntity, UUID> {

    @EntityGraph(attributePaths = {"companyTypes"})
    Optional<CompanyEntity> findById(UUID id);

    Optional<CompanyEntity> findByCnpj(String cnpj);

}
