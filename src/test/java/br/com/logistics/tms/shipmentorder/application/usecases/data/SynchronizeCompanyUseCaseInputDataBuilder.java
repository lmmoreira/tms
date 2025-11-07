package br.com.logistics.tms.shipmentorder.application.usecases.data;

import br.com.logistics.tms.shipmentorder.application.usecases.SynchronizeCompanyUseCase;

import java.util.*;

public class SynchronizeCompanyUseCaseInputDataBuilder {

    private UUID companyId = UUID.randomUUID();
    private Map<String, Object> data = new HashMap<>();

    public static SynchronizeCompanyUseCaseInputDataBuilder anInput() {
        return new SynchronizeCompanyUseCaseInputDataBuilder();
    }

    public SynchronizeCompanyUseCaseInputDataBuilder withCompanyId(final UUID companyId) {
        this.companyId = companyId;
        return this;
    }

    public SynchronizeCompanyUseCaseInputDataBuilder withTypes(final List<String> types) {
        this.data.put(SynchronizeCompanyUseCase.TYPES_KEY, types);
        return this;
    }

    public SynchronizeCompanyUseCaseInputDataBuilder withTypes(final String... types) {
        return withTypes(Arrays.asList(types));
    }

    public SynchronizeCompanyUseCaseInputDataBuilder withData(final Map<String, Object> data) {
        this.data = new HashMap<>(data);
        return this;
    }

    public SynchronizeCompanyUseCaseInputDataBuilder withNullData() {
        this.data = null;
        return this;
    }

    public SynchronizeCompanyUseCaseInputDataBuilder withEmptyData() {
        this.data = new HashMap<>();
        return this;
    }

    public SynchronizeCompanyUseCaseInputDataBuilder withDataEntry(final String key, final Object value) {
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
