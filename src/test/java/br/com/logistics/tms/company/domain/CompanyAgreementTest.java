package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Company Agreement Management Tests")
class CompanyAgreementTest {

    @Test
    @DisplayName("Should add agreement and return new Company instance")
    void shouldAddAgreementImmutably() {
        final Company company = Company.createCompany(
                "Source Company",
                "12345678901234",
                Set.of(CompanyType.MARKETPLACE),
                Map.of("test", "value")
        );

        final Company destination = Company.createCompany(
                "Destination Company",
                "98765432109876",
                Set.of(CompanyType.LOGISTICS_PROVIDER),
                Map.of("test", "value")
        );

        final Agreement agreement = Agreement.createAgreement(
                company.getCompanyId(),
                destination.getCompanyId(),
                AgreementType.DELIVERS_WITH,
                Map.of("priority", "high"),
                Set.of(),
                Instant.now().truncatedTo(ChronoUnit.SECONDS),
                null
        );

        final Company updated = company.addAgreement(agreement);

        assertThat(updated).isNotSameAs(company);
        assertThat(updated.getAgreements()).hasSize(1);
        assertThat(updated.getAgreements()).contains(agreement);
        assertThat(company.getAgreements()).isEmpty();
    }

    @Test
    @DisplayName("Should place AgreementAdded domain event when adding agreement")
    void shouldPlaceAgreementAddedEvent() {
        final Company company = Company.createCompany(
                "Source Company",
                "11111111111111",
                Set.of(CompanyType.MARKETPLACE),
                Map.of()
        );

        final Company destination = Company.createCompany(
                "Destination Company",
                "22222222222222",
                Set.of(CompanyType.LOGISTICS_PROVIDER),
                Map.of()
        );

        final Agreement agreement = Agreement.createAgreement(
                company.getCompanyId(),
                destination.getCompanyId(),
                AgreementType.DELIVERS_WITH,
                Map.of(),
                Set.of(),
                Instant.now().truncatedTo(ChronoUnit.SECONDS),
                null
        );

        final Company updated = company.addAgreement(agreement);

        assertThat(updated.getDomainEvents())
                .isNotEmpty()
                .anyMatch(event -> event instanceof AgreementAdded);

        final AgreementAdded event = (AgreementAdded) updated.getDomainEvents().stream()
                .filter(e -> e instanceof AgreementAdded)
                .findFirst()
                .orElseThrow();

        assertThat(event.getSourceCompanyId()).isEqualTo(company.getCompanyId().value());
        assertThat(event.getAgreementId()).isEqualTo(agreement.agreementId().value());
        assertThat(event.getDestinationCompanyId()).isEqualTo(destination.getCompanyId().value());
        assertThat(event.getAgreementType()).isEqualTo("DELIVERS_WITH");
    }

    @Test
    @DisplayName("Should reject duplicate agreement")
    void shouldRejectDuplicateAgreement() {
        final Company company = Company.createCompany(
                "Source Company",
                "33333333333333",
                Set.of(CompanyType.MARKETPLACE),
                Map.of()
        );

        final Company destination = Company.createCompany(
                "Destination Company",
                "44444444444444",
                Set.of(CompanyType.LOGISTICS_PROVIDER),
                Map.of()
        );

        final Agreement agreement = Agreement.createAgreement(
                company.getCompanyId(),
                destination.getCompanyId(),
                AgreementType.DELIVERS_WITH,
                Map.of(),
                Set.of(),
                Instant.now().truncatedTo(ChronoUnit.SECONDS),
                null
        );

        final Company withAgreement = company.addAgreement(agreement);

        assertThatThrownBy(() -> withAgreement.addAgreement(agreement))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Agreement already exists");
    }

    @Test
    @DisplayName("Should reject agreement with mismatched source company")
    void shouldRejectMismatchedSourceCompany() {
        final Company company = Company.createCompany(
                "Source Company",
                "55555555555555",
                Set.of(CompanyType.MARKETPLACE),
                Map.of()
        );

        final Company destination = Company.createCompany(
                "Destination Company",
                "66666666666666",
                Set.of(CompanyType.LOGISTICS_PROVIDER),
                Map.of()
        );

        final CompanyId wrongSource = CompanyId.unique();

        final Agreement agreement = Agreement.createAgreement(
                wrongSource,
                destination.getCompanyId(),
                AgreementType.DELIVERS_WITH,
                Map.of(),
                Set.of(),
                Instant.now().truncatedTo(ChronoUnit.SECONDS),
                null
        );

        assertThatThrownBy(() -> company.addAgreement(agreement))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Agreement source must match company");
    }

    @Test
    @DisplayName("Should reject overlapping active agreement")
    void shouldRejectOverlappingAgreement() {
        final Company company = Company.createCompany(
                "Source Company",
                "77777777777777",
                Set.of(CompanyType.MARKETPLACE),
                Map.of()
        );

        final Company destination = Company.createCompany(
                "Destination Company",
                "88888888888888",
                Set.of(CompanyType.LOGISTICS_PROVIDER),
                Map.of()
        );

        final Instant baseDate = Instant.parse("2026-01-01T00:00:00Z");

        final Agreement agreement1 = Agreement.createAgreement(
                company.getCompanyId(),
                destination.getCompanyId(),
                AgreementType.DELIVERS_WITH,
                Map.of(),
                Set.of(),
                baseDate,
                baseDate.plus(30, ChronoUnit.DAYS)
        );

        final Company withAgreement = company.addAgreement(agreement1);

        final Agreement overlappingAgreement = Agreement.createAgreement(
                company.getCompanyId(),
                destination.getCompanyId(),
                AgreementType.DELIVERS_WITH,
                Map.of(),
                Set.of(),
                baseDate.plus(15, ChronoUnit.DAYS),
                baseDate.plus(45, ChronoUnit.DAYS)
        );

        assertThatThrownBy(() -> withAgreement.addAgreement(overlappingAgreement))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Overlapping active agreement exists");
    }

    @Test
    @DisplayName("Should remove agreement and return new Company instance")
    void shouldRemoveAgreementImmutably() {
        final Company company = Company.createCompany(
                "Source Company",
                "99999999999999",
                Set.of(CompanyType.MARKETPLACE),
                Map.of()
        );

        final Company destination = Company.createCompany(
                "Destination Company",
                "10101010101010",
                Set.of(CompanyType.LOGISTICS_PROVIDER),
                Map.of()
        );

        final Agreement agreement = Agreement.createAgreement(
                company.getCompanyId(),
                destination.getCompanyId(),
                AgreementType.DELIVERS_WITH,
                Map.of(),
                Set.of(),
                Instant.now().truncatedTo(ChronoUnit.SECONDS),
                null
        );

        final Company withAgreement = company.addAgreement(agreement);
        final Company withoutAgreement = withAgreement.removeAgreement(agreement.agreementId());

        assertThat(withoutAgreement).isNotSameAs(withAgreement);
        assertThat(withoutAgreement.getAgreements()).isEmpty();
        assertThat(withAgreement.getAgreements()).hasSize(1);
    }

    @Test
    @DisplayName("Should place AgreementRemoved domain event when removing agreement")
    void shouldPlaceAgreementRemovedEvent() {
        final Company company = Company.createCompany(
                "Source Company",
                "11223344556677",
                Set.of(CompanyType.MARKETPLACE),
                Map.of()
        );

        final Company destination = Company.createCompany(
                "Destination Company",
                "77665544332211",
                Set.of(CompanyType.LOGISTICS_PROVIDER),
                Map.of()
        );

        final Agreement agreement = Agreement.createAgreement(
                company.getCompanyId(),
                destination.getCompanyId(),
                AgreementType.DELIVERS_WITH,
                Map.of(),
                Set.of(),
                Instant.now().truncatedTo(ChronoUnit.SECONDS),
                null
        );

        final Company withAgreement = company.addAgreement(agreement);
        final Company withoutAgreement = withAgreement.removeAgreement(agreement.agreementId());

        assertThat(withoutAgreement.getDomainEvents())
                .anyMatch(event -> event instanceof AgreementRemoved);

        final AgreementRemoved event = (AgreementRemoved) withoutAgreement.getDomainEvents().stream()
                .filter(e -> e instanceof AgreementRemoved)
                .findFirst()
                .orElseThrow();

        assertThat(event.getSourceCompanyId()).isEqualTo(company.getCompanyId().value());
        assertThat(event.getAgreementId()).isEqualTo(agreement.agreementId().value());
        assertThat(event.getDestinationCompanyId()).isEqualTo(destination.getCompanyId().value());
    }

    @Test
    @DisplayName("Should reject removing non-existent agreement")
    void shouldRejectRemovingNonExistentAgreement() {
        final Company company = Company.createCompany(
                "Source Company",
                "13579135791357",
                Set.of(CompanyType.MARKETPLACE),
                Map.of()
        );

        final AgreementId nonExistentId = AgreementId.unique();

        assertThatThrownBy(() -> company.removeAgreement(nonExistentId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Agreement not found");
    }

    @Test
    @DisplayName("Should update agreement and return new Company instance")
    void shouldUpdateAgreementImmutably() {
        final Company company = Company.createCompany(
                "Source Company",
                "24680246802468",
                Set.of(CompanyType.MARKETPLACE),
                Map.of()
        );

        final Company destination = Company.createCompany(
                "Destination Company",
                "86420864208642",
                Set.of(CompanyType.LOGISTICS_PROVIDER),
                Map.of()
        );

        final Instant validFrom = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        final Agreement originalAgreement = Agreement.createAgreement(
                company.getCompanyId(),
                destination.getCompanyId(),
                AgreementType.DELIVERS_WITH,
                Map.of(),
                Set.of(),
                validFrom,
                null
        );

        final Company withAgreement = company.addAgreement(originalAgreement);

        final Instant newValidTo = validFrom.plus(365, ChronoUnit.DAYS);
        final Agreement updatedAgreement = originalAgreement.updateValidTo(newValidTo);

        final Company updated = withAgreement.updateAgreement(originalAgreement.agreementId(), updatedAgreement);

        assertThat(updated).isNotSameAs(withAgreement);
        assertThat(updated.getAgreements()).hasSize(1);
        final Agreement resultAgreement = updated.getAgreements().iterator().next();
        assertThat(resultAgreement.validTo()).isEqualTo(newValidTo);
    }

    @Test
    @DisplayName("Should place AgreementUpdated domain event when updating agreement")
    void shouldPlaceAgreementUpdatedEvent() {
        final Company company = Company.createCompany(
                "Source Company",
                "11112222333344",
                Set.of(CompanyType.MARKETPLACE),
                Map.of()
        );

        final Company destination = Company.createCompany(
                "Destination Company",
                "44443333222211",
                Set.of(CompanyType.LOGISTICS_PROVIDER),
                Map.of()
        );

        final Instant validFrom = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        final Agreement originalAgreement = Agreement.createAgreement(
                company.getCompanyId(),
                destination.getCompanyId(),
                AgreementType.DELIVERS_WITH,
                Map.of(),
                Set.of(),
                validFrom,
                null
        );

        final Company withAgreement = company.addAgreement(originalAgreement);

        final Instant newValidTo = validFrom.plus(180, ChronoUnit.DAYS);
        final Agreement updatedAgreement = originalAgreement.updateValidTo(newValidTo);

        final Company updated = withAgreement.updateAgreement(originalAgreement.agreementId(), updatedAgreement);

        assertThat(updated.getDomainEvents())
                .anyMatch(event -> event instanceof AgreementUpdated);

        final AgreementUpdated event = (AgreementUpdated) updated.getDomainEvents().stream()
                .filter(e -> e instanceof AgreementUpdated)
                .findFirst()
                .orElseThrow();

        assertThat(event.getSourceCompanyId()).isEqualTo(company.getCompanyId().value());
        assertThat(event.getAgreementId()).isEqualTo(originalAgreement.agreementId().value());
        assertThat(event.getFieldChanged()).isEqualTo("validTo");
    }

    @Test
    @DisplayName("Should reject updating non-existent agreement")
    void shouldRejectUpdatingNonExistentAgreement() {
        final Company company = Company.createCompany(
                "Source Company",
                "55556666777788",
                Set.of(CompanyType.MARKETPLACE),
                Map.of()
        );

        final Company destination = Company.createCompany(
                "Destination Company",
                "88887777666655",
                Set.of(CompanyType.LOGISTICS_PROVIDER),
                Map.of()
        );

        final Agreement nonExistentAgreement = Agreement.createAgreement(
                company.getCompanyId(),
                destination.getCompanyId(),
                AgreementType.DELIVERS_WITH,
                Map.of(),
                Set.of(),
                Instant.now().truncatedTo(ChronoUnit.SECONDS),
                null
        );

        assertThatThrownBy(() -> company.updateAgreement(nonExistentAgreement.agreementId(), nonExistentAgreement))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Agreement not found");
    }

    @Test
    @DisplayName("Should reject update that would create overlap")
    void shouldRejectUpdateCreatingOverlap() {
        final Company company = Company.createCompany(
                "Source Company",
                "99998888777766",
                Set.of(CompanyType.MARKETPLACE),
                Map.of()
        );

        final Company destination = Company.createCompany(
                "Destination Company",
                "66677778888999",
                Set.of(CompanyType.LOGISTICS_PROVIDER),
                Map.of()
        );

        final Instant baseDate = Instant.parse("2026-01-01T00:00:00Z");

        final Agreement agreement1 = Agreement.createAgreement(
                company.getCompanyId(),
                destination.getCompanyId(),
                AgreementType.DELIVERS_WITH,
                Map.of(),
                Set.of(),
                baseDate,
                baseDate.plus(30, ChronoUnit.DAYS)
        );

        final Agreement agreement2 = Agreement.createAgreement(
                company.getCompanyId(),
                destination.getCompanyId(),
                AgreementType.DELIVERS_WITH,
                Map.of(),
                Set.of(),
                baseDate.plus(60, ChronoUnit.DAYS),
                baseDate.plus(90, ChronoUnit.DAYS)
        );

        Company withAgreements = company.addAgreement(agreement1);
        withAgreements = withAgreements.addAgreement(agreement2);

        final Agreement extendedAgreement = agreement2.updateValidTo(baseDate.plus(120, ChronoUnit.DAYS));

        final Company finalWithAgreements = withAgreements;
        assertThatThrownBy(() -> finalWithAgreements.updateAgreement(agreement2.agreementId(), extendedAgreement))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Update would create overlapping agreement");
    }

    @Test
    @DisplayName("Should check if company has agreement with specific destination and type")
    void shouldCheckHasAgreementWith() {
        final Company company = Company.createCompany(
                "Source Company",
                "10203040506070",
                Set.of(CompanyType.MARKETPLACE),
                Map.of()
        );

        final Company destination = Company.createCompany(
                "Destination Company",
                "70605040302010",
                Set.of(CompanyType.LOGISTICS_PROVIDER),
                Map.of()
        );

        final Agreement agreement = Agreement.createAgreement(
                company.getCompanyId(),
                destination.getCompanyId(),
                AgreementType.DELIVERS_WITH,
                Map.of(),
                Set.of(),
                Instant.now().truncatedTo(ChronoUnit.SECONDS),
                null
        );

        final Company withAgreement = company.addAgreement(agreement);

        assertThat(withAgreement.hasAgreementWith(destination.getCompanyId(), AgreementType.DELIVERS_WITH)).isTrue();
        assertThat(withAgreement.hasAgreementWith(destination.getCompanyId(), AgreementType.OPERATES)).isFalse();
        assertThat(withAgreement.hasAgreementWith(CompanyId.unique(), AgreementType.DELIVERS_WITH)).isFalse();
    }
}
