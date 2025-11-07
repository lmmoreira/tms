package br.com.logistics.tms.shipmentorder.application.usecases;

import br.com.logistics.tms.shipmentorder.data.CompanyTestDataBuilder;
import br.com.logistics.tms.shipmentorder.data.FakeCompanyRepository;
import br.com.logistics.tms.shipmentorder.domain.Company;
import br.com.logistics.tms.shipmentorder.domain.CompanyId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

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
        final List<String> types = List.of("LOGISTICS_PROVIDER", "CARRIER");
        final Map<String, Object> inputData = Map.of("types", types);
        final SynchronizeCompanyUseCase.Input input = new SynchronizeCompanyUseCase.Input(companyId, inputData);

        useCase.execute(input);

        final Optional<Company> savedCompany = companyRepository.findById(CompanyId.with(companyId));
        assertTrue(savedCompany.isPresent(), "Company should be saved");
        assertEquals(companyId, savedCompany.get().getCompanyId().value());
        
        final Set<String> savedTypes = savedCompany.get().types();
        assertEquals(2, savedTypes.size());
        assertTrue(savedTypes.contains("LOGISTICS_PROVIDER"));
        assertTrue(savedTypes.contains("CARRIER"));
    }

    @Test
    @DisplayName("Should update existing company when company exists")
    void shouldUpdateExistingCompanyWhenCompanyExists() {
        final UUID companyId = UUID.randomUUID();
        final Company existingCompany = CompanyTestDataBuilder.aCompany()
                .withCompanyId(companyId)
                .withTypes("CARRIER")
                .build();
        companyRepository.save(existingCompany);

        final List<String> newTypes = List.of("LOGISTICS_PROVIDER", "SHIPPER");
        final Map<String, Object> inputData = Map.of("types", newTypes);
        final SynchronizeCompanyUseCase.Input input = new SynchronizeCompanyUseCase.Input(companyId, inputData);
        useCase.execute(input);

        final Optional<Company> updatedCompany = companyRepository.findById(CompanyId.with(companyId));
        assertTrue(updatedCompany.isPresent(), "Company should exist");
        
        final Set<String> updatedTypes = updatedCompany.get().types();
        assertEquals(2, updatedTypes.size());
        assertTrue(updatedTypes.contains("LOGISTICS_PROVIDER"));
        assertTrue(updatedTypes.contains("SHIPPER"));
    }

    @Test
    @DisplayName("Should not save anything when data is null")
    void shouldNotSaveWhenDataIsNull() {
        final UUID companyId = UUID.randomUUID();
        final SynchronizeCompanyUseCase.Input input = new SynchronizeCompanyUseCase.Input(companyId, null);

        useCase.execute(input);

        assertEquals(0, companyRepository.count(), "No company should be saved");
        assertFalse(companyRepository.existsById(CompanyId.with(companyId)));
    }

    @Test
    @DisplayName("Should not save anything when data does not contain types key")
    void shouldNotSaveWhenDataDoesNotContainTypesKey() {
        final UUID companyId = UUID.randomUUID();
        final Map<String, Object> inputData = Map.of("someOtherKey", "someValue");
        final SynchronizeCompanyUseCase.Input input = new SynchronizeCompanyUseCase.Input(companyId, inputData);

        useCase.execute(input);

        assertEquals(0, companyRepository.count(), "No company should be saved");
        assertFalse(companyRepository.existsById(CompanyId.with(companyId)));
    }

    @Test
    @DisplayName("Should not save anything when data is empty")
    void shouldNotSaveWhenDataIsEmpty() {
        final UUID companyId = UUID.randomUUID();
        final Map<String, Object> inputData = Map.of();
        final SynchronizeCompanyUseCase.Input input = new SynchronizeCompanyUseCase.Input(companyId, inputData);

        useCase.execute(input);

        assertEquals(0, companyRepository.count(), "No company should be saved");
        assertFalse(companyRepository.existsById(CompanyId.with(companyId)));
    }

    @Test
    @DisplayName("Should handle empty types list")
    void shouldHandleEmptyTypesList() {
        final UUID companyId = UUID.randomUUID();
        final List<String> emptyTypes = List.of();
        final Map<String, Object> inputData = Map.of("types", emptyTypes);
        final SynchronizeCompanyUseCase.Input input = new SynchronizeCompanyUseCase.Input(companyId, inputData);

        useCase.execute(input);

        final Optional<Company> savedCompany = companyRepository.findById(CompanyId.with(companyId));
        assertTrue(savedCompany.isPresent(), "Company should be saved even with empty types");
        assertTrue(savedCompany.get().types().isEmpty(), "Types should be empty");
    }

    @Test
    @DisplayName("Should preserve other data fields when updating existing company")
    void shouldPreserveOtherDataFieldsWhenUpdating() {
        final UUID companyId = UUID.randomUUID();
        final Map<String, Object> initialData = new HashMap<>();
        initialData.put("types", List.of("CARRIER"));
        initialData.put("name", "Test Company");
        initialData.put("address", "123 Test St");
        
        final Company existingCompany = Company.createCompany(companyId, initialData);
        companyRepository.save(existingCompany);

        final List<String> newTypes = List.of("LOGISTICS_PROVIDER");
        final Map<String, Object> inputData = Map.of("types", newTypes);
        final SynchronizeCompanyUseCase.Input input = new SynchronizeCompanyUseCase.Input(companyId, inputData);
        useCase.execute(input);

        final Optional<Company> updatedCompany = companyRepository.findById(CompanyId.with(companyId));
        assertTrue(updatedCompany.isPresent());
        
        final Map<String, Object> updatedData = updatedCompany.get().getData().value();
        assertTrue(updatedData.containsKey("name"), "Original name field should be preserved");
        assertTrue(updatedData.containsKey("address"), "Original address field should be preserved");
        assertEquals("Test Company", updatedData.get("name"));
        assertEquals("123 Test St", updatedData.get("address"));
        
        final Set<String> types = updatedCompany.get().types();
        assertEquals(1, types.size());
        assertTrue(types.contains("LOGISTICS_PROVIDER"));
    }

    @Test
    @DisplayName("Should handle single type value")
    void shouldHandleSingleTypeValue() {
        final UUID companyId = UUID.randomUUID();
        final List<String> singleType = List.of("SHIPPER");
        final Map<String, Object> inputData = Map.of("types", singleType);
        final SynchronizeCompanyUseCase.Input input = new SynchronizeCompanyUseCase.Input(companyId, inputData);

        useCase.execute(input);

        final Optional<Company> savedCompany = companyRepository.findById(CompanyId.with(companyId));
        assertTrue(savedCompany.isPresent());
        
        final Set<String> savedTypes = savedCompany.get().types();
        assertEquals(1, savedTypes.size());
        assertTrue(savedTypes.contains("SHIPPER"));
    }

    @Test
    @DisplayName("Should verify company is logistics provider after synchronization")
    void shouldVerifyCompanyIsLogisticsProviderAfterSynchronization() {
        final UUID companyId = UUID.randomUUID();
        final List<String> types = List.of("LOGISTICS_PROVIDER", "CARRIER");
        final Map<String, Object> inputData = Map.of("types", types);
        final SynchronizeCompanyUseCase.Input input = new SynchronizeCompanyUseCase.Input(companyId, inputData);

        useCase.execute(input);

        final Optional<Company> savedCompany = companyRepository.findById(CompanyId.with(companyId));
        assertTrue(savedCompany.isPresent());
        assertTrue(savedCompany.get().isLogisticsProvider(), 
            "Company should be identified as logistics provider");
    }
}
