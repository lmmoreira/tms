package br.com.logistics.tms.company.infrastructure.jpa.entities;

import br.com.logistics.tms.company.domain.*;
import br.com.logistics.tms.company.infrastructure.config.CompanySchema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.*;

@Entity
@Table(name = "company", schema = CompanySchema.COMPANY_SCHEMA)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyEntity {

    @Id
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "cnpj", nullable = false)
    private String cnpj;

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
        return new CompanyEntity(
                company.getCompanyId().value(),
                company.getName(),
                company.getCnpj().value(),
                new HashSet<>(company.getCompanyTypes().value()),
                new HashMap<>(company.getConfigurations().value())
        );
    }

    public Company toCompany() {
        return new Company(
                CompanyId.with(this.id),
                this.name,
                Cnpj.with(this.cnpj),
                CompanyTypes.with(this.companyTypes),
                Configurations.with(this.configuration),
                Collections.emptySet(),
                Collections.emptySet()
        );
    }

}