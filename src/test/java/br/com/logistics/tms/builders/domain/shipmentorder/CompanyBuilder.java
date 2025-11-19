package br.com.logistics.tms.builders.domain.shipmentorder;

import br.com.logistics.tms.shipmentorder.application.usecases.SynchronizeCompanyUseCase;
import br.com.logistics.tms.shipmentorder.domain.Company;

import java.util.*;

public class CompanyBuilder {

    private UUID companyId = UUID.randomUUID();
    private Map<String, Object> data = new HashMap<>();

    public static CompanyBuilder aCompany() {
        return new CompanyBuilder();
    }

    public CompanyBuilder withCompanyId(final UUID companyId) {
        this.companyId = companyId;
        return this;
    }

    public CompanyBuilder withTypes(final List<String> types) {
        this.data.put(SynchronizeCompanyUseCase.TYPES_KEY, types);
        return this;
    }

    public CompanyBuilder withTypes(final String... types) {
        return withTypes(Arrays.asList(types));
    }

    public CompanyBuilder withData(final Map<String, Object> data) {
        this.data = new HashMap<>(data);
        return this;
    }

    public CompanyBuilder withDataEntry(final String key, final Object value) {
        this.data.put(key, value);
        return this;
    }

    public Company build() {
        if (data.isEmpty()) {
            data.put(SynchronizeCompanyUseCase.TYPES_KEY, List.of("DEFAULT"));
        }
        return Company.createCompany(companyId, data);
    }
}
