package br.com.logistics.tms.integration;

import br.com.logistics.tms.AbstractIntegrationTest;
import br.com.logistics.tms.company.domain.CompanyId;
import br.com.logistics.tms.company.domain.CompanyType;
import br.com.logistics.tms.company.infrastructure.jpa.entities.CompanyEntity;
import br.com.logistics.tms.integration.data.CreateCompanyDTODataBuilder;
import br.com.logistics.tms.integration.data.CreateShipmentOrderDTODataBuilder;
import br.com.logistics.tms.integration.data.UpdateCompanyDTODataBuilder;
import br.com.logistics.tms.integration.fixtures.CompanyIntegrationFixture;
import br.com.logistics.tms.integration.fixtures.ShipmentOrderIntegrationFixture;
import br.com.logistics.tms.shipmentorder.domain.ShipmentOrderId;
import br.com.logistics.tms.shipmentorder.infrastructure.jpa.entities.ShipmentOrderCompanyEntity;
import br.com.logistics.tms.shipmentorder.infrastructure.jpa.entities.ShipmentOrderEntity;
import org.junit.jupiter.api.Test;

import static br.com.logistics.tms.integration.assertions.CompanyEntityAssert.assertThatCompany;
import static br.com.logistics.tms.integration.assertions.ShipmentOrderCompanyEntityAssert.assertThatShipmentOrderCompany;
import static br.com.logistics.tms.integration.assertions.ShipmentOrderEntityAssert.assertThatShipmentOrder;

class CompanyShipmentOrderIT extends AbstractIntegrationTest {

    @Test
    void shouldCreateAndUpdateCompanyThenCreateShipmentOrderAndIncrementCompanyOrders() throws Exception {
        final CompanyId companyId = companyFixture.createCompany(
                CreateCompanyDTODataBuilder.aCreateCompanyDTO()
                        .withName("Test Company")
                        .withTypes(CompanyType.SELLER)
                        .build());

        final CompanyEntity company = companyJpaRepository.findById(companyId.value()).orElseThrow();
        assertThatCompany(company)
                .hasName("Test Company")
                .hasTypes(CompanyType.SELLER);

        final ShipmentOrderCompanyEntity shipmentOrderCompany = shipmentOrderCompanyJpaRepository
                .findById(companyId.value())
                .orElseThrow();
        assertThatShipmentOrderCompany(shipmentOrderCompany)
                .hasCompanyId(companyId.value())
                .hasData()
                .dataContainsKey("types")
                .dataCompanyTypesContains(CompanyType.SELLER);

        final CompanyId shipperId = companyFixture.createCompany(
                CreateCompanyDTODataBuilder.aCreateCompanyDTO()
                        .withName("Shipper Company")
                        .withTypes(CompanyType.LOGISTICS_PROVIDER)
                        .build());

        final CompanyEntity shipperCompany = companyJpaRepository.findById(shipperId.value()).orElseThrow();
        assertThatCompany(shipperCompany)
                .hasName("Shipper Company")
                .hasTypes(CompanyType.LOGISTICS_PROVIDER);

        companyFixture.updateCompany(companyId,
                UpdateCompanyDTODataBuilder.anUpdateCompanyDTO()
                        .withName("Updated Company")
                        .withTypes(CompanyType.SELLER, CompanyType.MARKETPLACE)
                        .withConfigurationEntry("webhook", "http://updated-webhook.com")
                        .build());

        final CompanyEntity updatedCompany = companyJpaRepository.findById(companyId.value()).orElseThrow();
        assertThatCompany(updatedCompany)
                .hasName("Updated Company")
                .hasExactlyTypes(CompanyType.SELLER, CompanyType.MARKETPLACE)
                .hasConfigurationEntry("webhook", "http://updated-webhook.com");

        final ShipmentOrderCompanyEntity updatedShipmentOrderCompany = shipmentOrderCompanyJpaRepository
                .findById(companyId.value())
                .orElseThrow();
        assertThatShipmentOrderCompany(updatedShipmentOrderCompany)
                .dataContainsKey("types")
                .dataCompanyTypesContains(CompanyType.SELLER)
                .dataCompanyTypesContains(CompanyType.MARKETPLACE);

        final ShipmentOrderId orderId = shipmentOrderFixture.createShipmentOrder(
                CreateShipmentOrderDTODataBuilder.aCreateShipmentOrderDTO()
                        .withCompanyId(companyId.value())
                        .withShipperId(shipperId.value())
                        .withExternalId("EXT-ORDER-001")
                        .build()
        );

        final ShipmentOrderEntity order = shipmentOrderJpaRepository.findById(orderId.value()).orElseThrow();
        assertThatShipmentOrder(order)
                .hasCompanyId(companyId.value())
                .hasShipperId(shipperId.value())
                .hasExternalId("EXT-ORDER-001")
                .isNotArchived();

        final CompanyEntity finalCompany = companyJpaRepository.findById(companyId.value()).orElseThrow();
        assertThatCompany(finalCompany)
                .hasShipmentOrderCount(1);
    }

    @Test
    void shouldIncrementCompanyCounterForMultipleShipmentOrders() throws Exception {
        final CompanyId companyId = companyFixture.createCompany(
                CreateCompanyDTODataBuilder.aCreateCompanyDTO()
                        .withName("Multi Order Company")
                        .build()
        );

        final CompanyId shipperId = companyFixture.createCompany(
                CreateCompanyDTODataBuilder.aCreateCompanyDTO()
                        .withTypes(CompanyType.LOGISTICS_PROVIDER)
                        .build()
        );

        shipmentOrderFixture.createShipmentOrder(
                CreateShipmentOrderDTODataBuilder.aCreateShipmentOrderDTO()
                        .withCompanyId(companyId.value())
                        .withShipperId(shipperId.value())
                        .build()
        );

        shipmentOrderFixture.createShipmentOrder(
                CreateShipmentOrderDTODataBuilder.aCreateShipmentOrderDTO()
                        .withCompanyId(companyId.value())
                        .withShipperId(shipperId.value())
                        .build()
        );

        shipmentOrderFixture.createShipmentOrder(
                CreateShipmentOrderDTODataBuilder.aCreateShipmentOrderDTO()
                        .withCompanyId(companyId.value())
                        .withShipperId(shipperId.value())
                        .build()
        );

        final CompanyEntity company = companyJpaRepository.findById(companyId.value()).orElseThrow();
        assertThatCompany(company)
                .hasShipmentOrderCount(3);
    }
}
