package br.com.logistics.tms.integration;

import br.com.logistics.tms.AbstractIntegrationTest;
import br.com.logistics.tms.assertions.jpa.CompanyEntityAssert;
import br.com.logistics.tms.assertions.jpa.ShipmentOrderCompanyEntityAssert;
import br.com.logistics.tms.assertions.jpa.ShipmentOrderEntityAssert;
import br.com.logistics.tms.builders.dto.CreateCompanyDTOBuilder;
import br.com.logistics.tms.builders.dto.CreateShipmentOrderDTOBuilder;
import br.com.logistics.tms.builders.dto.UpdateCompanyDTOBuilder;
import br.com.logistics.tms.company.domain.CompanyId;
import br.com.logistics.tms.company.domain.CompanyType;
import br.com.logistics.tms.company.infrastructure.jpa.entities.CompanyEntity;
import br.com.logistics.tms.shipmentorder.domain.ShipmentOrderId;
import br.com.logistics.tms.shipmentorder.infrastructure.jpa.entities.ShipmentOrderCompanyEntity;
import br.com.logistics.tms.shipmentorder.infrastructure.jpa.entities.ShipmentOrderEntity;
import org.junit.jupiter.api.Test;

import static br.com.logistics.tms.assertions.jpa.CompanyEntityAssert.assertThatCompany;
import static br.com.logistics.tms.assertions.jpa.ShipmentOrderCompanyEntityAssert.assertThatShipmentOrderCompany;
import static br.com.logistics.tms.assertions.jpa.ShipmentOrderEntityAssert.assertThatShipmentOrder;

class CompanyShipmentOrderIT extends AbstractIntegrationTest {

    @Test
    void shouldCreateAndUpdateCompanyThenCreateShipmentOrderAndIncrementCompanyOrders() throws Exception {
        final CompanyId companyId = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
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
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withName("Shipper Company")
                        .withTypes(CompanyType.LOGISTICS_PROVIDER)
                        .build());

        final CompanyEntity shipperCompany = companyJpaRepository.findById(shipperId.value()).orElseThrow();
        assertThatCompany(shipperCompany)
                .hasName("Shipper Company")
                .hasTypes(CompanyType.LOGISTICS_PROVIDER);

        companyFixture.updateCompany(companyId,
                UpdateCompanyDTOBuilder.anUpdateCompanyDTO()
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
                CreateShipmentOrderDTOBuilder.aCreateShipmentOrderDTO()
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
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withName("Multi Order Company")
                        .build()
        );

        final CompanyId shipperId = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withTypes(CompanyType.LOGISTICS_PROVIDER)
                        .build()
        );

        shipmentOrderFixture.createShipmentOrder(
                CreateShipmentOrderDTOBuilder.aCreateShipmentOrderDTO()
                        .withCompanyId(companyId.value())
                        .withShipperId(shipperId.value())
                        .build()
        );

        shipmentOrderFixture.createShipmentOrder(
                CreateShipmentOrderDTOBuilder.aCreateShipmentOrderDTO()
                        .withCompanyId(companyId.value())
                        .withShipperId(shipperId.value())
                        .build()
        );

        shipmentOrderFixture.createShipmentOrder(
                CreateShipmentOrderDTOBuilder.aCreateShipmentOrderDTO()
                        .withCompanyId(companyId.value())
                        .withShipperId(shipperId.value())
                        .build()
        );

        final CompanyEntity company = companyJpaRepository.findById(companyId.value()).orElseThrow();
        assertThatCompany(company)
                .hasShipmentOrderCount(3);
    }

    @Test
    void shouldUpdateCompanyTypesThenDeleteAndSyncStatusToShipmentOrder() throws Exception {
        final CompanyId companyId = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withName("Status Sync Test Company")
                        .withTypes(CompanyType.SELLER)
                        .build());

        final CompanyId shipperId = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withTypes(CompanyType.LOGISTICS_PROVIDER)
                        .build()
        );

        final CompanyEntity createdCompany = companyJpaRepository.findById(companyId.value()).orElseThrow();
        assertThatCompany(createdCompany)
                .hasName("Status Sync Test Company")
                .hasTypes(CompanyType.SELLER)
                .isActive();

        final ShipmentOrderCompanyEntity shipmentOrderCompanyAfterCreation = shipmentOrderCompanyJpaRepository
                .findById(companyId.value())
                .orElseThrow();
        assertThatShipmentOrderCompany(shipmentOrderCompanyAfterCreation)
                .hasCompanyId(companyId.value())
                .hasData()
                .dataContainsKey("types")
                .dataCompanyTypesContains(CompanyType.SELLER)
                .isActive();

        companyFixture.updateCompany(companyId,
                UpdateCompanyDTOBuilder.anUpdateCompanyDTO()
                        .withName("Status Sync Test Company")
                        .withTypes(CompanyType.SELLER, CompanyType.LOGISTICS_PROVIDER)
                        .build());

        final CompanyEntity updatedCompany = companyJpaRepository.findById(companyId.value()).orElseThrow();
        assertThatCompany(updatedCompany)
                .hasName("Status Sync Test Company")
                .hasTypes(CompanyType.SELLER, CompanyType.LOGISTICS_PROVIDER)
                .isActive();

        final ShipmentOrderCompanyEntity shipmentOrderCompanyAfterUpdate = shipmentOrderCompanyJpaRepository
                .findById(companyId.value())
                .orElseThrow();
        assertThatShipmentOrderCompany(shipmentOrderCompanyAfterUpdate)
                .dataContainsKey("types")
                .dataCompanyTypesContains(CompanyType.SELLER)
                .dataCompanyTypesContains(CompanyType.LOGISTICS_PROVIDER)
                .isActive();

        companyFixture.deleteCompany(companyId);

        final CompanyEntity deletedCompany = companyJpaRepository.findById(companyId.value()).orElseThrow();
        assertThatCompany(deletedCompany)
                .hasName("Status Sync Test Company")
                .hasTypes(CompanyType.SELLER, CompanyType.LOGISTICS_PROVIDER)
                .isDeleted();

        final ShipmentOrderCompanyEntity shipmentOrderCompanyAfterDelete = shipmentOrderCompanyJpaRepository
                .findById(companyId.value())
                .orElseThrow();
        assertThatShipmentOrderCompany(shipmentOrderCompanyAfterDelete)
                .hasCompanyId(companyId.value())
                .hasData()
                .dataContainsKey("types")
                .dataCompanyTypesContains(CompanyType.SELLER)
                .dataCompanyTypesContains(CompanyType.LOGISTICS_PROVIDER)
                .isDeleted();

        shipmentOrderFixture.createShipmentOrderWithDeletedCompany(
                CreateShipmentOrderDTOBuilder.aCreateShipmentOrderDTO()
                        .withCompanyId(companyId.value())
                        .withShipperId(shipperId.value())
                        .build()
        );
    }
}
