package br.com.logistics.tms.company.infrastructure.jpa.neo4j;

import br.com.logistics.tms.company.domain.Cnpj;
import br.com.logistics.tms.company.domain.Company;
import br.com.logistics.tms.company.domain.CompanyBuilder;
import br.com.logistics.tms.company.domain.CompanyId;
import br.com.logistics.tms.company.domain.Type;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Company")
@Builder
@Data
public class CompanyEntity {

    @Id
    private String id;

    private String name;

    private String cnpj;

    private Set<String> types;

    @Relationship(type = "PARENT_OF")
    private Set<RelationshipEntity> relation;

    public static CompanyEntity of(final Company company) {
        return CompanyEntity.builder()
            .id(company.companyId().value().toString())
            .name(company.name())
            .cnpj(company.cnpj().value())
            .types(company.types().stream().map(Type::toString).collect(Collectors.toSet()))
            .build();
    }

    public Company toCompany() {
        return CompanyBuilder.builder()
            .companyId(CompanyId.with(this.id))
            .name(this.name)
            .cnpj(Cnpj.with(this.cnpj))
            .types(this.types.stream().map(Type::with).collect(
                    Collectors.toSet())).build();
    }

}