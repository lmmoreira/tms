package br.com.logistics.tms.shipmentorder.data;

import br.com.logistics.tms.shipmentorder.domain.Company;

import java.util.*;

public class CompanyTestDataBuilder {

    private UUID companyId;
    private Map<String, Object> data;

    private CompanyTestDataBuilder() {
        this.companyId = UUID.randomUUID();
        this.data = new HashMap<>();
    }

    public static CompanyTestDataBuilder aCompany() {
        return new CompanyTestDataBuilder();
    }

    public CompanyTestDataBuilder withCompanyId(final UUID companyId) {
        this.companyId = companyId;
        return this;
    }

    public CompanyTestDataBuilder withData(final Map<String, Object> data) {
        this.data = new HashMap<>(data);
        return this;
    }

    public CompanyTestDataBuilder withTypes(final List<String> types) {
        this.data.put("types", types);
        return this;
    }

    public CompanyTestDataBuilder withType(final String type) {
        this.data.put("types", List.of(type));
        return this;
    }

    public Company build() {
        return Company.createCompany(companyId, data);
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public Map<String, Object> getData() {
        return new HashMap<>(data);
    }
}
