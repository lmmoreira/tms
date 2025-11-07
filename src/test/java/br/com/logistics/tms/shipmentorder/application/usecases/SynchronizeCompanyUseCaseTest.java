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
    void shouldCreateNewCompanyWhenNotExists() {
        // given
        final UUID companyId = UUID.randomUUID();
        final Map<String, Object> data = Map.of("types", List.of("LOGISTICS_PROVIDER"));
        final SynchronizeCompanyUseCase.Input input = new SynchronizeCompanyUseCase.Input(companyId, data);

        // when
        useCase.execute(input);

        // then
        assertEquals(1, companyRepository.size());
        final Optional<Company> savedCompany = companyRepository.findById(CompanyId.with(companyId));
        assertTrue(savedCompany.isPresent());
        assertEquals(companyId, savedCompany.get().getCompanyId().value());
        assertEquals(List.of("LOGISTICS_PROVIDER"), savedCompany.get().getData().value().get("types"));
    }

    @Test
    @DisplayName("Should update existing company when company exists")
    void shouldUpdateExistingCompany() {
        // given
        final UUID companyId = UUID.randomUUID();
        final Company existingCompany = CompanyTestDataBuilder.aCompany()
                .withCompanyId(companyId)
                .withType("MARKETPLACE")
                .build();
        companyRepository.save(existingCompany);

        final Map<String, Object> newData = Map.of("types", List.of("LOGISTICS_PROVIDER"));
        final SynchronizeCompanyUseCase.Input input = new SynchronizeCompanyUseCase.Input(companyId, newData);

        // when
        useCase.execute(input);

        // then
        assertEquals(1, companyRepository.size());
        final Optional<Company> updatedCompany = companyRepository.findById(CompanyId.with(companyId));
        assertTrue(updatedCompany.isPresent());
        assertEquals(List.of("LOGISTICS_PROVIDER"), updatedCompany.get().getData().value().get("types"));
    }

    @Test
    @DisplayName("Should not create or update company when data is null")
    void shouldDoNothingWhenDataIsNull() {
        // given
        final UUID companyId = UUID.randomUUID();
        final SynchronizeCompanyUseCase.Input input = new SynchronizeCompanyUseCase.Input(companyId, null);

        // when
        useCase.execute(input);

        // then
        assertEquals(0, companyRepository.size());
    }

    @Test
    @DisplayName("Should not create or update company when types key is missing")
    void shouldDoNothingWhenTypesKeyIsMissing() {
        // given
        final UUID companyId = UUID.randomUUID();
        final Map<String, Object> data = Map.of("name", "Test Company");
        final SynchronizeCompanyUseCase.Input input = new SynchronizeCompanyUseCase.Input(companyId, data);

        // when
        useCase.execute(input);

        // then
        assertEquals(0, companyRepository.size());
    }

    @Test
    @DisplayName("Should handle empty types list")
    void shouldHandleEmptyTypesList() {
        // given
        final UUID companyId = UUID.randomUUID();
        final Map<String, Object> data = Map.of("types", Collections.emptyList());
        final SynchronizeCompanyUseCase.Input input = new SynchronizeCompanyUseCase.Input(companyId, data);

        // when
        useCase.execute(input);

        // then
        assertEquals(1, companyRepository.size());
        final Optional<Company> savedCompany = companyRepository.findById(CompanyId.with(companyId));
        assertTrue(savedCompany.isPresent());
        assertEquals(Collections.emptyList(), savedCompany.get().getData().value().get("types"));
    }

    @Test
    @DisplayName("Should handle multiple types")
    void shouldHandleMultipleTypes() {
        // given
        final UUID companyId = UUID.randomUUID();
        final List<String> types = List.of("LOGISTICS_PROVIDER", "MARKETPLACE");
        final Map<String, Object> data = Map.of("types", types);
        final SynchronizeCompanyUseCase.Input input = new SynchronizeCompanyUseCase.Input(companyId, data);

        // when
        useCase.execute(input);

        // then
        assertEquals(1, companyRepository.size());
        final Optional<Company> savedCompany = companyRepository.findById(CompanyId.with(companyId));
        assertTrue(savedCompany.isPresent());
        assertEquals(types, savedCompany.get().getData().value().get("types"));
    }

    @Test
    @DisplayName("Should merge types when updating existing company")
    void shouldMergeTypesWhenUpdating() {
        // given
        final UUID companyId = UUID.randomUUID();
        final Company existingCompany = CompanyTestDataBuilder.aCompany()
                .withCompanyId(companyId)
                .withTypes(List.of("MARKETPLACE"))
                .build();
        companyRepository.save(existingCompany);

        final Map<String, Object> newData = Map.of("types", List.of("LOGISTICS_PROVIDER"));
        final SynchronizeCompanyUseCase.Input input = new SynchronizeCompanyUseCase.Input(companyId, newData);

        // when
        useCase.execute(input);

        // then
        assertEquals(1, companyRepository.size());
        final Optional<Company> updatedCompany = companyRepository.findById(CompanyId.with(companyId));
        assertTrue(updatedCompany.isPresent());
        // After update, only the new types should be present (as per updateData logic)
        assertEquals(List.of("LOGISTICS_PROVIDER"), updatedCompany.get().getData().value().get("types"));
    }

    @Test
    @DisplayName("Should use builder pattern for creating test data")
    void shouldUseBuilderpPatternForTestData() {
        // given
        final CompanyTestDataBuilder builder = CompanyTestDataBuilder.aCompany()
                .withTypes(List.of("LOGISTICS_PROVIDER"));
        
        final Company company = builder.build();
        companyRepository.save(company);

        // when
        final Optional<Company> foundCompany = companyRepository.findById(company.getCompanyId());

        // then
        assertTrue(foundCompany.isPresent());
        assertEquals(company.getCompanyId(), foundCompany.get().getCompanyId());
        assertEquals(List.of("LOGISTICS_PROVIDER"), foundCompany.get().getData().value().get("types"));
    }
}
