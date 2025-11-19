package br.com.logistics.tms.builders.dto;

import br.com.logistics.tms.company.domain.CompanyType;
import br.com.logistics.tms.company.infrastructure.dto.UpdateCompanyDTO;
import br.com.logistics.tms.utils.CnpjGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UpdateCompanyDTOBuilder {

    private String name = "Updated Company";
    private String cnpj = CnpjGenerator.randomCnpj();
    private Set<CompanyType> types = Set.of(CompanyType.SELLER);
    private Map<String, Object> configuration = new HashMap<>(Map.of(
            "notification", true
    ));

    public static UpdateCompanyDTOBuilder anUpdateCompanyDTO() {
        return new UpdateCompanyDTOBuilder();
    }

    public UpdateCompanyDTOBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public UpdateCompanyDTOBuilder withCnpj(final String cnpj) {
        this.cnpj = cnpj;
        return this;
    }

    public UpdateCompanyDTOBuilder withTypes(final Set<CompanyType> types) {
        this.types = types;
        return this;
    }

    public UpdateCompanyDTOBuilder withTypes(final CompanyType... types) {
        this.types = Set.of(types);
        return this;
    }

    public UpdateCompanyDTOBuilder withConfiguration(final Map<String, Object> configuration) {
        this.configuration = new HashMap<>(configuration);
        return this;
    }

    public UpdateCompanyDTOBuilder withConfigurationEntry(final String key, final Object value) {
        this.configuration.put(key, value);
        return this;
    }

    public UpdateCompanyDTO build() {
        return new UpdateCompanyDTO(name, cnpj, types, configuration);
    }
}

