package br.com.logistics.tms.shipmentorder.application.usecases;

import br.com.logistics.tms.builders.domain.shipmentorder.CompanyBuilder;
import br.com.logistics.tms.builders.input.SynchronizeCompanyInputBuilder;
import br.com.logistics.tms.shipmentorder.application.repositories.FakeCompanyRepository;
import br.com.logistics.tms.shipmentorder.domain.Company;
import br.com.logistics.tms.shipmentorder.domain.CompanyId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static br.com.logistics.tms.assertions.domain.shipmentorder.CompanyAssert.assertThatCompany;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SynchronizeCompanyUseCaseTest {

    private FakeCompanyRepository companyRepository;
    private SynchronizeCompanyUseCase useCase;

    @BeforeEach
    void setUp() {
        companyRepository = new FakeCompanyRepository();
        useCase = new SynchronizeCompanyUseCase(companyRepository);
    }

    @Test
    @DisplayName("Should create a new company when company does not exist")
    void shouldCreateNewCompanyWhenCompanyDoesNotExist() {
        final UUID companyId = UUID.randomUUID();
        final SynchronizeCompanyUseCase.Input input = SynchronizeCompanyInputBuilder.anInput()
                .withCompanyId(companyId)
                .withTypes("LOGISTICS_PROVIDER", "CARRIER")
                .build();

        useCase.execute(input);

        final Company savedCompany = companyRepository.findById(CompanyId.with(companyId))
                .orElseThrow(() -> new AssertionError("Company should be saved"));

        assertThatCompany(savedCompany)
                .hasCompanyId(companyId)
                .hasTypes("LOGISTICS_PROVIDER", "CARRIER")
                .hasTypesCount(2);
    }

    @Test
    @DisplayName("Should update existing company when company exists")
    void shouldUpdateExistingCompanyWhenCompanyExists() {
        final UUID companyId = UUID.randomUUID();
        final Company existingCompany = CompanyBuilder.aCompany()
                .withCompanyId(companyId)
                .withTypes("CARRIER")
                .build();
        companyRepository.save(existingCompany);

        final SynchronizeCompanyUseCase.Input input = SynchronizeCompanyInputBuilder.anInput()
                .withCompanyId(companyId)
                .withTypes("LOGISTICS_PROVIDER", "SHIPPER")
                .build();
        useCase.execute(input);

        final Company updatedCompany = companyRepository.findById(CompanyId.with(companyId))
                .orElseThrow(() -> new AssertionError("Company should exist"));

        assertThatCompany(updatedCompany)
                .hasTypes("LOGISTICS_PROVIDER", "SHIPPER")
                .hasTypesCount(2);
    }

    @Test
    @DisplayName("Should not save anything when data is null")
    void shouldNotSaveWhenDataIsNull() {
        final UUID companyId = UUID.randomUUID();
        final SynchronizeCompanyUseCase.Input input = SynchronizeCompanyInputBuilder.anInput()
                .withCompanyId(companyId)
                .withNullData()
                .build();

        useCase.execute(input);

        assertEquals(0, companyRepository.count(), "No company should be saved");
        assertFalse(companyRepository.existsById(CompanyId.with(companyId)));
    }

    @Test
    @DisplayName("Should not save anything when data does not contain types key")
    void shouldNotSaveWhenDataDoesNotContainTypesKey() {
        final UUID companyId = UUID.randomUUID();
        final SynchronizeCompanyUseCase.Input input = SynchronizeCompanyInputBuilder.anInput()
                .withCompanyId(companyId)
                .withDataEntry("someOtherKey", "someValue")
                .build();

        useCase.execute(input);

        assertEquals(0, companyRepository.count(), "No company should be saved");
        assertFalse(companyRepository.existsById(CompanyId.with(companyId)));
    }

    @Test
    @DisplayName("Should not save anything when data is empty")
    void shouldNotSaveWhenDataIsEmpty() {
        final UUID companyId = UUID.randomUUID();
        final SynchronizeCompanyUseCase.Input input = SynchronizeCompanyInputBuilder.anInput()
                .withCompanyId(companyId)
                .withEmptyData()
                .build();

        useCase.execute(input);

        assertEquals(0, companyRepository.count(), "No company should be saved");
        assertFalse(companyRepository.existsById(CompanyId.with(companyId)));
    }

    @Test
    @DisplayName("Should handle empty types list")
    void shouldHandleEmptyTypesList() {
        final UUID companyId = UUID.randomUUID();
        final SynchronizeCompanyUseCase.Input input = SynchronizeCompanyInputBuilder.anInput()
                .withCompanyId(companyId)
                .withTypes(List.of())
                .build();

        useCase.execute(input);

        final Company savedCompany = companyRepository.findById(CompanyId.with(companyId))
                .orElseThrow(() -> new AssertionError("Company should be saved even with empty types"));

        assertThatCompany(savedCompany)
                .hasEmptyTypes();
    }

    @Test
    @DisplayName("Should preserve other data fields when updating existing company")
    void shouldPreserveOtherDataFieldsWhenUpdating() {
        final UUID companyId = UUID.randomUUID();
        final Company existingCompany = CompanyBuilder.aCompany()
                .withCompanyId(companyId)
                .withTypes("CARRIER")
                .withDataEntry("name", "Test Company")
                .withDataEntry("address", "123 Test St")
                .build();
        companyRepository.save(existingCompany);

        final SynchronizeCompanyUseCase.Input input = SynchronizeCompanyInputBuilder.anInput()
                .withCompanyId(companyId)
                .withTypes("LOGISTICS_PROVIDER")
                .build();
        useCase.execute(input);

        final Company updatedCompany = companyRepository.findById(CompanyId.with(companyId))
                .orElseThrow(() -> new AssertionError("Company should exist"));

        assertThatCompany(updatedCompany)
                .hasDataEntry("name", "Test Company")
                .hasDataEntry("address", "123 Test St")
                .hasTypes("LOGISTICS_PROVIDER")
                .hasTypesCount(1);
    }

    @Test
    @DisplayName("Should handle single type value")
    void shouldHandleSingleTypeValue() {
        final UUID companyId = UUID.randomUUID();
        final SynchronizeCompanyUseCase.Input input = SynchronizeCompanyInputBuilder.anInput()
                .withCompanyId(companyId)
                .withTypes("SHIPPER")
                .build();

        useCase.execute(input);

        final Company savedCompany = companyRepository.findById(CompanyId.with(companyId))
                .orElseThrow(() -> new AssertionError("Company should be saved"));

        assertThatCompany(savedCompany)
                .hasTypes("SHIPPER")
                .hasTypesCount(1);
    }

    @Test
    @DisplayName("Should verify company is logistics provider after synchronization")
    void shouldVerifyCompanyIsLogisticsProviderAfterSynchronization() {
        final UUID companyId = UUID.randomUUID();
        final SynchronizeCompanyUseCase.Input input = SynchronizeCompanyInputBuilder.anInput()
                .withCompanyId(companyId)
                .withTypes("LOGISTICS_PROVIDER", "CARRIER")
                .build();

        useCase.execute(input);

        final Company savedCompany = companyRepository.findById(CompanyId.with(companyId))
                .orElseThrow(() -> new AssertionError("Company should be saved"));

        assertThatCompany(savedCompany)
                .isLogisticsProvider();
    }

    @Test
    @DisplayName("Should synchronize company with DELETED status when status change is received")
    void shouldSynchronizeCompanyWithDeletedStatus() {
        final UUID companyId = UUID.randomUUID();
        final Company existingCompany = CompanyBuilder.aCompany()
                .withCompanyId(companyId)
                .withTypes("SELLER")
                .build();
        companyRepository.save(existingCompany);

        final SynchronizeCompanyUseCase.Input input = SynchronizeCompanyInputBuilder.anInput()
                .withCompanyId(companyId)
                .withDataEntry("status", "D")
                .build();

        useCase.execute(input);

        final Company updatedCompany = companyRepository.findById(CompanyId.with(companyId))
                .orElseThrow(() -> new AssertionError("Company should exist"));

        assertThatCompany(updatedCompany)
                .isDeleted();
    }

    @Test
    @DisplayName("Should synchronize company with SUSPENDED status when status change is received")
    void shouldSynchronizeCompanyWithSuspendedStatus() {
        final UUID companyId = UUID.randomUUID();
        final Company existingCompany = CompanyBuilder.aCompany()
                .withCompanyId(companyId)
                .withTypes("CARRIER")
                .build();
        companyRepository.save(existingCompany);

        final SynchronizeCompanyUseCase.Input input = SynchronizeCompanyInputBuilder.anInput()
                .withCompanyId(companyId)
                .withDataEntry("status", "S")
                .build();

        useCase.execute(input);

        final Company updatedCompany = companyRepository.findById(CompanyId.with(companyId))
                .orElseThrow(() -> new AssertionError("Company should exist"));

        assertThatCompany(updatedCompany)
                .isSuspended();
    }

    @Test
    @DisplayName("Should maintain ACTIVE status when no status change is provided")
    void shouldMaintainActiveStatusWhenNoStatusProvided() {
        final UUID companyId = UUID.randomUUID();
        final Company existingCompany = CompanyBuilder.aCompany()
                .withCompanyId(companyId)
                .withTypes("SELLER")
                .build();
        companyRepository.save(existingCompany);

        final SynchronizeCompanyUseCase.Input input = SynchronizeCompanyInputBuilder.anInput()
                .withCompanyId(companyId)
                .withTypes("SELLER", "LOGISTICS_PROVIDER")
                .build();

        useCase.execute(input);

        final Company updatedCompany = companyRepository.findById(CompanyId.with(companyId))
                .orElseThrow(() -> new AssertionError("Company should exist"));

        assertThatCompany(updatedCompany)
                .isActive();
    }

    @Test
    @DisplayName("Should not update data when company is in SUSPENDED status")
    void shouldNotUpdateDataWhenCompanyIsSuspended() {
        final UUID companyId = UUID.randomUUID();
        final Company suspendedCompany = CompanyBuilder.aCompany()
                .withCompanyId(companyId)
                .withTypes("SELLER")
                .build()
                .updateStatus(br.com.logistics.tms.commons.domain.Status.suspended());
        companyRepository.save(suspendedCompany);

        final SynchronizeCompanyUseCase.Input input = SynchronizeCompanyInputBuilder.anInput()
                .withCompanyId(companyId)
                .withTypes("LOGISTICS_PROVIDER")
                .build();

        useCase.execute(input);

        final Company updatedCompany = companyRepository.findById(CompanyId.with(companyId))
                .orElseThrow(() -> new AssertionError("Company should exist"));

        assertThatCompany(updatedCompany)
                .hasTypes("SELLER")
                .isSuspended();
    }

    @Test
    @DisplayName("Should not update data when company is in DELETED status")
    void shouldNotUpdateDataWhenCompanyIsDeleted() {
        final UUID companyId = UUID.randomUUID();
        final Company deletedCompany = CompanyBuilder.aCompany()
                .withCompanyId(companyId)
                .withTypes("SELLER")
                .build()
                .updateStatus(br.com.logistics.tms.commons.domain.Status.deleted());
        companyRepository.save(deletedCompany);

        final SynchronizeCompanyUseCase.Input input = SynchronizeCompanyInputBuilder.anInput()
                .withCompanyId(companyId)
                .withTypes("LOGISTICS_PROVIDER")
                .build();

        useCase.execute(input);

        final Company updatedCompany = companyRepository.findById(CompanyId.with(companyId))
                .orElseThrow(() -> new AssertionError("Company should exist"));

        assertThatCompany(updatedCompany)
                .hasTypes("SELLER")
                .isDeleted();
    }

    @Test
    @DisplayName("Should update status from ACTIVE to DELETED")
    void shouldUpdateStatusFromActiveToDeleted() {
        final UUID companyId = UUID.randomUUID();
        final Company activeCompany = CompanyBuilder.aCompany()
                .withCompanyId(companyId)
                .withTypes("SELLER")
                .build();
        companyRepository.save(activeCompany);

        final Company beforeUpdate = companyRepository.findById(CompanyId.with(companyId))
                .orElseThrow();
        assertThatCompany(beforeUpdate).isActive();

        final SynchronizeCompanyUseCase.Input input = SynchronizeCompanyInputBuilder.anInput()
                .withCompanyId(companyId)
                .withDataEntry("status", "D")
                .build();
        useCase.execute(input);

        final Company updatedCompany = companyRepository.findById(CompanyId.with(companyId))
                .orElseThrow();

        assertThatCompany(updatedCompany).isDeleted();
    }

    @Test
    @DisplayName("Should preserve types when synchronizing status change to DELETED")
    void shouldPreserveTypesWhenSyncingStatusToDeleted() {
        final UUID companyId = UUID.randomUUID();
        final Company existingCompany = CompanyBuilder.aCompany()
                .withCompanyId(companyId)
                .withTypes("SELLER", "LOGISTICS_PROVIDER", "CARRIER")
                .build();
        companyRepository.save(existingCompany);

        final SynchronizeCompanyUseCase.Input input = SynchronizeCompanyInputBuilder.anInput()
                .withCompanyId(companyId)
                .withDataEntry("status", "D")
                .build();

        useCase.execute(input);

        final Company updatedCompany = companyRepository.findById(CompanyId.with(companyId))
                .orElseThrow();

        assertThatCompany(updatedCompany)
                .isDeleted()
                .hasTypes("SELLER", "LOGISTICS_PROVIDER", "CARRIER")
                .hasTypesCount(3);
    }

    @Test
    @DisplayName("Should sync ACTIVE status for new company creation")
    void shouldSyncActiveStatusForNewCompanyCreation() {
        final UUID companyId = UUID.randomUUID();
        final SynchronizeCompanyUseCase.Input input = SynchronizeCompanyInputBuilder.anInput()
                .withCompanyId(companyId)
                .withTypes("SELLER")
                .build();

        useCase.execute(input);

        final Company savedCompany = companyRepository.findById(CompanyId.with(companyId))
                .orElseThrow();

        assertThatCompany(savedCompany)
                .isActive();
    }
}
