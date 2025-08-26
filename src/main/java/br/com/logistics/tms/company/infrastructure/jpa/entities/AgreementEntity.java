package br.com.logistics.tms.company.infrastructure.jpa.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;
import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "agreement", schema = "company")
public class AgreementEntity {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "from", nullable = false)
    private CompanyEntity from;

    @ManyToOne
    @JoinColumn(name = "to", nullable = false)
    private CompanyEntity to;

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
}