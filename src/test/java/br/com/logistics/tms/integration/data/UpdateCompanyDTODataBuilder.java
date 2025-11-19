package br.com.logistics.tms.integration.data;

import br.com.logistics.tms.company.domain.CompanyType;
import br.com.logistics.tms.company.infrastructure.dto.UpdateCompanyDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UpdateCompanyDTODataBuilder {

    private String name = "Updated Company";
    private String cnpj = "50.617.696/0002-06";
    private Set<CompanyType> types = Set.of(CompanyType.SELLER);
    private Map<String, Object> configuration = new HashMap<>(Map.of(
            "notification", true
    ));

    public static UpdateCompanyDTODataBuilder anUpdateCompanyDTO() {
        return new UpdateCompanyDTODataBuilder();
    }

    public UpdateCompanyDTODataBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public UpdateCompanyDTODataBuilder withCnpj(final String cnpj) {
        this.cnpj = cnpj;
        return this;
    }

    public UpdateCompanyDTODataBuilder withTypes(final Set<CompanyType> types) {
        this.types = types;
        return this;
    }

    public UpdateCompanyDTODataBuilder withTypes(final CompanyType... types) {
        this.types = Set.of(types);
        return this;
    }

    public UpdateCompanyDTODataBuilder withConfiguration(final Map<String, Object> configuration) {
        this.configuration = new HashMap<>(configuration);
        return this;
    }

    public UpdateCompanyDTODataBuilder withConfigurationEntry(final String key, final Object value) {
        this.configuration.put(key, value);
        return this;
    }

    public UpdateCompanyDTO build() {
        return new UpdateCompanyDTO(name, cnpj, types, configuration);
    }
}
