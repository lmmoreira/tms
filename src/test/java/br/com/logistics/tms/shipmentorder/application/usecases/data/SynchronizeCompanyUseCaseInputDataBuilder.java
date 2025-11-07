package br.com.logistics.tms.shipmentorder.application.usecases.data;

import br.com.logistics.tms.shipmentorder.application.usecases.SynchronizeCompanyUseCase;

import java.util.*;

/**
 * Test data builder for creating SynchronizeCompanyUseCase.Input instances using a fluent builder pattern.
 * Provides sensible defaults and reduces repetition in tests.
 */
public class SynchronizeCompanyUseCaseInputDataBuilder {

    private UUID companyId = UUID.randomUUID();
    private Map<String, Object> data = new HashMap<>();

    /**
     * Creates a new builder instance with default values.
     */
    public static SynchronizeCompanyUseCaseInputDataBuilder anInput() {
        return new SynchronizeCompanyUseCaseInputDataBuilder();
    }

    /**
     * Sets the company ID.
     */
    public SynchronizeCompanyUseCaseInputDataBuilder withCompanyId(final UUID companyId) {
        this.companyId = companyId;
        return this;
    }

    /**
     * Sets the types in the data map.
     */
    public SynchronizeCompanyUseCaseInputDataBuilder withTypes(final List<String> types) {
        this.data.put(SynchronizeCompanyUseCase.TYPES_KEY, types);
        return this;
    }

    /**
     * Sets the types in the data map from varargs.
     */
    public SynchronizeCompanyUseCaseInputDataBuilder withTypes(final String... types) {
        return withTypes(Arrays.asList(types));
    }

    /**
     * Sets custom data.
     */
    public SynchronizeCompanyUseCaseInputDataBuilder withData(final Map<String, Object> data) {
        this.data = new HashMap<>(data);
        return this;
    }

    /**
     * Sets null data.
     */
    public SynchronizeCompanyUseCaseInputDataBuilder withNullData() {
        this.data = null;
        return this;
    }

    /**
     * Sets empty data.
     */
    public SynchronizeCompanyUseCaseInputDataBuilder withEmptyData() {
        this.data = new HashMap<>();
        return this;
    }

    /**
     * Adds a single data entry.
     */
    public SynchronizeCompanyUseCaseInputDataBuilder withDataEntry(final String key, final Object value) {
        if (this.data == null) {
            this.data = new HashMap<>();
        }
        this.data.put(key, value);
        return this;
    }

    /**
     * Builds the Input instance with the configured values.
     */
    public SynchronizeCompanyUseCase.Input build() {
        return new SynchronizeCompanyUseCase.Input(companyId, data);
    }
}
