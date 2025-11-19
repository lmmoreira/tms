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
        return parseTypes(this.data.value().get("types").toString());
    }

    public boolean isLogisticsProvider() {
        return types().contains("LOGISTICS_PROVIDER");
    }

    public static Set<String> parseTypes(final String input) {
        if (input == null) {
            return Collections.emptySet();
        }

        final int start = input.indexOf('[');
        final int end = (start >= 0) ? input.indexOf(']', start + 1) : -1;
        if (start < 0 || end < 0 || end <= start) {
            return Collections.emptySet();
        }

        final String inside = input.substring(start + 1, end);
        final String[] parts = inside.split(",");

        final Set<String> result = new LinkedHashSet<>();
        for (final String part : parts) {
            final String trimmed = (part == null) ? "" : part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }

        return Collections.unmodifiableSet(result);
    }

}
