package br.com.logistics.tms.builders.domain.company;

import br.com.logistics.tms.company.domain.*;

import java.time.Instant;
import java.util.*;

public class AgreementBuilder {

    private CompanyId from = CompanyId.unique();
    private CompanyId to = CompanyId.unique();
    private AgreementType type = AgreementType.DELIVERS_WITH;
    private Map<String, Object> configuration = new HashMap<>();
    private Set<AgreementCondition> conditions = new HashSet<>();
    private Instant validFrom = Instant.now();
    private Instant validTo = null;

    public static AgreementBuilder anAgreement() {
        return new AgreementBuilder();
    }

    public AgreementBuilder withFrom(final CompanyId from) {
        this.from = from;
        return this;
    }

    public AgreementBuilder withFrom(final UUID fromId) {
        this.from = CompanyId.with(fromId);
        return this;
    }

    public AgreementBuilder withTo(final CompanyId to) {
        this.to = to;
        return this;
    }

    public AgreementBuilder withTo(final UUID toId) {
        this.to = CompanyId.with(toId);
        return this;
    }

    public AgreementBuilder withType(final AgreementType type) {
        this.type = type;
        return this;
    }

    public AgreementBuilder withConfiguration(final Map<String, Object> configuration) {
        this.configuration = new HashMap<>(configuration);
        return this;
    }

    public AgreementBuilder withConfigurationEntry(final String key, final Object value) {
        this.configuration.put(key, value);
        return this;
    }

    public AgreementBuilder withConditions(final Set<AgreementCondition> conditions) {
        this.conditions = new HashSet<>(conditions);
        return this;
    }

    public AgreementBuilder withCondition(final AgreementCondition condition) {
        this.conditions.add(condition);
        return this;
    }

    public AgreementBuilder withValidFrom(final Instant validFrom) {
        this.validFrom = validFrom;
        return this;
    }

    public AgreementBuilder withValidTo(final Instant validTo) {
        this.validTo = validTo;
        return this;
    }

    public AgreementBuilder withNoValidTo() {
        this.validTo = null;
        return this;
    }

    public Agreement build() {
        if (configuration.isEmpty()) {
            configuration.put("default", true);
        }
        return Agreement.createAgreement(from, to, type, configuration, conditions, validFrom, validTo);
    }
}
