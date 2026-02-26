package br.com.logistics.tms.company.infrastructure.repositories;

import br.com.logistics.tms.AbstractIntegrationTest;
import br.com.logistics.tms.company.application.repositories.CompanyRepository;
import br.com.logistics.tms.company.domain.*;
import br.com.logistics.tms.company.infrastructure.jpa.entities.CompanyEntity;
import br.com.logistics.tms.utils.CnpjGenerator;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class CompanyAgreementPersistenceTest extends AbstractIntegrationTest {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistAgreementsWhenSavingCompany() {
        final Company sourceCompany = Company.createCompany(
                "Source Company",
                CnpjGenerator.randomCnpj(),
                Set.of(CompanyType.MARKETPLACE),
                Map.of("test", "source")
        );
        final Company savedSource = companyRepository.create(sourceCompany);

        final Company destinationCompany = Company.createCompany(
                "Destination Company",
                CnpjGenerator.randomCnpj(),
                Set.of(CompanyType.LOGISTICS_PROVIDER),
                Map.of("test", "destination")
        );
        final Company savedDestination = companyRepository.create(destinationCompany);

        final Map<String, Object> conditionsData = new HashMap<>();
        conditionsData.put("percentage", 10.0);
        final AgreementCondition condition = new AgreementCondition(
                AgreementConditionId.unique(),
                AgreementConditionType.USES_PROVIDER,
                Conditions.with(conditionsData)
        );

        final Agreement agreement = new Agreement(
                AgreementId.unique(),
                savedSource.getCompanyId(),
                savedDestination.getCompanyId(),
                AgreementType.DELIVERS_WITH,
                Configurations.with(Map.of("priority", "high")),
                Set.of(condition),
                Instant.now().truncatedTo(ChronoUnit.SECONDS),
                null
        );

        final Company withAgreement = savedSource.addAgreement(agreement);
        companyRepository.update(withAgreement);

        entityManager.flush();
        entityManager.clear();

        final Company reloadedCompany = companyRepository.getCompanyById(savedSource.getCompanyId())
                .orElseThrow();

        assertThat(reloadedCompany.getAgreements()).hasSize(1);

        final Agreement reloadedAgreement = reloadedCompany.getAgreements().iterator().next();
        assertThat(reloadedAgreement.agreementId()).isEqualTo(agreement.agreementId());
        assertThat(reloadedAgreement.from()).isEqualTo(savedSource.getCompanyId());
        assertThat(reloadedAgreement.to()).isEqualTo(savedDestination.getCompanyId());
        assertThat(reloadedAgreement.type()).isEqualTo(AgreementType.DELIVERS_WITH);
        assertThat(reloadedAgreement.conditions()).hasSize(1);

        final CompanyEntity companyEntity = companyJpaRepository.findById(savedSource.getCompanyId().value())
                .orElseThrow();
        assertThat(companyEntity.getAgreements()).hasSize(1);
    }

    @Test
    void shouldCascadeDeleteAgreementsWhenRemovingFromCompany() {
        final Company sourceCompany = Company.createCompany(
                "Source Company",
                CnpjGenerator.randomCnpj(),
                Set.of(CompanyType.MARKETPLACE),
                Map.of("test", "source")
        );
        final Company savedSource = companyRepository.create(sourceCompany);

        final Company destinationCompany = Company.createCompany(
                "Destination Company",
                CnpjGenerator.randomCnpj(),
                Set.of(CompanyType.LOGISTICS_PROVIDER),
                Map.of("test", "destination")
        );
        final Company savedDestination = companyRepository.create(destinationCompany);

        final Map<String, Object> conditionsData = new HashMap<>();
        conditionsData.put("percentage", 15.0);
        final AgreementCondition condition = new AgreementCondition(
                AgreementConditionId.unique(),
                AgreementConditionType.USES_PROVIDER,
                Conditions.with(conditionsData)
        );

        final Agreement agreement = new Agreement(
                AgreementId.unique(),
                savedSource.getCompanyId(),
                savedDestination.getCompanyId(),
                AgreementType.DELIVERS_WITH,
                Configurations.with(Map.of("priority", "standard")),
                Set.of(condition),
                Instant.now().truncatedTo(ChronoUnit.SECONDS),
                Instant.now().plus(30, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS)
        );

        final Company companyWithAgreement = savedSource.addAgreement(agreement);
        companyRepository.update(companyWithAgreement);

        entityManager.flush();
        entityManager.clear();

        final Company companyBeforeRemoval = companyRepository.getCompanyById(savedSource.getCompanyId())
                .orElseThrow();
        final Company companyAfterRemoval = companyBeforeRemoval.removeAgreement(agreement.agreementId());
        companyRepository.update(companyAfterRemoval);

        entityManager.flush();
        entityManager.clear();

        final Company reloadedCompany = companyRepository.getCompanyById(savedSource.getCompanyId())
                .orElseThrow();

        assertThat(reloadedCompany.getAgreements()).isEmpty();

        final CompanyEntity companyEntity = companyJpaRepository.findById(savedSource.getCompanyId().value())
                .orElseThrow();
        assertThat(companyEntity.getAgreements()).isEmpty();
    }

    @Test
    void shouldFindCompanyByAgreementId() {
        final Company sourceCompany = Company.createCompany(
                "Findable Company",
                CnpjGenerator.randomCnpj(),
                Set.of(CompanyType.MARKETPLACE),
                Map.of("test", "findable")
        );
        final Company savedSource = companyRepository.create(sourceCompany);

        final Company destinationCompany = Company.createCompany(
                "Target Company",
                CnpjGenerator.randomCnpj(),
                Set.of(CompanyType.LOGISTICS_PROVIDER),
                Map.of("test", "target")
        );
        final Company savedDestination = companyRepository.create(destinationCompany);

        final Map<String, Object> conditionsData = new HashMap<>();
        conditionsData.put("percentage", 20.0);
        final AgreementCondition condition = new AgreementCondition(
                AgreementConditionId.unique(),
                AgreementConditionType.USES_PROVIDER,
                Conditions.with(conditionsData)
        );

        final Agreement agreement = new Agreement(
                AgreementId.unique(),
                savedSource.getCompanyId(),
                savedDestination.getCompanyId(),
                AgreementType.DELIVERS_WITH,
                Configurations.with(Map.of("priority", "express")),
                Set.of(condition),
                Instant.now().truncatedTo(ChronoUnit.SECONDS),
                null
        );

        final Company withAgreement = savedSource.addAgreement(agreement);
        companyRepository.update(withAgreement);

        entityManager.flush();
        entityManager.clear();

        final Company foundCompany = companyRepository.findCompanyByAgreementId(agreement.agreementId())
                .orElseThrow();

        assertThat(foundCompany.getCompanyId()).isEqualTo(savedSource.getCompanyId());
        assertThat(foundCompany.getAgreements()).hasSize(1);
        assertThat(foundCompany.getAgreements().iterator().next().agreementId())
                .isEqualTo(agreement.agreementId());
    }
}
