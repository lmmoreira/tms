package br.com.logistics.tms.builders.input;

import br.com.logistics.tms.shipmentorder.application.usecases.SynchronizeCompanyUseCase;

import java.util.*;

public class SynchronizeCompanyInputBuilder {

    private UUID companyId = UUID.randomUUID();
    private Map<String, Object> data = new HashMap<>();

    public static SynchronizeCompanyInputBuilder anInput() {
        return new SynchronizeCompanyInputBuilder();
    }

    public SynchronizeCompanyInputBuilder withCompanyId(final UUID companyId) {
        this.companyId = companyId;
        return this;
    }

    public SynchronizeCompanyInputBuilder withTypes(final List<String> types) {
        this.data.put(SynchronizeCompanyUseCase.TYPES_KEY, types);
        return this;
    }

    public SynchronizeCompanyInputBuilder withTypes(final String... types) {
        return withTypes(Arrays.asList(types));
    }

    public SynchronizeCompanyInputBuilder withData(final Map<String, Object> data) {
        this.data = new HashMap<>(data);
        return this;
    }

    public SynchronizeCompanyInputBuilder withNullData() {
        this.data = null;
        return this;
    }

    public SynchronizeCompanyInputBuilder withEmptyData() {
        this.data = new HashMap<>();
        return this;
    }

    public SynchronizeCompanyInputBuilder withDataEntry(final String key, final Object value) {
        if (this.data == null) {
            this.data = new HashMap<>();
        }
        this.data.put(key, value);
        return this;
    }

    public SynchronizeCompanyUseCase.Input build() {
        return new SynchronizeCompanyUseCase.Input(companyId, data);
    }
}
