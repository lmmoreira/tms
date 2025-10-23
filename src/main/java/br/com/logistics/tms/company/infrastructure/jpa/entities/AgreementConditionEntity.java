package br.com.logistics.tms.company.infrastructure.jpa.entities;


import br.com.logistics.tms.company.infrastructure.config.CompanySchema;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "agreement_condition", schema = CompanySchema.COMPANY_SCHEMA)
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
}