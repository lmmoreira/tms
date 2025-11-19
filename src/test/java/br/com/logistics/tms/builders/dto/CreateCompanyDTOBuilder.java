package br.com.logistics.tms.builders.dto;

import br.com.logistics.tms.utils.CnpjGenerator;

import br.com.logistics.tms.utils.CnpjGenerator;
import br.com.logistics.tms.company.domain.CompanyType;
import br.com.logistics.tms.company.infrastructure.dto.CreateCompanyDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CreateCompanyDTOBuilder {

    private String name = "Default Company";
    private String cnpj = CnpjGenerator.randomCnpj();
    private Set<CompanyType> types = Set.of(CompanyType.SELLER);
    private Map<String, Object> configuration = new HashMap<>(Map.of(
            "notification", true
    ));

    public static CreateCompanyDTOBuilder aCreateCompanyDTO() {
        return new CreateCompanyDTOBuilder();
    }

    public CreateCompanyDTOBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public CreateCompanyDTOBuilder withCnpj(final String cnpj) {
        this.cnpj = cnpj;
        return this;
    }

    public CreateCompanyDTOBuilder withTypes(final Set<CompanyType> types) {
        this.types = types;
        return this;
    }

    public CreateCompanyDTOBuilder withTypes(final CompanyType... types) {
        this.types = Set.of(types);
        return this;
    }

    public CreateCompanyDTOBuilder withConfiguration(final Map<String, Object> configuration) {
        this.configuration = new HashMap<>(configuration);
        return this;
    }

    public CreateCompanyDTOBuilder withConfigurationEntry(final String key, final Object value) {
        this.configuration.put(key, value);
        return this;
    }

    public CreateCompanyDTO build() {
        return new CreateCompanyDTO(name, cnpj, types, configuration);
    }
}
