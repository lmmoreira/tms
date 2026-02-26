package br.com.logistics.tms.company.application.usecases;

import br.com.logistics.tms.AbstractTestBase;
import br.com.logistics.tms.builders.domain.company.AgreementBuilder;
import br.com.logistics.tms.company.application.repositories.FakeCompanyRepository;
import br.com.logistics.tms.company.domain.*;
import br.com.logistics.tms.company.domain.exception.AgreementNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RemoveAgreementUseCaseTest extends AbstractTestBase {

    private FakeCompanyRepository companyRepository;
    private RemoveAgreementUseCase useCase;

    @BeforeEach
    void setUp() {
        companyRepository = new FakeCompanyRepository();
        useCase = new RemoveAgreementUseCase(companyRepository);
    }

    @Test
    @DisplayName("Should remove agreement from company")
    void shouldRemoveAgreementFromCompany() {
        final Company sourceCompany = Company.createCompany(
                "Company A",
                "11.111.111/1111-11",
                Set.of(CompanyType.SELLER),
                Map.of("test", "value")
        );
        final Company destinationCompany = Company.createCompany(
                "Company B",
                "22.222.222/2222-22",
                Set.of(CompanyType.LOGISTICS_PROVIDER),
                Map.of("test", "value")
        );
        companyRepository.create(sourceCompany);
        companyRepository.create(destinationCompany);

        final Agreement agreement = AgreementBuilder.anAgreement()
                .withFrom(sourceCompany.getCompanyId())
                .withTo(destinationCompany.getCompanyId())
                .withType(AgreementType.DELIVERS_WITH)
                .build();
        final Company companyWithAgreement = sourceCompany.addAgreement(agreement);
        companyRepository.update(companyWithAgreement);

        final RemoveAgreementUseCase.Input input = new RemoveAgreementUseCase.Input(
                agreement.agreementId().value()
        );

        final RemoveAgreementUseCase.Output output = useCase.execute(input);

        assertThat(output.agreementId()).isEqualTo(agreement.agreementId().value());
        assertThat(output.companyId()).isEqualTo(sourceCompany.getCompanyId().value());

        final Company updatedCompany = companyRepository.getCompanyById(sourceCompany.getCompanyId())
                .orElseThrow();
        assertThat(updatedCompany.getAgreements()).isEmpty();
    }

    @Test
    @DisplayName("Should fail when agreement not found")
    void shouldFailWhenAgreementNotFound() {
        final RemoveAgreementUseCase.Input input = new RemoveAgreementUseCase.Input(
                AgreementId.unique().value()
        );

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(AgreementNotFoundException.class)
                .hasMessageContaining("Agreement not found");
    }

    @Test
    @DisplayName("Should remove only specified agreement leaving others intact")
    void shouldRemoveOnlySpecifiedAgreementLeavingOthersIntact() {
        final Company sourceCompany = Company.createCompany(
                "Company A",
                "11.111.111/1111-11",
                Set.of(CompanyType.SELLER),
                Map.of("test", "value")
        );
        final Company destinationCompany1 = Company.createCompany(
                "Company B",
                "22.222.222/2222-22",
                Set.of(CompanyType.LOGISTICS_PROVIDER),
                Map.of("test", "value")
        );
        final Company destinationCompany2 = Company.createCompany(
                "Company C",
                "33.333.333/3333-33",
                Set.of(CompanyType.LOGISTICS_PROVIDER),
                Map.of("test", "value")
        );
        companyRepository.create(sourceCompany);
        companyRepository.create(destinationCompany1);
        companyRepository.create(destinationCompany2);

        final Agreement agreement1 = AgreementBuilder.anAgreement()
                .withFrom(sourceCompany.getCompanyId())
                .withTo(destinationCompany1.getCompanyId())
                .withType(AgreementType.DELIVERS_WITH)
                .build();
        final Agreement agreement2 = AgreementBuilder.anAgreement()
                .withFrom(sourceCompany.getCompanyId())
                .withTo(destinationCompany2.getCompanyId())
                .withType(AgreementType.DELIVERS_WITH)
                .build();
        
        Company companyWithAgreements = sourceCompany.addAgreement(agreement1);
        companyWithAgreements = companyWithAgreements.addAgreement(agreement2);
        companyRepository.update(companyWithAgreements);

        final RemoveAgreementUseCase.Input input = new RemoveAgreementUseCase.Input(
                agreement1.agreementId().value()
        );

        useCase.execute(input);

        final Company updatedCompany = companyRepository.getCompanyById(sourceCompany.getCompanyId())
                .orElseThrow();
        assertThat(updatedCompany.getAgreements()).hasSize(1);
        
        final Agreement remainingAgreement = updatedCompany.getAgreements().iterator().next();
        assertThat(remainingAgreement.agreementId()).isEqualTo(agreement2.agreementId());
        assertThat(remainingAgreement.to()).isEqualTo(destinationCompany2.getCompanyId());
    }

    @Test
    @DisplayName("Should remove expired agreement")
    void shouldRemoveExpiredAgreement() {
        final Company sourceCompany = Company.createCompany(
                "Company A",
                "11.111.111/1111-11",
                Set.of(CompanyType.SELLER),
                Map.of("test", "value")
        );
        final Company destinationCompany = Company.createCompany(
                "Company B",
                "22.222.222/2222-22",
                Set.of(CompanyType.LOGISTICS_PROVIDER),
                Map.of("test", "value")
        );
        companyRepository.create(sourceCompany);
        companyRepository.create(destinationCompany);

        final Instant pastValidFrom = Instant.now().minus(60, ChronoUnit.DAYS);
        final Instant pastValidTo = Instant.now().minus(30, ChronoUnit.DAYS);
        
        final Agreement expiredAgreement = AgreementBuilder.anAgreement()
                .withFrom(sourceCompany.getCompanyId())
                .withTo(destinationCompany.getCompanyId())
                .withType(AgreementType.DELIVERS_WITH)
                .withValidFrom(pastValidFrom)
                .withValidTo(pastValidTo)
                .build();
        final Company companyWithAgreement = sourceCompany.addAgreement(expiredAgreement);
        companyRepository.update(companyWithAgreement);

        final RemoveAgreementUseCase.Input input = new RemoveAgreementUseCase.Input(
                expiredAgreement.agreementId().value()
        );

        useCase.execute(input);

        final Company updatedCompany = companyRepository.getCompanyById(sourceCompany.getCompanyId())
                .orElseThrow();
        assertThat(updatedCompany.getAgreements()).isEmpty();
    }

    @Test
    @DisplayName("Should remove future agreement")
    void shouldRemoveFutureAgreement() {
        final Company sourceCompany = Company.createCompany(
                "Company A",
                "11.111.111/1111-11",
                Set.of(CompanyType.SELLER),
                Map.of("test", "value")
        );
        final Company destinationCompany = Company.createCompany(
                "Company B",
                "22.222.222/2222-22",
                Set.of(CompanyType.LOGISTICS_PROVIDER),
                Map.of("test", "value")
        );
        companyRepository.create(sourceCompany);
        companyRepository.create(destinationCompany);

        final Instant futureValidFrom = Instant.now().plus(30, ChronoUnit.DAYS);
        
        final Agreement futureAgreement = AgreementBuilder.anAgreement()
                .withFrom(sourceCompany.getCompanyId())
                .withTo(destinationCompany.getCompanyId())
                .withType(AgreementType.DELIVERS_WITH)
                .withValidFrom(futureValidFrom)
                .withNoValidTo()
                .build();
        final Company companyWithAgreement = sourceCompany.addAgreement(futureAgreement);
        companyRepository.update(companyWithAgreement);

        final RemoveAgreementUseCase.Input input = new RemoveAgreementUseCase.Input(
                futureAgreement.agreementId().value()
        );

        useCase.execute(input);

        final Company updatedCompany = companyRepository.getCompanyById(sourceCompany.getCompanyId())
                .orElseThrow();
        assertThat(updatedCompany.getAgreements()).isEmpty();
    }

    @Test
    @DisplayName("Should allow recreation of removed agreement")
    void shouldAllowRecreationOfRemovedAgreement() {
        final Company sourceCompany = Company.createCompany(
                "Company A",
                "11.111.111/1111-11",
                Set.of(CompanyType.SELLER),
                Map.of("test", "value")
        );
        final Company destinationCompany = Company.createCompany(
                "Company B",
                "22.222.222/2222-22",
                Set.of(CompanyType.LOGISTICS_PROVIDER),
                Map.of("test", "value")
        );
        companyRepository.create(sourceCompany);
        companyRepository.create(destinationCompany);

        final Agreement firstAgreement = AgreementBuilder.anAgreement()
                .withFrom(sourceCompany.getCompanyId())
                .withTo(destinationCompany.getCompanyId())
                .withType(AgreementType.DELIVERS_WITH)
                .build();
        Company companyWithAgreement = sourceCompany.addAgreement(firstAgreement);
        companyRepository.update(companyWithAgreement);

        final RemoveAgreementUseCase.Input removeInput = new RemoveAgreementUseCase.Input(
                firstAgreement.agreementId().value()
        );
        useCase.execute(removeInput);

        final Agreement newAgreement = AgreementBuilder.anAgreement()
                .withFrom(sourceCompany.getCompanyId())
                .withTo(destinationCompany.getCompanyId())
                .withType(AgreementType.DELIVERS_WITH)
                .build();
        
        final Company updatedCompany = companyRepository.getCompanyById(sourceCompany.getCompanyId())
                .orElseThrow();
        final Company companyWithNewAgreement = updatedCompany.addAgreement(newAgreement);
        companyRepository.update(companyWithNewAgreement);

        final Company finalCompany = companyRepository.getCompanyById(sourceCompany.getCompanyId())
                .orElseThrow();
        assertThat(finalCompany.getAgreements()).hasSize(1);
        
        final Agreement recreatedAgreement = finalCompany.getAgreements().iterator().next();
        assertThat(recreatedAgreement.agreementId()).isNotEqualTo(firstAgreement.agreementId());
    }

    @Test
    @DisplayName("Should remove agreement with conditions")
    void shouldRemoveAgreementWithConditions() {
        final Company sourceCompany = Company.createCompany(
                "Company A",
                "11.111.111/1111-11",
                Set.of(CompanyType.SELLER),
                Map.of("test", "value")
        );
        final Company destinationCompany = Company.createCompany(
                "Company B",
                "22.222.222/2222-22",
                Set.of(CompanyType.LOGISTICS_PROVIDER),
                Map.of("test", "value")
        );
        companyRepository.create(sourceCompany);
        companyRepository.create(destinationCompany);

        final AgreementCondition condition1 = new AgreementCondition(
                AgreementConditionId.unique(),
                AgreementConditionType.DISCOUNT_PERCENTAGE,
                Conditions.with(Map.of("value", 10.0))
        );
        final AgreementCondition condition2 = new AgreementCondition(
                AgreementConditionId.unique(),
                AgreementConditionType.DELIVERY_SLA_DAYS,
                Conditions.with(Map.of("value", 5))
        );
        
        final Agreement agreementWithConditions = AgreementBuilder.anAgreement()
                .withFrom(sourceCompany.getCompanyId())
                .withTo(destinationCompany.getCompanyId())
                .withType(AgreementType.DELIVERS_WITH)
                .withCondition(condition1)
                .withCondition(condition2)
                .build();
        final Company companyWithAgreement = sourceCompany.addAgreement(agreementWithConditions);
        companyRepository.update(companyWithAgreement);

        final RemoveAgreementUseCase.Input input = new RemoveAgreementUseCase.Input(
                agreementWithConditions.agreementId().value()
        );

        useCase.execute(input);

        final Company updatedCompany = companyRepository.getCompanyById(sourceCompany.getCompanyId())
                .orElseThrow();
        assertThat(updatedCompany.getAgreements()).isEmpty();
    }

    @Test
    @DisplayName("Should remove all agreements when executed multiple times")
    void shouldRemoveAllAgreementsWhenExecutedMultipleTimes() {
        final Company sourceCompany = Company.createCompany(
                "Company A",
                "11.111.111/1111-11",
                Set.of(CompanyType.SELLER),
                Map.of("test", "value")
        );
        final Company destinationCompany1 = Company.createCompany(
                "Company B",
                "22.222.222/2222-22",
                Set.of(CompanyType.LOGISTICS_PROVIDER),
                Map.of("test", "value")
        );
        final Company destinationCompany2 = Company.createCompany(
                "Company C",
                "33.333.333/3333-33",
                Set.of(CompanyType.LOGISTICS_PROVIDER),
                Map.of("test", "value")
        );
        companyRepository.create(sourceCompany);
        companyRepository.create(destinationCompany1);
        companyRepository.create(destinationCompany2);

        final Agreement agreement1 = AgreementBuilder.anAgreement()
                .withFrom(sourceCompany.getCompanyId())
                .withTo(destinationCompany1.getCompanyId())
                .build();
        final Agreement agreement2 = AgreementBuilder.anAgreement()
                .withFrom(sourceCompany.getCompanyId())
                .withTo(destinationCompany2.getCompanyId())
                .build();
        
        Company companyWithAgreements = sourceCompany.addAgreement(agreement1);
        companyWithAgreements = companyWithAgreements.addAgreement(agreement2);
        companyRepository.update(companyWithAgreements);

        useCase.execute(new RemoveAgreementUseCase.Input(agreement1.agreementId().value()));
        useCase.execute(new RemoveAgreementUseCase.Input(agreement2.agreementId().value()));

        final Company finalCompany = companyRepository.getCompanyById(sourceCompany.getCompanyId())
                .orElseThrow();
        assertThat(finalCompany.getAgreements()).isEmpty();
    }
}
