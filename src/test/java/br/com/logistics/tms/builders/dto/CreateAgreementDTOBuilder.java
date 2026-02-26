package br.com.logistics.tms.builders.dto;

import br.com.logistics.tms.commons.domain.Id;
import br.com.logistics.tms.company.domain.AgreementCondition;
import br.com.logistics.tms.company.domain.AgreementConditionId;
import br.com.logistics.tms.company.domain.AgreementConditionType;
import br.com.logistics.tms.company.domain.AgreementType;
import br.com.logistics.tms.company.domain.Conditions;
import br.com.logistics.tms.company.infrastructure.dto.CreateAgreementDTO;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CreateAgreementDTOBuilder {

    private UUID destinationCompanyId = Id.unique();
    private AgreementType type = AgreementType.SELLS_ON;
    private Map<String, Object> configuration = new HashMap<>(Map.of(
            "priority", "HIGH",
            "autoRenew", true
    ));
    private Set<AgreementCondition> conditions = Set.of(
            new AgreementCondition(
                    AgreementConditionId.unique(),
                    AgreementConditionType.DISCOUNT_PERCENTAGE,
                    Conditions.with(Map.of("value", 10.0))
            )
    );
    private Instant validFrom = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    private Instant validTo = null;

    public static CreateAgreementDTOBuilder aCreateAgreementDTO() {
        return new CreateAgreementDTOBuilder();
    }

    public CreateAgreementDTOBuilder withDestinationCompanyId(final UUID destinationCompanyId) {
        this.destinationCompanyId = destinationCompanyId;
        return this;
    }

    public CreateAgreementDTOBuilder withType(final AgreementType type) {
        this.type = type;
        return this;
    }

    public CreateAgreementDTOBuilder withConfiguration(final Map<String, Object> configuration) {
        this.configuration = new HashMap<>(configuration);
        return this;
    }

    public CreateAgreementDTOBuilder withConfigurationEntry(final String key, final Object value) {
        this.configuration.put(key, value);
        return this;
    }

    public CreateAgreementDTOBuilder withConditions(final Set<AgreementCondition> conditions) {
        this.conditions = conditions;
        return this;
    }

    public CreateAgreementDTOBuilder withConditions(final AgreementCondition... conditions) {
        this.conditions = Set.of(conditions);
        return this;
    }

    public CreateAgreementDTOBuilder withValidFrom(final Instant validFrom) {
        this.validFrom = validFrom;
        return this;
    }

    public CreateAgreementDTOBuilder withValidTo(final Instant validTo) {
        this.validTo = validTo;
        return this;
    }

    public CreateAgreementDTOBuilder withNoValidTo() {
        this.validTo = null;
        return this;
    }

    public CreateAgreementDTO build() {
        return new CreateAgreementDTO(
                destinationCompanyId,
                type,
                configuration,
                conditions,
                validFrom,
                validTo
        );
    }
}
