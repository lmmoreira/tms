package br.com.logistics.tms.company.infrastructure.jpa.repositories;

import br.com.logistics.tms.commons.infrastructure.jpa.repositories.CustomJpaRepository;
import br.com.logistics.tms.company.infrastructure.jpa.entities.CompanyEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface CompanyJpaRepository extends CustomJpaRepository<CompanyEntity, UUID> {

    @EntityGraph(attributePaths = {"companyTypes", "agreements"})
    Optional<CompanyEntity> findById(UUID id);

    Optional<CompanyEntity> findByCnpj(String cnpj);

    @Query("SELECT c FROM CompanyEntity c JOIN c.agreements a WHERE a.id = :agreementId")
    Optional<CompanyEntity> findByAgreementsId(@Param("agreementId") UUID agreementId);

}
