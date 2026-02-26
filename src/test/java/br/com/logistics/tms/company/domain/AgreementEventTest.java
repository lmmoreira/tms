package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.AbstractTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Agreement Domain Event Tests")
class AgreementEventTest extends AbstractTestBase {

    @Test
    @DisplayName("Should create AgreementAdded event with correct data")
    void shouldCreateAgreementAddedEvent() {
        final UUID sourceCompanyId = UUID.randomUUID();
        final UUID agreementId = UUID.randomUUID();
        final UUID destinationCompanyId = UUID.randomUUID();
        final String agreementType = "DELIVERS_WITH";

        final AgreementAdded event = new AgreementAdded(
                sourceCompanyId,
                agreementId,
                destinationCompanyId,
                agreementType
        );

        assertThat(event.getSourceCompanyId()).isEqualTo(sourceCompanyId);
        assertThat(event.getAgreementId()).isEqualTo(agreementId);
        assertThat(event.getDestinationCompanyId()).isEqualTo(destinationCompanyId);
        assertThat(event.getAgreementType()).isEqualTo(agreementType);
        assertThat(event.getAggregateId()).isEqualTo(sourceCompanyId);
        assertThat(event.getDomainEventId()).isNotNull();
        assertThat(event.getOccurredOn()).isNotNull();
    }

    @Test
    @DisplayName("Should create AgreementRemoved event with correct data")
    void shouldCreateAgreementRemovedEvent() {
        final UUID sourceCompanyId = UUID.randomUUID();
        final UUID agreementId = UUID.randomUUID();
        final UUID destinationCompanyId = UUID.randomUUID();

        final AgreementRemoved event = new AgreementRemoved(
                sourceCompanyId,
                agreementId,
                destinationCompanyId
        );

        assertThat(event.getSourceCompanyId()).isEqualTo(sourceCompanyId);
        assertThat(event.getAgreementId()).isEqualTo(agreementId);
        assertThat(event.getDestinationCompanyId()).isEqualTo(destinationCompanyId);
        assertThat(event.getAggregateId()).isEqualTo(sourceCompanyId);
        assertThat(event.getDomainEventId()).isNotNull();
        assertThat(event.getOccurredOn()).isNotNull();
    }

    @Test
    @DisplayName("Should create AgreementUpdated event with correct data")
    void shouldCreateAgreementUpdatedEvent() {
        final UUID sourceCompanyId = UUID.randomUUID();
        final UUID agreementId = UUID.randomUUID();
        final String fieldChanged = "validTo";
        final String oldValue = "2026-01-01T00:00:00Z";
        final String newValue = "2026-12-31T23:59:59Z";

        final AgreementUpdated event = new AgreementUpdated(
                sourceCompanyId,
                agreementId,
                fieldChanged,
                oldValue,
                newValue
        );

        assertThat(event.getSourceCompanyId()).isEqualTo(sourceCompanyId);
        assertThat(event.getAgreementId()).isEqualTo(agreementId);
        assertThat(event.getFieldChanged()).isEqualTo(fieldChanged);
        assertThat(event.getOldValue()).isEqualTo(oldValue);
        assertThat(event.getNewValue()).isEqualTo(newValue);
        assertThat(event.getAggregateId()).isEqualTo(sourceCompanyId);
        assertThat(event.getDomainEventId()).isNotNull();
        assertThat(event.getOccurredOn()).isNotNull();
    }

    @Test
    @DisplayName("Should verify AgreementAdded event is placed correctly in aggregate")
    void shouldVerifyAgreementAddedEventInAggregate() {
        final Company company = Company.createCompany(
                "Test Company",
                "12.345.678/9012-34",
                java.util.Set.of(CompanyType.MARKETPLACE),
                java.util.Map.of("test", "value")
        );

        final Company destination = Company.createCompany(
                "Destination Company",
                "98.765.432/1098-76",
                java.util.Set.of(CompanyType.LOGISTICS_PROVIDER),
                java.util.Map.of("test", "value")
        );

        final Agreement agreement = Agreement.createAgreement(
                company.getCompanyId(),
                destination.getCompanyId(),
                AgreementType.DELIVERS_WITH,
                java.util.Map.of("test", "value"),
                java.util.Set.of(),
                java.time.Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS),
                null
        );

        final Company updated = company.addAgreement(agreement);

        assertThat(updated.getDomainEvents())
                .isNotEmpty()
                .anyMatch(event -> event instanceof AgreementAdded);
    }

    @Test
    @DisplayName("Should verify AgreementRemoved event is placed correctly in aggregate")
    void shouldVerifyAgreementRemovedEventInAggregate() {
        final Company company = Company.createCompany(
                "Test Company",
                "11.111.111/1111-11",
                java.util.Set.of(CompanyType.MARKETPLACE),
                java.util.Map.of("test", "value")
        );

        final Company destination = Company.createCompany(
                "Destination Company",
                "22.222.222/2222-22",
                java.util.Set.of(CompanyType.LOGISTICS_PROVIDER),
                java.util.Map.of("test", "value")
        );

        final Agreement agreement = Agreement.createAgreement(
                company.getCompanyId(),
                destination.getCompanyId(),
                AgreementType.DELIVERS_WITH,
                java.util.Map.of("test", "value"),
                java.util.Set.of(),
                java.time.Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS),
                null
        );

        final Company withAgreement = company.addAgreement(agreement);
        final Company updated = withAgreement.removeAgreement(agreement.agreementId());

        assertThat(updated.getDomainEvents())
                .anyMatch(event -> event instanceof AgreementRemoved);
    }

    @Test
    @DisplayName("Should verify AgreementUpdated event is placed correctly in aggregate")
    void shouldVerifyAgreementUpdatedEventInAggregate() {
        final Company company = Company.createCompany(
                "Test Company",
                "33.333.333/3333-33",
                java.util.Set.of(CompanyType.MARKETPLACE),
                java.util.Map.of("test", "value")
        );

        final Company destination = Company.createCompany(
                "Destination Company",
                "44.444.444/4444-44",
                java.util.Set.of(CompanyType.LOGISTICS_PROVIDER),
                java.util.Map.of("test", "value")
        );

        final java.time.Instant validFrom = java.time.Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        final Agreement agreement = Agreement.createAgreement(
                company.getCompanyId(),
                destination.getCompanyId(),
                AgreementType.DELIVERS_WITH,
                java.util.Map.of("test", "value"),
                java.util.Set.of(),
                validFrom,
                null
        );

        final Company withAgreement = company.addAgreement(agreement);

        final java.time.Instant newValidTo = validFrom.plus(365, java.time.temporal.ChronoUnit.DAYS);
        final Agreement updatedAgreement = agreement.updateValidTo(newValidTo);

        final Company updated = withAgreement.updateAgreement(agreement.agreementId(), updatedAgreement);

        assertThat(updated.getDomainEvents())
                .anyMatch(event -> event instanceof AgreementUpdated);
    }
}
