package br.com.logistics.tms.shipmentorder.data;

import br.com.logistics.tms.shipmentorder.application.usecases.SynchronizeCompanyUseCase;
import br.com.logistics.tms.shipmentorder.domain.Company;

import java.util.*;

/**
 * Test data builder for creating Company instances using a fluent builder pattern.
 * Provides sensible defaults for testing.
 */
public class CompanyTestDataBuilder {

    private UUID companyId = UUID.randomUUID();
    private Map<String, Object> data = new HashMap<>();

    public static CompanyTestDataBuilder aCompany() {
        return new CompanyTestDataBuilder();
    }

    public CompanyTestDataBuilder withCompanyId(final UUID companyId) {
        this.companyId = companyId;
        return this;
    }

    public CompanyTestDataBuilder withTypes(final List<String> types) {
        this.data.put(SynchronizeCompanyUseCase.TYPES_KEY, types);
        return this;
    }

    public CompanyTestDataBuilder withTypes(final String... types) {
        return withTypes(Arrays.asList(types));
    }

    public CompanyTestDataBuilder withData(final Map<String, Object> data) {
        this.data = new HashMap<>(data);
        return this;
    }

    public CompanyTestDataBuilder withDataEntry(final String key, final Object value) {
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
