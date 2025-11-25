package br.com.logistics.tms.assertions.jpa;

import br.com.logistics.tms.company.domain.CompanyType;
import br.com.logistics.tms.company.infrastructure.jpa.entities.CompanyEntity;
import org.assertj.core.api.AbstractAssert;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CompanyEntityAssert extends AbstractAssert<CompanyEntityAssert, CompanyEntity> {

    private CompanyEntityAssert(final CompanyEntity actual) {
        super(actual, CompanyEntityAssert.class);
    }

    public static CompanyEntityAssert assertThatCompany(final CompanyEntity actual) {
        return new CompanyEntityAssert(actual);
    }

    public CompanyEntityAssert hasName(final String name) {
        isNotNull();
        assertThat(actual.getName())
                .as("Company name")
                .isEqualTo(name);
        return this;
    }

    public CompanyEntityAssert hasCnpj(final String cnpj) {
        isNotNull();
        assertThat(actual.getCnpj())
                .as("Company CNPJ")
                .isEqualTo(cnpj);
        return this;
    }

    public CompanyEntityAssert hasTypes(final CompanyType... types) {
        isNotNull();
        assertThat(actual.getCompanyTypes())
                .as("Company types")
                .contains(types);
        return this;
    }

    public CompanyEntityAssert hasExactlyTypes(final CompanyType... types) {
        isNotNull();
        assertThat(actual.getCompanyTypes())
                .as("Company types")
                .containsExactlyInAnyOrder(types);
        return this;
    }

    public CompanyEntityAssert hasConfigurationEntry(final String key, final Object value) {
        isNotNull();
        assertThat(actual.getConfiguration())
                .as("Company configuration")
                .isNotNull()
                .containsEntry(key, value);
        return this;
    }

    public CompanyEntityAssert hasConfiguration(final Map<String, Object> configuration) {
        isNotNull();
        assertThat(actual.getConfiguration())
                .as("Company configuration")
                .containsAllEntriesOf(configuration);
        return this;
    }

    public CompanyEntityAssert hasShipmentOrderCount(final int count) {
        isNotNull();
        assertThat(actual.getConfiguration())
                .as("Company configuration")
                .isNotNull();
        
        final Object orderNumber = actual.getConfiguration().get("shipmentOrderNumber");
        assertThat(orderNumber)
                .as("Shipment order number")
                .isNotNull();
        
        final int actualCount = Integer.parseInt(orderNumber.toString());
        assertThat(actualCount)
                .as("Shipment order count")
                .isEqualTo(count);
        
        return this;
    }

    public CompanyEntityAssert hasStatus(final Character status) {
        isNotNull();
        assertThat(actual.getStatus())
                .as("Company status")
                .isEqualTo(status);
        return this;
    }

    public CompanyEntityAssert isActive() {
        isNotNull();
        assertThat(actual.getStatus())
                .as("Company status")
                .isEqualTo('A');
        return this;
    }

    public CompanyEntityAssert isSuspended() {
        isNotNull();
        assertThat(actual.getStatus())
                .as("Company status")
                .isEqualTo('S');
        return this;
    }

    public CompanyEntityAssert isDeleted() {
        isNotNull();
        assertThat(actual.getStatus())
                .as("Company status")
                .isEqualTo('D');
        return this;
    }
}
