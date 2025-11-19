package br.com.logistics.tms.integration.data;

import br.com.logistics.tms.company.domain.CompanyType;
import br.com.logistics.tms.company.infrastructure.dto.CreateCompanyDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CreateCompanyDTODataBuilder {

    private String name = "Default Company";
    private String cnpj = CnpjGenerator.randomCnpj();
    private Set<CompanyType> types = Set.of(CompanyType.SELLER);
    private Map<String, Object> configuration = new HashMap<>(Map.of(
            "notification", true
    ));

    public static CreateCompanyDTODataBuilder aCreateCompanyDTO() {
        return new CreateCompanyDTODataBuilder();
    }

    public CreateCompanyDTODataBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public CreateCompanyDTODataBuilder withCnpj(final String cnpj) {
        this.cnpj = cnpj;
        return this;
    }

    public CreateCompanyDTODataBuilder withTypes(final Set<CompanyType> types) {
        this.types = types;
        return this;
    }

    public CreateCompanyDTODataBuilder withTypes(final CompanyType... types) {
        this.types = Set.of(types);
        return this;
    }

    public CreateCompanyDTODataBuilder withConfiguration(final Map<String, Object> configuration) {
        this.configuration = new HashMap<>(configuration);
        return this;
    }

    public CreateCompanyDTODataBuilder withConfigurationEntry(final String key, final Object value) {
        this.configuration.put(key, value);
        return this;
    }

    public CreateCompanyDTO build() {
        return new CreateCompanyDTO(name, cnpj, types, configuration);
    }
}
