package br.com.logistics.tms.assertions.jpa;

import br.com.logistics.tms.company.infrastructure.jpa.entities.AgreementEntity;
import org.assertj.core.api.AbstractAssert;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class AgreementEntityAssert extends AbstractAssert<AgreementEntityAssert, AgreementEntity> {

    private AgreementEntityAssert(final AgreementEntity actual) {
        super(actual, AgreementEntityAssert.class);
    }

    public static AgreementEntityAssert assertThatAgreement(final AgreementEntity actual) {
        return new AgreementEntityAssert(actual);
    }

    public AgreementEntityAssert hasId(final UUID id) {
        isNotNull();
        assertThat(actual.getId())
                .as("Agreement ID")
                .isEqualTo(id);
        return this;
    }

    public AgreementEntityAssert hasFrom(final UUID fromCompanyId) {
        isNotNull();
        assertThat(actual.getSourceId())
                .as("Agreement source company ID")
                .isEqualTo(fromCompanyId);
        return this;
    }

    public AgreementEntityAssert hasTo(final UUID toCompanyId) {
        isNotNull();
        assertThat(actual.getDestinationId())
                .as("Agreement destination company ID")
                .isEqualTo(toCompanyId);
        return this;
    }

    public AgreementEntityAssert hasRelationType(final String relationType) {
        isNotNull();
        assertThat(actual.getRelationType())
                .as("Agreement relation type")
                .isEqualTo(relationType);
        return this;
    }

    public AgreementEntityAssert hasConfiguration(final Map<String, Object> configuration) {
        isNotNull();
        assertThat(actual.getConfiguration())
                .as("Agreement configuration")
                .containsAllEntriesOf(configuration);
        return this;
    }

    public AgreementEntityAssert hasConfigurationEntry(final String key, final Object value) {
        isNotNull();
        assertThat(actual.getConfiguration())
                .as("Agreement configuration")
                .isNotNull()
                .containsEntry(key, value);
        return this;
    }

    public AgreementEntityAssert hasValidFrom(final Instant validFrom) {
        isNotNull();
        assertThat(actual.getValidFrom())
                .as("Agreement valid from")
                .isEqualTo(validFrom);
        return this;
    }

    public AgreementEntityAssert hasValidTo(final Instant validTo) {
        isNotNull();
        assertThat(actual.getValidTo())
                .as("Agreement valid to")
                .isEqualTo(validTo);
        return this;
    }

    public AgreementEntityAssert hasNoValidTo() {
        isNotNull();
        assertThat(actual.getValidTo())
                .as("Agreement valid to")
                .isNull();
        return this;
    }

    public AgreementEntityAssert hasConditions(final int count) {
        isNotNull();
        assertThat(actual.getConditions())
                .as("Agreement conditions")
                .isNotNull()
                .hasSize(count);
        return this;
    }

    public AgreementEntityAssert hasNoConditions() {
        isNotNull();
        assertThat(actual.getConditions())
                .as("Agreement conditions")
                .isNullOrEmpty();
        return this;
    }
}
