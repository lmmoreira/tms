package br.com.logistics.tms.assertions.jpa;

import br.com.logistics.tms.company.domain.CompanyType;
import br.com.logistics.tms.shipmentorder.domain.Company;
import br.com.logistics.tms.shipmentorder.infrastructure.jpa.entities.ShipmentOrderCompanyEntity;
import org.assertj.core.api.AbstractAssert;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class ShipmentOrderCompanyEntityAssert extends AbstractAssert<ShipmentOrderCompanyEntityAssert, ShipmentOrderCompanyEntity> {

    private ShipmentOrderCompanyEntityAssert(final ShipmentOrderCompanyEntity actual) {
        super(actual, ShipmentOrderCompanyEntityAssert.class);
    }

    public static ShipmentOrderCompanyEntityAssert assertThatShipmentOrderCompany(final ShipmentOrderCompanyEntity actual) {
        return new ShipmentOrderCompanyEntityAssert(actual);
    }

    public ShipmentOrderCompanyEntityAssert hasCompanyId(final UUID companyId) {
        isNotNull();
        assertThat(actual.getCompanyId())
                .as("ShipmentOrderCompany companyId")
                .isEqualTo(companyId);
        return this;
    }

    public ShipmentOrderCompanyEntityAssert hasData() {
        isNotNull();
        assertThat(actual.getData())
                .as("ShipmentOrderCompany data")
                .isNotNull()
                .isNotEmpty();
        return this;
    }

    public ShipmentOrderCompanyEntityAssert hasDataEntry(final String key, final Object value) {
        isNotNull();
        assertThat(actual.getData())
                .as("ShipmentOrderCompany data")
                .isNotNull()
                .containsEntry(key, value);
        return this;
    }

    public ShipmentOrderCompanyEntityAssert hasData(final Map<String, Object> expectedData) {
        isNotNull();
        assertThat(actual.getData())
                .as("ShipmentOrderCompany data")
                .containsAllEntriesOf(expectedData);
        return this;
    }

    public ShipmentOrderCompanyEntityAssert dataContainsKey(final String key) {
        isNotNull();
        assertThat(actual.getData())
                .as("ShipmentOrderCompany data")
                .containsKey(key);
        return this;
    }

    public ShipmentOrderCompanyEntityAssert dataCompanyTypesContains(final CompanyType companyType) {
        isNotNull();

        final Company company = actual.toDomain();
        assertThat(company.types())
                .as("ShipmentOrderCompany data")
                .contains(companyType.name());
        return this;
    }

    public ShipmentOrderCompanyEntityAssert hasNameInData(final String name) {
        isNotNull();
        assertThat(actual.getData())
                .as("ShipmentOrderCompany data")
                .isNotNull()
                .containsEntry("name", name);
        return this;
    }
}
