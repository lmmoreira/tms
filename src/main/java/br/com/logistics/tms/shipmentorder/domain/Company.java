package br.com.logistics.tms.shipmentorder.domain;

import br.com.logistics.tms.commons.domain.AbstractAggregateRoot;
import br.com.logistics.tms.commons.domain.AbstractDomainEvent;
import br.com.logistics.tms.commons.domain.exception.ValidationException;

import java.util.*;

public class Company extends AbstractAggregateRoot {

    private final CompanyId companyId;
    private final CompanyData data;

    private Company(CompanyId companyId,
                    CompanyData data) {
        super(new HashSet<>(), new HashMap<>());

        if (companyId == null) throw new ValidationException("Invalid companyId for Company");
        if (data == null) throw new ValidationException("Invalid data for Company");

        this.companyId = companyId;
        this.data = data;
    }

    public static Company createCompany(UUID companyId, Map<String, Object> data) {
        return new Company(CompanyId.with(companyId), CompanyData.with(data));
    }

    public Company updateData(Map<String, Object> newData) {
        final Map<String, Object> mergedData = new HashMap<>(this.data.value());
        mergedData.putAll(newData);
        return new Company(this.companyId, CompanyData.with(mergedData));
    }

    public CompanyId getCompanyId() {
        return companyId;
    }

    public CompanyData getData() {
        return data;
    }

    public Set<String> types() {
        final Object typesObj = this.data.value().get("types");
        if (typesObj instanceof List<?> typesList) {
            final Set<String> typesSet = new HashSet<>();
            for (Object type : typesList) {
                if (type instanceof String typeStr) {
                    typesSet.add(typeStr);
                }
            }
            return typesSet;
        }
        return Collections.emptySet();
    }

    public boolean isLogisticsProvider() {
        return types().contains("LOGISTICS_PROVIDER");
    }

}
