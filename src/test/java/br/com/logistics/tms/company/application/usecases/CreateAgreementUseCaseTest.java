package br.com.logistics.tms.company.application.usecases;

import br.com.logistics.tms.builders.domain.company.AgreementBuilder;
import br.com.logistics.tms.commons.domain.exception.ValidationException;
import br.com.logistics.tms.company.application.repositories.FakeCompanyRepository;
import br.com.logistics.tms.company.domain.*;
import br.com.logistics.tms.company.domain.exception.CompanyNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static br.com.logistics.tms.assertions.domain.company.AgreementAssert.assertThatAgreement;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateAgreementUseCaseTest extends br.com.logistics.tms.AbstractTestBase {

    private FakeCompanyRepository companyRepository;
    private CreateAgreementUseCase useCase;

    @BeforeEach
    void setUp() {
        companyRepository = new FakeCompanyRepository();
        useCase = new CreateAgreementUseCase(companyRepository);
    }

    @Test
    @DisplayName("Should create agreement between two companies")
    void shouldCreateAgreementBetweenTwoCompanies() {
        final Company sourceCompany = Company.createCompany(
                "Shoppe Logistics",
                "12.345.678/9012-34",
                Set.of(CompanyType.SELLER),
                Map.of("region", "SP")
        );
        final Company destinationCompany = Company.createCompany(
                "Loggi Transportes",
                "98.765.432/1098-76",
                Set.of(CompanyType.LOGISTICS_PROVIDER),
                Map.of("region", "SP")
        );
        companyRepository.create(sourceCompany);
        companyRepository.create(destinationCompany);

        final Instant validFrom = Instant.now();
        final Instant validTo = validFrom.plus(365, ChronoUnit.DAYS);
        final Map<String, Object> configuration = new HashMap<>();
        configuration.put("discount", 15.0);
        final Set<AgreementCondition> conditions = Set.of(
                new AgreementCondition(
                        AgreementConditionId.unique(),
                        AgreementConditionType.DISCOUNT_PERCENTAGE,
                        Conditions.with(Map.of("value", 15.0))
                )
        );

        final CreateAgreementUseCase.Input input = new CreateAgreementUseCase.Input(
                sourceCompany.getCompanyId().value(),
                destinationCompany.getCompanyId().value(),
                AgreementType.DELIVERS_WITH,
                configuration,
                conditions,
                validFrom,
                validTo
        );

        final CreateAgreementUseCase.Output output = useCase.execute(input);

        assertThat(output.agreementId()).isNotNull();
        assertThat(output.sourceCompanyId()).isEqualTo(sourceCompany.getCompanyId().value());
        assertThat(output.destinationCompanyId()).isEqualTo(destinationCompany.getCompanyId().value());
        assertThat(output.agreementType()).isEqualTo("DELIVERS_WITH");

        final Company updatedCompany = companyRepository.getCompanyById(sourceCompany.getCompanyId())
                .orElseThrow();
        assertThat(updatedCompany.getAgreements()).hasSize(1);
        
        final Agreement agreement = updatedCompany.getAgreements().iterator().next();
        assertThatAgreement(agreement)
                .hasFrom(sourceCompany.getCompanyId())
                .hasTo(destinationCompany.getCompanyId())
                .hasType(AgreementType.DELIVERS_WITH)
                .hasConditionsCount(1);
    }

    @Test
    @DisplayName("Should create agreement without conditions")
    void shouldCreateAgreementWithoutConditions() {
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

        final CreateAgreementUseCase.Input input = new CreateAgreementUseCase.Input(
                sourceCompany.getCompanyId().value(),
                destinationCompany.getCompanyId().value(),
                AgreementType.DELIVERS_WITH,
                Map.of("default", true),
                new HashSet<>(),
                Instant.now(),
                null
        );

        final CreateAgreementUseCase.Output output = useCase.execute(input);

        assertThat(output.agreementId()).isNotNull();
        
        final Company updatedCompany = companyRepository.getCompanyById(sourceCompany.getCompanyId())
                .orElseThrow();
        final Agreement agreement = updatedCompany.getAgreements().iterator().next();
        assertThatAgreement(agreement).hasEmptyConditions();
    }

    @Test
    @DisplayName("Should fail when source company not found")
    void shouldFailWhenSourceCompanyNotFound() {
        final Company destinationCompany = Company.createCompany(
                "Company B",
                "22.222.222/2222-22",
                Set.of(CompanyType.LOGISTICS_PROVIDER),
                Map.of("test", "value")
        );
        companyRepository.create(destinationCompany);

        final CreateAgreementUseCase.Input input = new CreateAgreementUseCase.Input(
                CompanyId.unique().value(),
                destinationCompany.getCompanyId().value(),
                AgreementType.DELIVERS_WITH,
                Map.of("default", true),
                new HashSet<>(),
                Instant.now(),
                null
        );

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(CompanyNotFoundException.class)
                .hasMessageContaining("Source company not found");
    }

    @Test
    @DisplayName("Should fail when destination company not found")
    void shouldFailWhenDestinationCompanyNotFound() {
        final Company sourceCompany = Company.createCompany(
                "Company A",
                "11.111.111/1111-11",
                Set.of(CompanyType.SELLER),
                Map.of("test", "value")
        );
        companyRepository.create(sourceCompany);

        final CreateAgreementUseCase.Input input = new CreateAgreementUseCase.Input(
                sourceCompany.getCompanyId().value(),
                CompanyId.unique().value(),
                AgreementType.DELIVERS_WITH,
                Map.of("default", true),
                new HashSet<>(),
                Instant.now(),
                null
        );

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(CompanyNotFoundException.class)
                .hasMessageContaining("Destination company not found");
    }

    @Test
    @DisplayName("Should fail when creating self-referencing agreement")
    void shouldFailWhenCreatingSelfReferencingAgreement() {
        final Company company = Company.createCompany(
                "Company A",
                "11.111.111/1111-11",
                Set.of(CompanyType.SELLER),
                Map.of("test", "value")
        );
        companyRepository.create(company);

        final CreateAgreementUseCase.Input input = new CreateAgreementUseCase.Input(
                company.getCompanyId().value(),
                company.getCompanyId().value(),
                AgreementType.DELIVERS_WITH,
                Map.of("default", true),
                new HashSet<>(),
                Instant.now(),
                null
        );

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("source and destination must be different");
    }

    @Test
    @DisplayName("Should fail when creating duplicate agreement")
    void shouldFailWhenCreatingDuplicateAgreement() {
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

        final Agreement existingAgreement = AgreementBuilder.anAgreement()
                .withFrom(sourceCompany.getCompanyId())
                .withTo(destinationCompany.getCompanyId())
                .withType(AgreementType.DELIVERS_WITH)
                .build();
        final Company companyWithAgreement = sourceCompany.addAgreement(existingAgreement);
        companyRepository.update(companyWithAgreement);

        final CreateAgreementUseCase.Input input = new CreateAgreementUseCase.Input(
                sourceCompany.getCompanyId().value(),
                destinationCompany.getCompanyId().value(),
                AgreementType.DELIVERS_WITH,
                Map.of("default", true),
                new HashSet<>(),
                Instant.now(),
                null
        );

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should fail when creating overlapping agreement")
    void shouldFailWhenCreatingOverlappingAgreement() {
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

        final Instant validFrom = Instant.now();
        final Instant validTo = validFrom.plus(365, ChronoUnit.DAYS);
        
        final Agreement existingAgreement = AgreementBuilder.anAgreement()
                .withFrom(sourceCompany.getCompanyId())
                .withTo(destinationCompany.getCompanyId())
                .withType(AgreementType.DELIVERS_WITH)
                .withValidFrom(validFrom)
                .withValidTo(validTo)
                .build();
        final Company companyWithAgreement = sourceCompany.addAgreement(existingAgreement);
        companyRepository.update(companyWithAgreement);

        final Instant overlappingValidFrom = validFrom.plus(180, ChronoUnit.DAYS);
        final CreateAgreementUseCase.Input input = new CreateAgreementUseCase.Input(
                sourceCompany.getCompanyId().value(),
                destinationCompany.getCompanyId().value(),
                AgreementType.DELIVERS_WITH,
                Map.of("default", true),
                new HashSet<>(),
                overlappingValidFrom,
                null
        );

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Overlapping");
    }

    @Test
    @DisplayName("Should create agreement with multiple conditions")
    void shouldCreateAgreementWithMultipleConditions() {
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

        final Set<AgreementCondition> conditions = Set.of(
                new AgreementCondition(
                        AgreementConditionId.unique(),
                        AgreementConditionType.DISCOUNT_PERCENTAGE,
                        Conditions.with(Map.of("value", 10.0))
                ),
                new AgreementCondition(
                        AgreementConditionId.unique(),
                        AgreementConditionType.DELIVERY_SLA_DAYS,
                        Conditions.with(Map.of("value", 5))
                )
        );

        final CreateAgreementUseCase.Input input = new CreateAgreementUseCase.Input(
                sourceCompany.getCompanyId().value(),
                destinationCompany.getCompanyId().value(),
                AgreementType.DELIVERS_WITH,
                Map.of("default", true),
                conditions,
                Instant.now(),
                null
        );

        final CreateAgreementUseCase.Output output = useCase.execute(input);

        final Company updatedCompany = companyRepository.getCompanyById(sourceCompany.getCompanyId())
                .orElseThrow();
        final Agreement agreement = updatedCompany.getAgreements().iterator().next();
        assertThatAgreement(agreement).hasConditionsCount(2);
    }

    @Test
    @DisplayName("Should create open-ended agreement when validTo is null")
    void shouldCreateOpenEndedAgreementWhenValidToIsNull() {
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

        final CreateAgreementUseCase.Input input = new CreateAgreementUseCase.Input(
                sourceCompany.getCompanyId().value(),
                destinationCompany.getCompanyId().value(),
                AgreementType.DELIVERS_WITH,
                Map.of("default", true),
                new HashSet<>(),
                Instant.now(),
                null
        );

        useCase.execute(input);

        final Company updatedCompany = companyRepository.getCompanyById(sourceCompany.getCompanyId())
                .orElseThrow();
        final Agreement agreement = updatedCompany.getAgreements().iterator().next();
        assertThatAgreement(agreement).hasNoValidTo();
    }

    @Test
    @DisplayName("Should create agreement with future start date")
    void shouldCreateAgreementWithFutureStartDate() {
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
        final CreateAgreementUseCase.Input input = new CreateAgreementUseCase.Input(
                sourceCompany.getCompanyId().value(),
                destinationCompany.getCompanyId().value(),
                AgreementType.DELIVERS_WITH,
                Map.of("default", true),
                new HashSet<>(),
                futureValidFrom,
                null
        );

        useCase.execute(input);

        final Company updatedCompany = companyRepository.getCompanyById(sourceCompany.getCompanyId())
                .orElseThrow();
        final Agreement agreement = updatedCompany.getAgreements().iterator().next();
        assertThatAgreement(agreement)
                .hasValidFrom(futureValidFrom)
                .isNotActive();
    }
}
