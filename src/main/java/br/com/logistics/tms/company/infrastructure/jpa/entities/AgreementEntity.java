package br.com.logistics.tms.company.infrastructure.jpa.entities;

import br.com.logistics.tms.company.domain.*;
import br.com.logistics.tms.company.infrastructure.config.CompanySchema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "agreement", schema = CompanySchema.COMPANY_SCHEMA)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"conditions"})
public class AgreementEntity {

    @Id
    private UUID id;

    @Column(name = "source", nullable = false, insertable = false, updatable = false)
    private UUID sourceId;

    @Column(name = "destination", nullable = false)
    private UUID destinationId;

    @Column(name = "relation_type", nullable = false)
    private String relationType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "configuration")
    private Map<String, Object> configuration;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_to")
    private Instant validTo;

    @OneToMany(mappedBy = "agreement", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<AgreementConditionEntity> conditions;

    public static AgreementEntity of(final Agreement agreement, final UUID sourceCompanyId) {
        final AgreementEntity entity = AgreementEntity.builder()
                .id(agreement.agreementId().value())
                .sourceId(sourceCompanyId)
                .destinationId(agreement.to().value())
                .relationType(agreement.type().name())
                .configuration(new HashMap<>(agreement.configurations().value()))
                .validFrom(agreement.validFrom())
                .validTo(agreement.validTo())
                .build();

        final Set<AgreementConditionEntity> conditionEntities = agreement.conditions().stream()
                .map(condition -> AgreementConditionEntity.of(condition, entity))
                .collect(Collectors.toSet());
        entity.setConditions(conditionEntities);

        return entity;
    }

    public Agreement toAgreement() {
        final Set<AgreementCondition> conditions = this.conditions == null ? Set.of() :
                this.conditions.stream()
                        .map(AgreementConditionEntity::toAgreementCondition)
                        .collect(Collectors.toSet());

        return new Agreement(
                AgreementId.with(this.id),
                CompanyId.with(this.sourceId),
                CompanyId.with(this.destinationId),
                AgreementType.valueOf(this.relationType),
                Configurations.with(this.configuration),
                conditions,
                this.validFrom,
                this.validTo
        );
    }
}