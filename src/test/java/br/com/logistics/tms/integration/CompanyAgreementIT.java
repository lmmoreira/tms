package br.com.logistics.tms.integration;

import br.com.logistics.tms.AbstractIntegrationTest;
import br.com.logistics.tms.builders.dto.CreateAgreementDTOBuilder;
import br.com.logistics.tms.builders.dto.CreateCompanyDTOBuilder;
import br.com.logistics.tms.company.domain.AgreementId;
import br.com.logistics.tms.company.domain.AgreementType;
import br.com.logistics.tms.company.domain.CompanyId;
import br.com.logistics.tms.company.domain.CompanyType;
import br.com.logistics.tms.company.infrastructure.dto.UpdateAgreementDTO;
import br.com.logistics.tms.company.infrastructure.jpa.entities.AgreementEntity;
import br.com.logistics.tms.company.infrastructure.jpa.entities.CompanyEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static br.com.logistics.tms.assertions.jpa.AgreementEntityAssert.assertThatAgreement;
import static br.com.logistics.tms.assertions.jpa.CompanyEntityAssert.assertThatCompany;
import static org.assertj.core.api.Assertions.assertThat;

class CompanyAgreementIT extends AbstractIntegrationTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void shouldCreateUpdateAndRemoveAgreementBetweenCompaniesInCompleteBusinessFlow() throws Exception {
        // Story Part 1: Create Shoppe marketplace
        final CompanyId shoppeId = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withName("Shoppe")
                        .withTypes(CompanyType.MARKETPLACE)
                        .build()
        );

        final CompanyEntity shoppe = companyJpaRepository.findById(shoppeId.value()).orElseThrow();
        assertThatCompany(shoppe)
                .hasName("Shoppe")
                .hasTypes(CompanyType.MARKETPLACE)
                .isActive();

        // Story Part 2: Create Loggi logistics provider
        final CompanyId loggiId = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withName("Loggi")
                        .withTypes(CompanyType.LOGISTICS_PROVIDER)
                        .build()
        );

        final CompanyEntity loggi = companyJpaRepository.findById(loggiId.value()).orElseThrow();
        assertThatCompany(loggi)
                .hasName("Loggi")
                .hasTypes(CompanyType.LOGISTICS_PROVIDER)
                .isActive();

        // Story Part 3: Create agreement (Shoppe delivers with Loggi)
        final Instant validFrom = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        final Instant initialValidTo = Instant.now().plus(90, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
        
        final AgreementId agreementId = agreementFixture.createAgreement(
                shoppeId.value(),
                CreateAgreementDTOBuilder.aCreateAgreementDTO()
                        .withDestinationCompanyId(loggiId.value())
                        .withType(AgreementType.DELIVERS_WITH)
                        .withValidTo(initialValidTo)
                        .withValidFrom(validFrom)
                        .build()
        );

        // Story Part 4: Verify agreement persisted in database
        final CompanyEntity shoppeWithAgreement = entityManager.createQuery(
                "SELECT c FROM CompanyEntity c LEFT JOIN FETCH c.agreements WHERE c.id = :id",
                CompanyEntity.class
        ).setParameter("id", shoppeId.value()).getSingleResult();
        
        assertThatCompany(shoppeWithAgreement)
                .hasName("Shoppe");

        assertThat(shoppeWithAgreement.getAgreements()).hasSize(1);
        
        final AgreementEntity agreement = shoppeWithAgreement.getAgreements().iterator().next();
        assertThatAgreement(agreement)
                .hasId(agreementId.value())
                .hasFrom(shoppeId.value())
                .hasTo(loggiId.value())
                .hasRelationType("DELIVERS_WITH")
                .hasValidFrom(validFrom)
                .hasValidTo(initialValidTo);

        // Story Part 5: Update agreement validity
        final Instant validTo = Instant.now().plus(30, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
        
        agreementFixture.updateAgreement(
                shoppeId.value(),
                agreementId.value(),
                new UpdateAgreementDTO(validTo, null)
        );

        // Story Part 6: Verify update in database
        final CompanyEntity shoppeWithUpdatedAgreement = entityManager.createQuery(
                "SELECT c FROM CompanyEntity c LEFT JOIN FETCH c.agreements WHERE c.id = :id",
                CompanyEntity.class
        ).setParameter("id", shoppeId.value()).getSingleResult();
        
        assertThat(shoppeWithUpdatedAgreement.getAgreements()).hasSize(1);

        final AgreementEntity updatedAgreement = shoppeWithUpdatedAgreement.getAgreements().iterator().next();
        assertThatAgreement(updatedAgreement)
                .hasId(agreementId.value())
                .hasFrom(shoppeId.value())
                .hasTo(loggiId.value())
                .hasRelationType("DELIVERS_WITH")
                .hasValidFrom(validFrom)
                .hasValidTo(validTo);

        // Story Part 7: Remove agreement
        agreementFixture.removeAgreement(shoppeId.value(), agreementId.value());

        // Story Part 8: Verify deletion (agreement gone, companies still exist)
        final CompanyEntity shoppeAfterRemoval = entityManager.createQuery(
                "SELECT c FROM CompanyEntity c LEFT JOIN FETCH c.agreements WHERE c.id = :id",
                CompanyEntity.class
        ).setParameter("id", shoppeId.value()).getSingleResult();
        
        assertThatCompany(shoppeAfterRemoval)
                .hasName("Shoppe")
                .isActive();

        assertThat(shoppeAfterRemoval.getAgreements()).isEmpty();

        final CompanyEntity loggiAfterRemoval = companyJpaRepository.findById(loggiId.value()).orElseThrow();
        assertThatCompany(loggiAfterRemoval)
                .hasName("Loggi")
                .isActive();
    }
}
