package br.com.logistics.tms.company.infrastructure.jpa.entities;


import br.com.logistics.tms.company.domain.AgreementCondition;
import br.com.logistics.tms.company.domain.AgreementConditionId;
import br.com.logistics.tms.company.domain.AgreementConditionType;
import br.com.logistics.tms.company.domain.Conditions;
import br.com.logistics.tms.company.infrastructure.config.CompanySchema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "agreement_condition", schema = CompanySchema.COMPANY_SCHEMA)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"agreement"})
public class AgreementConditionEntity {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "agreement_id", nullable = false)
    private AgreementEntity agreement;

    @Column(name = "condition_type", nullable = false)
    private String conditionType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "conditions")
    private Map<String, Object> conditions;

    public static AgreementConditionEntity of(final AgreementCondition condition, final AgreementEntity agreementEntity) {
        return AgreementConditionEntity.builder()
                .id(condition.agreementConditionId().value())
                .agreement(agreementEntity)
                .conditionType(condition.conditionType().name())
                .conditions(new HashMap<>(condition.conditions().value()))
                .build();
    }

    public AgreementCondition toAgreementCondition() {
        return new AgreementCondition(
                AgreementConditionId.with(this.id),
                AgreementConditionType.valueOf(this.conditionType),
                Conditions.with(this.conditions)
        );
    }
}