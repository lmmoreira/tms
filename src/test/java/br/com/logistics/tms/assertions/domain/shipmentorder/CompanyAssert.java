package br.com.logistics.tms.assertions.domain.shipmentorder;

import br.com.logistics.tms.shipmentorder.domain.Company;
import org.assertj.core.api.AbstractAssert;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CompanyAssert extends AbstractAssert<CompanyAssert, Company> {

    private CompanyAssert(final Company actual) {
        super(actual, CompanyAssert.class);
    }

    public static CompanyAssert assertThatCompany(final Company actual) {
        return new CompanyAssert(actual);
    }

    public CompanyAssert hasCompanyId(final UUID expectedCompanyId) {
        isNotNull();
        assertThat(actual.getCompanyId().value())
                .as("Company ID")
                .isEqualTo(expectedCompanyId);
        return this;
    }

    public CompanyAssert hasTypes(final String... expectedTypes) {
        isNotNull();
        final Set<String> actualTypes = actual.types();
        assertThat(actualTypes)
                .as("Company types")
                .contains(expectedTypes);
        return this;
    }

    public CompanyAssert hasExactlyTypes(final String... expectedTypes) {
        isNotNull();
        final Set<String> actualTypes = actual.types();
        assertThat(actualTypes)
                .as("Company types")
                .containsExactlyInAnyOrder(expectedTypes);
        return this;
    }

    public CompanyAssert hasTypesCount(final int expectedCount) {
        isNotNull();
        assertThat(actual.types())
                .as("Company types count")
                .hasSize(expectedCount);
        return this;
    }

    public CompanyAssert hasEmptyTypes() {
        isNotNull();
        assertThat(actual.types())
                .as("Company types")
                .isEmpty();
        return this;
    }

    public CompanyAssert isLogisticsProvider() {
        isNotNull();
        assertThat(actual.isLogisticsProvider())
                .as("Company is logistics provider")
                .isTrue();
        return this;
    }

    public CompanyAssert isNotLogisticsProvider() {
        isNotNull();
        assertThat(actual.isLogisticsProvider())
                .as("Company is not logistics provider")
                .isFalse();
        return this;
    }

    public CompanyAssert hasDataEntry(final String key, final Object value) {
        isNotNull();
        final Map<String, Object> data = actual.getData().value();
        assertThat(data)
                .as("Company data")
                .containsEntry(key, value);
        return this;
    }

    public CompanyAssert hasDataKey(final String key) {
        isNotNull();
        final Map<String, Object> data = actual.getData().value();
        assertThat(data)
                .as("Company data")
                .containsKey(key);
        return this;
    }

    public CompanyAssert doesNotHaveDataKey(final String key) {
        isNotNull();
        final Map<String, Object> data = actual.getData().value();
        assertThat(data)
                .as("Company data")
                .doesNotContainKey(key);
        return this;
    }

    public CompanyAssert hasDataValue(final String key, final Object expectedValue) {
        isNotNull();
        final Map<String, Object> data = actual.getData().value();
        assertThat(data.get(key))
                .as("Company data value for key '%s'", key)
                .isEqualTo(expectedValue);
        return this;
    }

    public CompanyAssert hasStatus(final Character status) {
        isNotNull();
        assertThat(actual.getStatus().value())
                .as("Company status")
                .isEqualTo(status);
        return this;
    }

    public CompanyAssert isActive() {
        isNotNull();
        assertThat(actual.getStatus().isActive())
                .as("Company is active")
                .isTrue();
        return this;
    }

    public CompanyAssert isSuspended() {
        isNotNull();
        assertThat(actual.getStatus().isSuspended())
                .as("Company is suspended")
                .isTrue();
        return this;
    }

    public CompanyAssert isDeleted() {
        isNotNull();
        assertThat(actual.getStatus().isDeleted())
                .as("Company is deleted")
                .isTrue();
        return this;
    }

    public CompanyAssert isInactive() {
        isNotNull();
        assertThat(actual.getStatus().isInactive())
                .as("Company is inactive")
                .isTrue();
        return this;
    }
}
