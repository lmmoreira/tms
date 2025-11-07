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

    /**
     * Creates a new builder instance with default values.
     */
    public static CompanyTestDataBuilder aCompany() {
        return new CompanyTestDataBuilder();
    }

    /**
     * Sets the company ID.
     */
    public CompanyTestDataBuilder withCompanyId(final UUID companyId) {
        this.companyId = companyId;
        return this;
    }

    /**
     * Sets the company types.
     */
    public CompanyTestDataBuilder withTypes(final List<String> types) {
        this.data.put(SynchronizeCompanyUseCase.TYPES_KEY, types);
        return this;
    }

    /**
     * Sets the company types from varargs.
     */
    public CompanyTestDataBuilder withTypes(final String... types) {
        return withTypes(Arrays.asList(types));
    }

    /**
     * Sets custom data for the company.
     */
    public CompanyTestDataBuilder withData(final Map<String, Object> data) {
        this.data = new HashMap<>(data);
        return this;
    }

    /**
     * Adds a single data entry.
     */
    public CompanyTestDataBuilder withDataEntry(final String key, final Object value) {
        this.data.put(key, value);
        return this;
    }

    /**
     * Builds a Company instance with the configured values.
     */
    public Company build() {
        // Ensure we have at least some data to satisfy CompanyData validation
        if (data.isEmpty()) {
            data.put(SynchronizeCompanyUseCase.TYPES_KEY, List.of("DEFAULT"));
        }
        return Company.createCompany(companyId, data);
    }
}
