package br.com.logistics.tms.company.infrastructure.jpa.entities;

import br.com.logistics.tms.commons.domain.Status;
import br.com.logistics.tms.company.domain.*;
import br.com.logistics.tms.company.infrastructure.config.CompanySchema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "company", schema = CompanySchema.COMPANY_SCHEMA)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"agreements"})
public class CompanyEntity implements Serializable {

    @Id
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "cnpj", nullable = false)
    private String cnpj;

    @Column(name = "status", nullable = false, columnDefinition = "CHAR(1) DEFAULT 'A'")
    private Character status;

    @Version
    private Integer version;

    @ElementCollection(targetClass = CompanyType.class)
    @CollectionTable(
            name = "company_type",
            schema = "company",
            joinColumns = @JoinColumn(name = "company_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private Set<CompanyType> companyTypes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "configuration")
    private Map<String, Object> configuration;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "source", nullable = false)
    private Set<AgreementEntity> agreements = new HashSet<>();

    public static CompanyEntity of(final Company company) {
        final CompanyEntity entity = CompanyEntity.builder()
                .id(company.getCompanyId().value())
                .name(company.getName())
                .cnpj(company.getCnpj().value())
                .companyTypes(new HashSet<>(company.getCompanyTypes().value()))
                .configuration(new HashMap<>(company.getConfigurations().value()))
                .status(company.getStatus().value())
                .version((Integer) company.getPersistentMetadata().getOrDefault("version", null))
                .build();

        final Set<AgreementEntity> agreementEntities = company.getAgreements().stream()
                .map(agreement -> AgreementEntity.of(agreement, entity.getId()))
                .collect(java.util.stream.Collectors.toSet());
        entity.setAgreements(agreementEntities);

        return entity;
    }

    public Company toCompany() {
        final Set<Agreement> agreements = this.agreements == null ? Collections.emptySet() :
                this.agreements.stream()
                        .map(AgreementEntity::toAgreement)
                        .collect(java.util.stream.Collectors.toSet());

        return new Company(
                CompanyId.with(this.id),
                this.name,
                Cnpj.with(this.cnpj),
                CompanyTypes.with(this.companyTypes),
                Configurations.with(this.configuration),
                agreements,
                Status.of(this.status),
                Collections.emptySet(),
                Map.of("version", this.version)
        );
    }

}