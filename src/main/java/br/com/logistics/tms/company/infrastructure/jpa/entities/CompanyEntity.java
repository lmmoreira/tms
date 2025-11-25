package br.com.logistics.tms.company.infrastructure.jpa.entities;

import br.com.logistics.tms.commons.domain.Status;
import br.com.logistics.tms.company.domain.*;
import br.com.logistics.tms.company.infrastructure.config.CompanySchema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "company", schema = CompanySchema.COMPANY_SCHEMA)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    public static CompanyEntity of(final Company company) {
        return CompanyEntity.builder()
                .id(company.getCompanyId().value())
                .name(company.getName())
                .cnpj(company.getCnpj().value())
                .companyTypes(new HashSet<>(company.getCompanyTypes().value()))
                .configuration(new HashMap<>(company.getConfigurations().value()))
                .status(company.getStatus().value())
                .version((Integer) company.getPersistentMetadata().getOrDefault("version", null))
                .build();
    }

    public Company toCompany() {
        return new Company(
                CompanyId.with(this.id),
                this.name,
                Cnpj.with(this.cnpj),
                CompanyTypes.with(this.companyTypes),
                Configurations.with(this.configuration),
                Collections.emptySet(),
                Status.of(this.status),
                Collections.emptySet(),
                Map.of("version", this.version)
        );
    }

}