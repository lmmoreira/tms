# Testing Patterns and Examples

This document provides comprehensive testing patterns for TMS.

---

## Unit Tests (Domain Layer)

### Testing Aggregates

**Location:** `company/domain/CompanyTest.java`

```java
package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.exception.ValidationException;
import br.com.logistics.tms.company.domain.events.CompanyCreated;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Company aggregate.
 * Pure domain logic, no Spring context.
 */
class CompanyTest {

    @Test
    void shouldCreateCompanyWithValidData() {
        // Given
        String name = "Test Company";
        String cnpj = "12345678901234";
        Set<CompanyType> types = Set.of(CompanyType.MARKETPLACE);
        Map<String, Object> config = Map.of("key", "value");

        // When
        Company company = Company.createCompany(name, cnpj, types, config);

        // Then
        assertNotNull(company.getCompanyId());
        assertEquals(name, company.getName());
        assertEquals(cnpj, company.getCnpj().value());
        assertEquals(types, company.getTypes());
        assertEquals(config, company.getConfiguration());
    }

    @Test
    void shouldPlaceDomainEventWhenCreated() {
        // Given
        Company company = Company.createCompany("Test", "12345678901234", 
            Set.of(CompanyType.MARKETPLACE), Map.of());

        // When/Then
        assertEquals(1, company.getDomainEvents().size());
        assertTrue(company.getDomainEvents().stream()
            .anyMatch(e -> e instanceof CompanyCreated));
    }

    @Test
    void shouldThrowExceptionForInvalidName() {
        // When/Then
        assertThrows(ValidationException.class, () -> 
            Company.createCompany("", "12345678901234", Set.of(CompanyType.MARKETPLACE), Map.of())
        );
    }

    @Test
    void shouldReturnNewInstanceWhenUpdatingName() {
        // Given
        Company original = Company.createCompany("Original", "12345678901234",
            Set.of(CompanyType.MARKETPLACE), Map.of());

        // When
        Company updated = original.updateName("Updated");

        // Then
        assertNotSame(original, updated);
        assertEquals("Original", original.getName());
        assertEquals("Updated", updated.getName());
    }

    @Test
    void shouldReturnSameInstanceWhenNameUnchanged() {
        // Given
        Company company = Company.createCompany("Test", "12345678901234",
            Set.of(CompanyType.MARKETPLACE), Map.of());

        // When
        Company same = company.updateName("Test");

        // Then
        assertSame(company, same);
    }

    @Test
    void shouldAddConfiguration() {
        // Given
        Company company = Company.createCompany("Test", "12345678901234",
            Set.of(CompanyType.MARKETPLACE), Map.of());

        // When
        Company updated = company.addConfiguration("newKey", "newValue");

        // Then
        assertTrue(updated.getConfiguration().containsKey("newKey"));
        assertEquals("newValue", updated.getConfiguration().get("newKey"));
    }

    @Test
    void shouldThrowExceptionForDuplicateConfigurationKey() {
        // Given
        Company company = Company.createCompany("Test", "12345678901234",
            Set.of(CompanyType.MARKETPLACE), Map.of("key", "value"));

        // When/Then
        assertThrows(ValidationException.class, () ->
            company.addConfiguration("key", "newValue")
        );
    }

    @Test
    void shouldValidateBusinessRule() {
        // Given
        Company marketplace = Company.createCompany("Test", "12345678901234",
            Set.of(CompanyType.MARKETPLACE), Map.of());
        Company carrier = Company.createCompany("Test", "12345678901235",
            Set.of(CompanyType.CARRIER), Map.of());

        // When/Then
        assertTrue(marketplace.canCreateShipmentOrders());
        assertFalse(carrier.canCreateShipmentOrders());
    }
}
```

### Testing Value Objects

```java
package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CnpjTest {

    @Test
    void shouldCreateValidCnpj() {
        // When
        Cnpj cnpj = new Cnpj("12345678901234");

        // Then
        assertEquals("12345678901234", cnpj.value());
    }

    @Test
    void shouldThrowExceptionForInvalidCnpj() {
        assertThrows(ValidationException.class, () -> new Cnpj("123"));
        assertThrows(ValidationException.class, () -> new Cnpj(null));
        assertThrows(ValidationException.class, () -> new Cnpj(""));
    }

    @Test
    void shouldBeEqualWhenSameValue() {
        // Given
        Cnpj cnpj1 = new Cnpj("12345678901234");
        Cnpj cnpj2 = new Cnpj("12345678901234");

        // Then
        assertEquals(cnpj1, cnpj2);
        assertEquals(cnpj1.hashCode(), cnpj2.hashCode());
    }
}
```

---

## Integration Tests (Use Cases)

### Testing Use Cases with Mock Repository

**Location:** `company/application/usecases/CreateCompanyUseCaseTest.java`

```java
package br.com.logistics.tms.company.application.usecases;

import br.com.logistics.tms.commons.exception.ValidationException;
import br.com.logistics.tms.company.application.repositories.CompanyRepository;
import br.com.logistics.tms.company.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CreateCompanyUseCase.
 * Uses mocks, no Spring context.
 */
@ExtendWith(MockitoExtension.class)
class CreateCompanyUseCaseTest {

    @Mock
    private CompanyRepository companyRepository;

    private CreateCompanyUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateCompanyUseCase(companyRepository);
    }

    @Test
    void shouldCreateCompanySuccessfully() {
        // Given
        CreateCompanyUseCase.Input input = new CreateCompanyUseCase.Input(
            "Test Company",
            "12345678901234",
            Set.of(CompanyType.MARKETPLACE),
            Map.of()
        );

        when(companyRepository.getCompanyByCnpj(any())).thenReturn(Optional.empty());
        when(companyRepository.create(any())).thenAnswer(inv -> inv.getArgument(0));

        // When
        CreateCompanyUseCase.Output output = useCase.execute(input);

        // Then
        assertNotNull(output.companyId());
        assertEquals("Test Company", output.name());
        assertEquals("12345678901234", output.cnpj());

        verify(companyRepository).getCompanyByCnpj(any());
        verify(companyRepository).create(any());
    }

    @Test
    void shouldThrowExceptionWhenCompanyAlreadyExists() {
        // Given
        CreateCompanyUseCase.Input input = new CreateCompanyUseCase.Input(
            "Test Company",
            "12345678901234",
            Set.of(CompanyType.MARKETPLACE),
            Map.of()
        );

        Company existingCompany = Company.createCompany("Existing", "12345678901234",
            Set.of(CompanyType.MARKETPLACE), Map.of());
        when(companyRepository.getCompanyByCnpj(any())).thenReturn(Optional.of(existingCompany));

        // When/Then
        assertThrows(ValidationException.class, () -> useCase.execute(input));
        verify(companyRepository, never()).create(any());
    }

    @Test
    void shouldValidateRequiredFields() {
        // Given
        CreateCompanyUseCase.Input input = new CreateCompanyUseCase.Input(
            "",
            "12345678901234",
            Set.of(CompanyType.MARKETPLACE),
            Map.of()
        );

        // When/Then
        assertThrows(ValidationException.class, () -> useCase.execute(input));
    }
}
```

---

## Full Integration Tests with Testcontainers

### Testing Complete Flow

**Location:** `company/CompanyIntegrationTest.java`

```java
package br.com.logistics.tms.company;

import br.com.logistics.tms.company.application.repositories.CompanyRepository;
import br.com.logistics.tms.company.application.usecases.CreateCompanyUseCase;
import br.com.logistics.tms.company.application.usecases.GetCompanyByIdUseCase;
import br.com.logistics.tms.company.domain.CompanyType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Full integration test with real database and messaging.
 */
@SpringBootTest
@Testcontainers
class CompanyIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("tms_test")
        .withUsername("test")
        .withPassword("test");

    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
    }

    @Autowired
    private CreateCompanyUseCase createCompanyUseCase;

    @Autowired
    private GetCompanyByIdUseCase getCompanyByIdUseCase;

    @Autowired
    private CompanyRepository companyRepository;

    @Test
    void shouldCreateAndRetrieveCompany() {
        // Given
        CreateCompanyUseCase.Input createInput = new CreateCompanyUseCase.Input(
            "Integration Test Company",
            "98765432109876",
            Set.of(CompanyType.MARKETPLACE, CompanyType.SHIPPER),
            Map.of("apiKey", "test123")
        );

        // When - Create
        CreateCompanyUseCase.Output createOutput = createCompanyUseCase.execute(createInput);

        // Then - Verify creation
        assertNotNull(createOutput.companyId());
        assertEquals("Integration Test Company", createOutput.name());

        // When - Retrieve
        GetCompanyByIdUseCase.Input getInput = new GetCompanyByIdUseCase.Input(createOutput.companyId());
        GetCompanyByIdUseCase.Output getOutput = getCompanyByIdUseCase.execute(getInput);

        // Then - Verify retrieval
        assertEquals(createOutput.companyId(), getOutput.companyId());
        assertEquals("Integration Test Company", getOutput.name());
        assertEquals("98765432109876", getOutput.cnpj());
        assertTrue(getOutput.types().contains(CompanyType.MARKETPLACE));
        assertEquals("test123", getOutput.configuration().get("apiKey"));
    }

    @Test
    void shouldHandleConcurrentUpdates() throws InterruptedException {
        // Test optimistic locking, versioning, etc.
    }
}
```

---

## REST API Tests

**Location:** `company/infrastructure/rest/CreateControllerTest.java`

```java
package br.com.logistics.tms.company.infrastructure.rest;

import br.com.logistics.tms.company.domain.CompanyType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * REST API integration tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class CreateControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateCompanyViaRestApi() throws Exception {
        // Given
        Map<String, Object> requestBody = Map.of(
            "name", "REST Test Company",
            "cnpj", "11122233344455",
            "types", Set.of("MARKETPLACE"),
            "configuration", Map.of("key", "value")
        );

        // When/Then
        mockMvc.perform(post("/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.company_id").exists())
            .andExpect(jsonPath("$.name").value("REST Test Company"))
            .andExpect(jsonPath("$.cnpj").value("11122233344455"));
    }

    @Test
    void shouldReturn400ForInvalidRequest() throws Exception {
        // Given
        Map<String, Object> invalidRequest = Map.of("name", "");

        // When/Then
        mockMvc.perform(post("/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }
}
```

---

## Event-Driven Tests

```java
@SpringBootTest
@Testcontainers
class EventDrivenIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @Autowired
    private CreateShipmentOrderUseCase createShipmentOrderUseCase;

    @Autowired
    private CompanyRepository companyRepository;

    @Test
    void shouldIncrementCompanyCounterWhenOrderCreated() throws InterruptedException {
        // Given - Create company
        Company company = Company.createCompany("Test", "12345678901234",
            Set.of(CompanyType.MARKETPLACE), Map.of());
        company = companyRepository.create(company);

        int initialCounter = company.getOrderCounter();

        // When - Create shipment order (triggers event)
        createShipmentOrderUseCase.execute(
            new CreateShipmentOrderUseCase.Input(company.getCompanyId().value(), "ORDER-001")
        );

        // Then - Wait for event processing and verify counter incremented
        Thread.sleep(2000); // Wait for async event processing

        Company updated = companyRepository.getCompanyById(company.getCompanyId()).orElseThrow();
        assertEquals(initialCounter + 1, updated.getOrderCounter());
    }
}
```

---

## Test Utilities

### Test Data Builders

```java
public class CompanyTestBuilder {
    
    private String name = "Test Company";
    private String cnpj = "12345678901234";
    private Set<CompanyType> types = Set.of(CompanyType.MARKETPLACE);
    private Map<String, Object> config = Map.of();

    public static CompanyTestBuilder aCompany() {
        return new CompanyTestBuilder();
    }

    public CompanyTestBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public CompanyTestBuilder withCnpj(String cnpj) {
        this.cnpj = cnpj;
        return this;
    }

    public CompanyTestBuilder withTypes(Set<CompanyType> types) {
        this.types = types;
        return this;
    }

    public Company build() {
        return Company.createCompany(name, cnpj, types, config);
    }
}
```

---

## Key Testing Principles

✅ **Unit Tests:** Fast, pure domain logic, no dependencies
✅ **Integration Tests:** Use Testcontainers for real dependencies
✅ **Test Immutability:** Verify new instances are returned
✅ **Test Events:** Verify domain events are placed correctly
✅ **Test Validation:** Cover all validation rules
✅ **Test Business Logic:** Verify domain behavior
✅ **REST Tests:** Use MockMvc for API testing
✅ **Event Tests:** Verify async event processing
