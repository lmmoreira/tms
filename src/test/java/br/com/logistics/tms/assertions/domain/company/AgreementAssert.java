package br.com.logistics.tms.assertions.domain.company;

import br.com.logistics.tms.company.domain.Agreement;
import br.com.logistics.tms.company.domain.AgreementCondition;
import br.com.logistics.tms.company.domain.AgreementType;
import br.com.logistics.tms.company.domain.CompanyId;
import org.assertj.core.api.AbstractAssert;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class AgreementAssert extends AbstractAssert<AgreementAssert, Agreement> {

    private AgreementAssert(final Agreement actual) {
        super(actual, AgreementAssert.class);
    }

    public static AgreementAssert assertThatAgreement(final Agreement actual) {
        return new AgreementAssert(actual);
    }

    public AgreementAssert hasAgreementId(final UUID expectedAgreementId) {
        isNotNull();
        assertThat(actual.agreementId().value())
                .as("Agreement ID")
                .isEqualTo(expectedAgreementId);
        return this;
    }

    public AgreementAssert hasFrom(final CompanyId expectedFrom) {
        isNotNull();
        assertThat(actual.from())
                .as("Agreement source company")
                .isEqualTo(expectedFrom);
        return this;
    }

    public AgreementAssert hasFrom(final UUID expectedFromId) {
        isNotNull();
        assertThat(actual.from().value())
                .as("Agreement source company ID")
                .isEqualTo(expectedFromId);
        return this;
    }

    public AgreementAssert hasTo(final CompanyId expectedTo) {
        isNotNull();
        assertThat(actual.to())
                .as("Agreement destination company")
                .isEqualTo(expectedTo);
        return this;
    }

    public AgreementAssert hasTo(final UUID expectedToId) {
        isNotNull();
        assertThat(actual.to().value())
                .as("Agreement destination company ID")
                .isEqualTo(expectedToId);
        return this;
    }

    public AgreementAssert hasType(final AgreementType expectedType) {
        isNotNull();
        assertThat(actual.type())
                .as("Agreement type")
                .isEqualTo(expectedType);
        return this;
    }

    public AgreementAssert hasConfigurationEntry(final String key, final Object value) {
        isNotNull();
        final Map<String, Object> configurations = actual.configurations().value();
        assertThat(configurations)
                .as("Agreement configurations")
                .containsEntry(key, value);
        return this;
    }

    public AgreementAssert hasConfigurationKey(final String key) {
        isNotNull();
        final Map<String, Object> configurations = actual.configurations().value();
        assertThat(configurations)
                .as("Agreement configurations")
                .containsKey(key);
        return this;
    }

    public AgreementAssert doesNotHaveConfigurationKey(final String key) {
        isNotNull();
        final Map<String, Object> configurations = actual.configurations().value();
        assertThat(configurations)
                .as("Agreement configurations")
                .doesNotContainKey(key);
        return this;
    }

    public AgreementAssert hasConditionsCount(final int expectedCount) {
        isNotNull();
        assertThat(actual.conditions())
                .as("Agreement conditions count")
                .hasSize(expectedCount);
        return this;
    }

    public AgreementAssert hasCondition(final AgreementCondition expectedCondition) {
        isNotNull();
        assertThat(actual.conditions())
                .as("Agreement conditions")
                .contains(expectedCondition);
        return this;
    }

    public AgreementAssert hasEmptyConditions() {
        isNotNull();
        assertThat(actual.conditions())
                .as("Agreement conditions")
                .isEmpty();
        return this;
    }

    public AgreementAssert hasValidFrom(final Instant expectedValidFrom) {
        isNotNull();
        assertThat(actual.validFrom())
                .as("Agreement validFrom")
                .isEqualTo(expectedValidFrom);
        return this;
    }

    public AgreementAssert hasValidTo(final Instant expectedValidTo) {
        isNotNull();
        assertThat(actual.validTo())
                .as("Agreement validTo")
                .isEqualTo(expectedValidTo);
        return this;
    }

    public AgreementAssert hasNoValidTo() {
        isNotNull();
        assertThat(actual.validTo())
                .as("Agreement validTo")
                .isNull();
        return this;
    }

    public AgreementAssert isActive() {
        isNotNull();
        assertThat(actual.isActive())
                .as("Agreement is active")
                .isTrue();
        return this;
    }

    public AgreementAssert isNotActive() {
        isNotNull();
        assertThat(actual.isActive())
                .as("Agreement is not active")
                .isFalse();
        return this;
    }

    public AgreementAssert isValidOn(final Instant date) {
        isNotNull();
        assertThat(actual.isValidOn(date))
                .as("Agreement is valid on %s", date)
                .isTrue();
        return this;
    }

    public AgreementAssert isNotValidOn(final Instant date) {
        isNotNull();
        assertThat(actual.isValidOn(date))
                .as("Agreement is not valid on %s", date)
                .isFalse();
        return this;
    }

    public AgreementAssert isBetween(final CompanyId from, final CompanyId to) {
        isNotNull();
        assertThat(actual.isBetween(from, to))
                .as("Agreement is between %s and %s", from, to)
                .isTrue();
        return this;
    }

    public AgreementAssert isNotBetween(final CompanyId from, final CompanyId to) {
        isNotNull();
        assertThat(actual.isBetween(from, to))
                .as("Agreement is not between %s and %s", from, to)
                .isFalse();
        return this;
    }

    public AgreementAssert overlapsWith(final Agreement other) {
        isNotNull();
        assertThat(actual.overlapsWith(other))
                .as("Agreement overlaps with other agreement")
                .isTrue();
        return this;
    }

    public AgreementAssert doesNotOverlapWith(final Agreement other) {
        isNotNull();
        assertThat(actual.overlapsWith(other))
                .as("Agreement does not overlap with other agreement")
                .isFalse();
        return this;
    }
}
