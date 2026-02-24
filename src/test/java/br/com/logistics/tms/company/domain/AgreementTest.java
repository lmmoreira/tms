package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Agreement Domain Tests")
class AgreementTest {

    @Test
    @DisplayName("Should create agreement via factory method")
    void shouldCreateAgreementViaFactory() {
        final CompanyId from = CompanyId.unique();
        final CompanyId to = CompanyId.unique();
        final Map<String, Object> config = Map.of("priority", "high");
        final Map<String, Object> conditionData = Map.of("percentage", 10.0);
        final AgreementCondition condition = new AgreementCondition(
                AgreementConditionId.unique(),
                AgreementConditionType.DISCOUNT_PERCENTAGE,
                Conditions.with(conditionData)
        );
        final Instant validFrom = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        final Agreement agreement = Agreement.createAgreement(
                from,
                to,
                AgreementType.DELIVERS_WITH,
                config,
                Set.of(condition),
                validFrom,
                null
        );

        assertThat(agreement.agreementId()).isNotNull();
        assertThat(agreement.from()).isEqualTo(from);
        assertThat(agreement.to()).isEqualTo(to);
        assertThat(agreement.type()).isEqualTo(AgreementType.DELIVERS_WITH);
        assertThat(agreement.validFrom()).isEqualTo(validFrom);
        assertThat(agreement.validTo()).isNull();
        assertThat(agreement.conditions()).hasSize(1);
    }

    @Test
    @DisplayName("Should reject self-reference in factory method")
    void shouldRejectSelfReferenceInFactory() {
        final CompanyId companyId = CompanyId.unique();
        final Map<String, Object> config = Map.of();
        final Instant validFrom = Instant.now();

        assertThatThrownBy(() -> Agreement.createAgreement(
                companyId,
                companyId,
                AgreementType.DELIVERS_WITH,
                config,
                Set.of(),
                validFrom,
                null
        ))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("source and destination must be different");
    }

    @Test
    @DisplayName("Should validate required fields in constructor")
    void shouldValidateRequiredFieldsInConstructor() {
        final CompanyId from = CompanyId.unique();
        final CompanyId to = CompanyId.unique();
        final Instant validFrom = Instant.now();

        assertThatThrownBy(() -> new Agreement(
                null,
                from,
                to,
                AgreementType.DELIVERS_WITH,
                Configurations.with(Map.of()),
                Set.of(),
                validFrom,
                null
        ))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid agreementId");

        assertThatThrownBy(() -> new Agreement(
                AgreementId.unique(),
                null,
                to,
                AgreementType.DELIVERS_WITH,
                Configurations.with(Map.of()),
                Set.of(),
                validFrom,
                null
        ))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid from");

        assertThatThrownBy(() -> new Agreement(
                AgreementId.unique(),
                from,
                null,
                AgreementType.DELIVERS_WITH,
                Configurations.with(Map.of()),
                Set.of(),
                validFrom,
                null
        ))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid to");
    }

    @Test
    @DisplayName("Should validate date range - validFrom before validTo")
    void shouldValidateDateRange() {
        final CompanyId from = CompanyId.unique();
        final CompanyId to = CompanyId.unique();
        final Instant validFrom = Instant.now().plus(10, ChronoUnit.DAYS);
        final Instant validTo = Instant.now();

        assertThatThrownBy(() -> new Agreement(
                AgreementId.unique(),
                from,
                to,
                AgreementType.DELIVERS_WITH,
                Configurations.with(Map.of()),
                Set.of(),
                validFrom,
                validTo
        ))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("validFrom must be before validTo");
    }

    @Test
    @DisplayName("Should update validTo and return new instance")
    void shouldUpdateValidToImmutably() {
        final CompanyId from = CompanyId.unique();
        final CompanyId to = CompanyId.unique();
        final Instant validFrom = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        final Agreement original = Agreement.createAgreement(
                from,
                to,
                AgreementType.DELIVERS_WITH,
                Map.of(),
                Set.of(),
                validFrom,
                null
        );

        final Instant newValidTo = validFrom.plus(30, ChronoUnit.DAYS);
        final Agreement updated = original.updateValidTo(newValidTo);

        assertThat(updated).isNotSameAs(original);
        assertThat(updated.validTo()).isEqualTo(newValidTo);
        assertThat(updated.agreementId()).isEqualTo(original.agreementId());
        assertThat(original.validTo()).isNull();
    }

    @Test
    @DisplayName("Should reject updateValidTo with date before validFrom")
    void shouldRejectInvalidValidToUpdate() {
        final CompanyId from = CompanyId.unique();
        final CompanyId to = CompanyId.unique();
        final Instant validFrom = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        final Agreement agreement = Agreement.createAgreement(
                from,
                to,
                AgreementType.DELIVERS_WITH,
                Map.of(),
                Set.of(),
                validFrom,
                null
        );

        final Instant invalidValidTo = validFrom.minus(1, ChronoUnit.DAYS);

        assertThatThrownBy(() -> agreement.updateValidTo(invalidValidTo))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Valid to must be after valid from");
    }

    @Test
    @DisplayName("Should update conditions and return new instance")
    void shouldUpdateConditionsImmutably() {
        final CompanyId from = CompanyId.unique();
        final CompanyId to = CompanyId.unique();
        final Instant validFrom = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        final AgreementCondition oldCondition = new AgreementCondition(
                AgreementConditionId.unique(),
                AgreementConditionType.DISCOUNT_PERCENTAGE,
                Conditions.with(Map.of("percentage", 10.0))
        );
        final Agreement original = Agreement.createAgreement(
                from,
                to,
                AgreementType.DELIVERS_WITH,
                Map.of(),
                Set.of(oldCondition),
                validFrom,
                null
        );

        final AgreementCondition newCondition = new AgreementCondition(
                AgreementConditionId.unique(),
                AgreementConditionType.DELIVERY_SLA_DAYS,
                Conditions.with(Map.of("maxDays", 3))
        );
        final Agreement updated = original.updateConditions(Set.of(newCondition));

        assertThat(updated).isNotSameAs(original);
        assertThat(updated.conditions()).hasSize(1);
        assertThat(updated.conditions()).contains(newCondition);
        assertThat(updated.conditions()).doesNotContain(oldCondition);
        assertThat(original.conditions()).contains(oldCondition);
    }

    @Test
    @DisplayName("Should reject updateConditions with empty set")
    void shouldRejectEmptyConditionsUpdate() {
        final CompanyId from = CompanyId.unique();
        final CompanyId to = CompanyId.unique();
        final Instant validFrom = Instant.now();
        final Agreement agreement = Agreement.createAgreement(
                from,
                to,
                AgreementType.DELIVERS_WITH,
                Map.of(),
                Set.of(),
                validFrom,
                null
        );

        assertThatThrownBy(() -> agreement.updateConditions(Set.of()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Agreement must have at least one condition");
    }

    @Test
    @DisplayName("Should detect overlap with same destination and type")
    void shouldDetectOverlap() {
        final CompanyId from = CompanyId.unique();
        final CompanyId to = CompanyId.unique();
        final Instant baseDate = Instant.parse("2026-01-01T00:00:00Z");

        final Agreement agreement1 = Agreement.createAgreement(
                from,
                to,
                AgreementType.DELIVERS_WITH,
                Map.of(),
                Set.of(),
                baseDate,
                baseDate.plus(30, ChronoUnit.DAYS)
        );

        final Agreement agreement2 = Agreement.createAgreement(
                from,
                to,
                AgreementType.DELIVERS_WITH,
                Map.of(),
                Set.of(),
                baseDate.plus(15, ChronoUnit.DAYS),
                baseDate.plus(45, ChronoUnit.DAYS)
        );

        assertThat(agreement1.overlapsWith(agreement2)).isTrue();
        assertThat(agreement2.overlapsWith(agreement1)).isTrue();
    }

    @Test
    @DisplayName("Should NOT detect overlap with different destination")
    void shouldNotDetectOverlapWithDifferentDestination() {
        final CompanyId from = CompanyId.unique();
        final CompanyId to1 = CompanyId.unique();
        final CompanyId to2 = CompanyId.unique();
        final Instant baseDate = Instant.parse("2026-01-01T00:00:00Z");

        final Agreement agreement1 = Agreement.createAgreement(
                from,
                to1,
                AgreementType.DELIVERS_WITH,
                Map.of(),
                Set.of(),
                baseDate,
                baseDate.plus(30, ChronoUnit.DAYS)
        );

        final Agreement agreement2 = Agreement.createAgreement(
                from,
                to2,
                AgreementType.DELIVERS_WITH,
                Map.of(),
                Set.of(),
                baseDate.plus(15, ChronoUnit.DAYS),
                baseDate.plus(45, ChronoUnit.DAYS)
        );

        assertThat(agreement1.overlapsWith(agreement2)).isFalse();
    }

    @Test
    @DisplayName("Should NOT detect overlap with different type")
    void shouldNotDetectOverlapWithDifferentType() {
        final CompanyId from = CompanyId.unique();
        final CompanyId to = CompanyId.unique();
        final Instant baseDate = Instant.parse("2026-01-01T00:00:00Z");

        final Agreement agreement1 = Agreement.createAgreement(
                from,
                to,
                AgreementType.DELIVERS_WITH,
                Map.of(),
                Set.of(),
                baseDate,
                baseDate.plus(30, ChronoUnit.DAYS)
        );

        final Agreement agreement2 = Agreement.createAgreement(
                from,
                to,
                AgreementType.OPERATES,
                Map.of(),
                Set.of(),
                baseDate.plus(15, ChronoUnit.DAYS),
                baseDate.plus(45, ChronoUnit.DAYS)
        );

        assertThat(agreement1.overlapsWith(agreement2)).isFalse();
    }

    @Test
    @DisplayName("Should detect overlap with open-ended validTo")
    void shouldDetectOverlapWithOpenEndedValidTo() {
        final CompanyId from = CompanyId.unique();
        final CompanyId to = CompanyId.unique();
        final Instant baseDate = Instant.parse("2026-01-01T00:00:00Z");

        final Agreement agreement1 = Agreement.createAgreement(
                from,
                to,
                AgreementType.DELIVERS_WITH,
                Map.of(),
                Set.of(),
                baseDate,
                null
        );

        final Agreement agreement2 = Agreement.createAgreement(
                from,
                to,
                AgreementType.DELIVERS_WITH,
                Map.of(),
                Set.of(),
                baseDate.plus(100, ChronoUnit.DAYS),
                baseDate.plus(200, ChronoUnit.DAYS)
        );

        assertThat(agreement1.overlapsWith(agreement2)).isTrue();
    }

    @Test
    @DisplayName("Should check if agreement is active")
    void shouldCheckIfActive() {
        final CompanyId from = CompanyId.unique();
        final CompanyId to = CompanyId.unique();
        final Instant past = Instant.now().minus(10, ChronoUnit.DAYS);
        final Instant future = Instant.now().plus(10, ChronoUnit.DAYS);

        final Agreement activeAgreement = Agreement.createAgreement(
                from,
                to,
                AgreementType.DELIVERS_WITH,
                Map.of(),
                Set.of(),
                past,
                future
        );

        assertThat(activeAgreement.isActive()).isTrue();

        final Agreement expiredAgreement = Agreement.createAgreement(
                from,
                to,
                AgreementType.DELIVERS_WITH,
                Map.of(),
                Set.of(),
                past.minus(20, ChronoUnit.DAYS),
                past.minus(1, ChronoUnit.DAYS)
        );

        assertThat(expiredAgreement.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should check if valid on specific date")
    void shouldCheckIfValidOnDate() {
        final CompanyId from = CompanyId.unique();
        final CompanyId to = CompanyId.unique();
        final Instant start = Instant.parse("2026-01-01T00:00:00Z");
        final Instant end = Instant.parse("2026-12-31T23:59:59Z");

        final Agreement agreement = Agreement.createAgreement(
                from,
                to,
                AgreementType.DELIVERS_WITH,
                Map.of(),
                Set.of(),
                start,
                end
        );

        assertThat(agreement.isValidOn(Instant.parse("2026-06-15T12:00:00Z"))).isTrue();
        assertThat(agreement.isValidOn(Instant.parse("2025-12-31T23:59:59Z"))).isFalse();
        assertThat(agreement.isValidOn(Instant.parse("2027-01-01T00:00:01Z"))).isFalse();
    }
}
