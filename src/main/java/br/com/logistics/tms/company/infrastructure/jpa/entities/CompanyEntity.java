package br.com.logistics.tms.company.infrastructure.jpa.entities;

import br.com.logistics.tms.company.domain.Cnpj;
import br.com.logistics.tms.company.domain.Company;
import br.com.logistics.tms.company.domain.CompanyId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "company", schema = "company")
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class CompanyEntity {

    @Id
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "cnpj", nullable = false)
    private String cnpj;

    public static CompanyEntity of(final Company company) {
        return CompanyEntity.builder()
            .id(company.companyId().value())
            .name(company.name())
            .cnpj(company.cnpj().value()).build();
    }

    public Company toCompany() {
        return new Company(CompanyId.with(this.id), name, Cnpj.with(cnpj));
    }

}