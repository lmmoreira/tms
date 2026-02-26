# Test Structure and Organization

**Guidelines for organizing test files in the TMS project - Updated with centralized test utilities.**

---

## Overview

The test directory structure uses **centralized test utilities** at the root level for maximum reusability across unit and integration tests, while keeping the main test files organized by module.

### Parallel Test Execution

**Unit and integration tests run in parallel for optimal performance:**

- **Unit Tests (*Test.java):** Run via Surefire with 8 parallel threads
- **Integration Tests (*IT.java):** Run via Failsafe with 4 parallel threads  
- **Shared Testcontainers:** Single PostgreSQL + RabbitMQ instance via `TestContainersManager` singleton
- **Test Isolation:** Each integration test gets fresh Spring context + fixture cleanup

---

## Directory Structure

```
src/test/java/br/com/logistics/tms/
│
├── assertions/                              # Custom AssertJ assertions (CENTRALIZED)
│   ├── domain/                              # Domain object assertions
│   │   ├── company/                         # Company module domain
│   │   │   └── CompanyAssert.java           # (Future)
│   │   └── shipmentorder/                   # ShipmentOrder module domain
│   │       └── CompanyAssert.java           # (Future)
│   ├── jpa/                                 # JPA entity assertions
│   │   ├── CompanyEntityAssert.java
│   │   ├── ShipmentOrderEntityAssert.java
│   │   └── ShipmentOrderCompanyEntityAssert.java
│   └── outbox/                              # Infrastructure assertions
│       └── OutboxAssert.java
│
├── builders/                                # Test object builders (CENTRALIZED)
│   ├── dto/                                 # REST DTOs
│   │   ├── CreateCompanyDTOBuilder.java
│   │   ├── UpdateCompanyDTOBuilder.java
│   │   └── CreateShipmentOrderDTOBuilder.java
│   ├── input/                               # Use case inputs
│   │   └── SynchronizeCompanyInputBuilder.java
│   └── domain/                              # Domain aggregates
│       ├── company/                         # Company module
│       │   └── CompanyBuilder.java          # (Future)
│       └── shipmentorder/                   # ShipmentOrder module
│           └── CompanyBuilder.java
│
├── utils/                                   # Test utilities (CENTRALIZED)
│   └── CnpjGenerator.java
│
├── integration/                             # Integration tests
│   ├── fixtures/                            # Integration-specific helpers
│   │   ├── CompanyIntegrationFixture.java
│   │   └── ShipmentOrderIntegrationFixture.java
│   ├── CompanyShipmentOrderIT.java          # (*IT.java suffix)
│   └── TmsApplicationIT.java
│
├── {module}/                                # Module tests
│   ├── application/
│   │   ├── repositories/                    # Fake repositories
│   │   │   └── Fake{Entity}Repository.java
│   │   └── usecases/
│   │       └── {UseCase}Test.java           # Unit tests (*Test.java)
│   └── domain/
│       └── {Aggregate}Test.java             # Domain unit tests
│
├── architecture/                            # Architecture rules (*Test.java)
│   └── {ArchUnit tests}
│
├── AbstractIntegrationTest.java             # Base class for IT tests
└── TestContainersManager.java               # Singleton container manager
```

---

## Package Locations

### 1. Custom Assertions (CENTRALIZED)

**Location:** `assertions/{domain|jpa|outbox}`

**Why:** Assertions are reusable test utilities that should be accessible from both unit and integration tests. Centralizing them at the root level eliminates duplication and provides a single source of truth.

**Structure:**
```
assertions/
├── domain/              # Domain object assertions (for unit tests)
│   ├── company/
│   └── shipmentorder/
├── jpa/                 # JPA entity assertions (for integration tests)
└── outbox/              # Infrastructure assertions
```

**Example:**
```
src/test/java/br/com/logistics/tms/assertions/jpa/
└── CompanyEntityAssert.java
```

**Usage:**
```java
import static br.com.logistics.tms.assertions.jpa.CompanyEntityAssert.assertThatCompany;

assertThatCompany(entity)
    .hasName("Test Company")
    .hasTypes(CompanyType.SELLER);
```

---

### 2. Test Builders (CENTRALIZED)

**Location:** `builders/{dto|input|domain}`

**Why:** Test builders reduce boilerplate and should be reusable across all test types. The clear categorization (dto/input/domain) makes it obvious what each builder creates.

**Structure:**
```
builders/
├── dto/                # REST DTOs (for integration tests)
├── input/              # Use case inputs (for unit tests)
└── domain/             # Domain aggregates (for unit tests)
    ├── company/
    └── shipmentorder/
```

**Examples:**
```
src/test/java/br/com/logistics/tms/builders/dto/
└── CreateCompanyDTOBuilder.java              # DTO builder

src/test/java/br/com/logistics/tms/builders/input/
└── SynchronizeCompanyInputBuilder.java       # Use case input builder

src/test/java/br/com/logistics/tms/builders/domain/shipmentorder/
└── CompanyBuilder.java                       # Domain aggregate builder
```

**Usage:**
```java
// DTO Builder (integration tests)
import br.com.logistics.tms.builders.dto.CreateCompanyDTOBuilder;

final CreateCompanyDTO dto = CreateCompanyDTOBuilder.aCreateCompanyDTO()
    .withName("Test")
    .build();

// Domain Builder (unit tests)
import br.com.logistics.tms.builders.domain.shipmentorder.CompanyBuilder;

final Company company = CompanyBuilder.aCompany()
    .withCompanyId(id)
    .build();

// Input Builder (unit tests)
import br.com.logistics.tms.builders.input.SynchronizeCompanyInputBuilder;

final Input input = SynchronizeCompanyInputBuilder.anInput()
    .withCompanyId(id)
    .build();
```

---

### 3. Test Utilities (CENTRALIZED)

**Location:** `utils/`

**Why:** Generic utilities like generators should be accessible from all tests.

**Example:**
```
src/test/java/br/com/logistics/tms/utils/
└── CnpjGenerator.java
```

**Usage:**
```java
import br.com.logistics.tms.utils.CnpjGenerator;

final String cnpj = CnpjGenerator.randomCnpj();
```

---

### 4. Integration Fixtures (MODULE-SPECIFIC)

**Location:** `integration/fixtures/`

**Why:** Integration fixtures are tightly coupled to integration tests and use MockMvc, repositories, and other integration-specific concerns. They remain in the integration package.

**Example:**
```
src/test/java/br/com/logistics/tms/integration/fixtures/
├── CompanyIntegrationFixture.java
└── ShipmentOrderIntegrationFixture.java
```

---

### 5. Fake Repositories (MODULE-SPECIFIC)

**Location:** `{module}.application.repositories`

**Why:** Fake repositories implement repository interfaces from the application layer, so they are placed alongside the interface they implement in the test structure.

**Example:**
```
src/test/java/br/com/logistics/tms/shipmentorder/application/repositories/
└── FakeCompanyRepository.java
```

**Implements:**
```
src/main/java/br/com/logistics/tms/shipmentorder/application/repositories/
└── CompanyRepository.java (interface)
```

### 2. Domain Test Data Builders

**Location:** `{module}.domain`

**Why:** Test data builders for domain aggregates are placed in the domain package within the test structure, as they create domain objects.

**Example:**
```
src/test/java/br/com/logistics/tms/shipmentorder/domain/
└── CompanyTestDataBuilder.java
```

**Builds:**
```
src/main/java/br/com/logistics/tms/shipmentorder/domain/
└── Company.java (aggregate)
```

### 3. Use Case Input Builders

**Location:** `{module}.application.usecases.data`

**Why:** Input builders are placed alongside the use case tests they support, making them easy to discover and maintain.

**Example:**
```
src/test/java/br/com/logistics/tms/shipmentorder/application/usecases/data/
└── SynchronizeCompanyUseCaseInputDataBuilder.java
```

**Used by:**
```
src/test/java/br/com/logistics/tms/shipmentorder/application/usecases/
└── SynchronizeCompanyUseCaseTest.java
```

### 4. Use Case Tests

**Location:** `{module}.application.usecases`

**Example:**
```
src/test/java/br/com/logistics/tms/shipmentorder/application/usecases/
└── SynchronizeCompanyUseCaseTest.java
```

### 5. Domain Tests

**Location:** `{module}.domain`

**Example:**
```
src/test/java/br/com/logistics/tms/shipmentorder/domain/
└── CompanyTest.java
```

### 6. Integration Tests

**Location:** `integration/`

**Naming Convention:** `*IT.java` (e.g., `CompanyShipmentOrderIT.java`)

**Why:** Integration tests validate complete business flows across modules. They are placed in a separate `integration` package to distinguish them from unit tests.

**Execution:** Run via **maven-failsafe-plugin** with 4 parallel threads

**Example:**
```
src/test/java/br/com/logistics/tms/integration/
├── CompanyShipmentOrderIT.java           # Integration test
├── fixtures/
│   ├── CompanyIntegrationFixture.java
│   └── ShipmentOrderIntegrationFixture.java
├── assertions/
│   ├── CompanyEntityAssert.java
│   ├── ShipmentOrderEntityAssert.java
│   └── ShipmentOrderCompanyEntityAssert.java
└── data/
    ├── CreateCompanyDTODataBuilder.java
    ├── UpdateCompanyDTODataBuilder.java
    └── CreateShipmentOrderDTODataBuilder.java
```

**See:** `/doc/ai/INTEGRATION_TESTS.md` for complete integration test documentation.

---

## Migration History

### Previous Structure (Deprecated)

The old structure had a generic `data` package that mixed different types of test utilities:

```
❌ OLD STRUCTURE (Don't use)
src/test/java/br/com/logistics/tms/{module}/
└── data/                              # ❌ Mixed utilities
    ├── FakeCompanyRepository.java     # ❌ Should be in application/repositories
    └── CompanyTestDataBuilder.java    # ❌ Should be in domain
```

### Current Structure (Correct)

```
✅ NEW STRUCTURE (Use this)
src/test/java/br/com/logistics/tms/{module}/
├── application/
│   └── repositories/                  # ✅ Fake repositories here
│       └── FakeCompanyRepository.java
└── domain/                            # ✅ Domain builders here
    └── CompanyTestDataBuilder.java
```

**Why the change?**
1. **Better organization:** Each test utility is placed where it logically belongs
2. **Clearer semantics:** Immediately obvious what each utility does
3. **Mirrors main structure:** Test package structure aligns with main source structure
4. **Easier discovery:** Developers can find test utilities intuitively

---

## Naming Conventions

| Type | Naming Pattern | Maven Plugin | Example |
|------|----------------|--------------|---------|
| **Unit Test** | `*Test.java` | Surefire (8 threads) | `CompanyTest.java` |
| **Integration Test** | `*IT.java` | Failsafe (4 threads) | `CompanyShipmentOrderIT.java` |
| **ArchUnit Test** | `*Test.java` | Surefire (8 threads) | `LayerArchitectureTest.java` |
| **Fake Repository** | `Fake{Entity}Repository` | - | `FakeCompanyRepository` |
| **Domain Builder** | `{Entity}Builder` | - | `CompanyBuilder` |
| **DTO Builder** | `{Operation}{Entity}DTOBuilder` | - | `CreateCompanyDTOBuilder` |
| **Input Builder** | `{UseCase}InputBuilder` | - | `SynchronizeCompanyInputBuilder` |
| **Integration Fixture** | `{Entity}IntegrationFixture` | - | `CompanyIntegrationFixture` |
| **JPA Assertion** | `{Entity}EntityAssert` | - | `CompanyEntityAssert` |
| **Domain Assertion** | `{Entity}Assert` | - | `CompanyAssert` |

**Critical:** 
- Use `*IT.java` suffix for integration tests to ensure they run via Failsafe
- Builders use "Builder" suffix (NOT "DataBuilder")
- Clear distinction: DTOBuilder vs Builder (domain) vs InputBuilder

---

## Complete Example

### Test Structure for ShipmentOrder Module

```
src/test/java/br/com/logistics/tms/
│
├── assertions/                                      # CENTRALIZED
│   ├── jpa/
│   │   └── ShipmentOrderCompanyEntityAssert.java    # JPA entity assertion
│   └── domain/shipmentorder/
│       └── CompanyAssert.java                       # Domain object assertion (future)
│
├── builders/                                        # CENTRALIZED
│   ├── dto/
│   │   └── CreateShipmentOrderDTOBuilder.java       # DTO builder
│   ├── input/
│   │   └── SynchronizeCompanyInputBuilder.java      # Use case input builder
│   └── domain/shipmentorder/
│       └── CompanyBuilder.java                      # Domain aggregate builder
│
├── utils/                                           # CENTRALIZED
│   └── CnpjGenerator.java                           # Utility
│
├── integration/                                     # Integration tests
│   ├── fixtures/
│   │   └── ShipmentOrderIntegrationFixture.java     # Integration fixture
│   └── CompanyShipmentOrderIT.java                  # Integration test
│
└── shipmentorder/                                   # Module tests
    ├── application/
    │   ├── repositories/
    │   │   └── FakeCompanyRepository.java           # Fake repository
    │   └── usecases/
    │       └── SynchronizeCompanyUseCaseTest.java   # Use case test
    └── domain/
        └── CompanyTest.java                         # Domain test (future)
```

### Imports in Tests

```java
// Unit Test (SynchronizeCompanyUseCaseTest.java)
import br.com.logistics.tms.builders.input.SynchronizeCompanyInputBuilder;
import br.com.logistics.tms.builders.domain.shipmentorder.CompanyBuilder;
import br.com.logistics.tms.shipmentorder.application.repositories.FakeCompanyRepository;

// Integration Test (CompanyShipmentOrderIT.java)
import br.com.logistics.tms.assertions.jpa.CompanyEntityAssert;
import br.com.logistics.tms.builders.dto.CreateCompanyDTOBuilder;
import br.com.logistics.tms.utils.CnpjGenerator;
```

---

## Parallel Execution Details

### Maven Configuration

**Surefire Plugin (Unit Tests):**
```xml
<plugin>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <parallel>classes</parallel>
        <threadCount>8</threadCount>
        <forkCount>1</forkCount>
        <reuseForks>true</reuseForks>
        <includes>
            <include>**/*Test.java</include>
        </includes>
        <excludes>
            <exclude>**/*IT.java</exclude>
        </excludes>
    </configuration>
</plugin>
```

**Failsafe Plugin (Integration Tests):**
```xml
<plugin>
    <artifactId>maven-failsafe-plugin</artifactId>
    <configuration>
        <parallel>classes</parallel>
        <threadCount>4</threadCount>
        <forkCount>1</forkCount>
        <reuseForks>true</reuseForks>
        <includes>
            <include>**/*IT.java</include>
        </includes>
    </configuration>
</plugin>
```

### Testcontainers Singleton Pattern

**TestcontainersManager:**
- Thread-safe singleton via synchronized getInstance()
- Single PostgreSQL container shared across all tests
- Single RabbitMQ container shared across all tests
- Flyway migrations run once on container startup
- Containers stopped on JVM shutdown via shutdown hook
- Network shared between containers for inter-container communication

**Benefits:**
- ✅ 10x faster test execution (no container restarts)
- ✅ Reduced resource usage (single DB + RabbitMQ instance)
- ✅ Consistent state via fresh fixtures in `@BeforeEach`
- ✅ Test isolation via Spring context + cleanup

**See:** `/doc/ai/TESTCONTAINERS.md` for complete Testcontainers guide

### Test Isolation Strategy

**AbstractIntegrationTest:**
```java
@BeforeEach
void setUp() {
    // Fresh fixtures per test (automatic)
    companyFixture = new CompanyIntegrationFixture(...);
    shipmentOrderFixture = new ShipmentOrderIntegrationFixture(...);
}
```

**Why Fresh Fixtures:**
- Prevents state leakage between tests
- Each test sees clean initial state
- No cross-test contamination
- Simpler than transactional rollback for async operations

**Database Cleanup:**
- Can add explicit cleanup in `@BeforeEach` if needed
- NOT using `@Transactional` rollback (incompatible with async listeners)
- Parallel execution safe via Spring context isolation

---

## Key Principles

### ✅ DO

- **Centralize test utilities** - assertions/builders/utils at root level
- **Use clear naming** - Builder (not DataBuilder), DTOBuilder vs Builder vs InputBuilder
- **Use `*IT.java` for integration tests** - Ensures Failsafe execution
- **Use `*Test.java` for unit tests** - Ensures Surefire execution
- **Extend `AbstractIntegrationTest`** - Gets Testcontainers + cleanup
- **Keep fake repositories in module** - They implement module-specific interfaces
- **Keep integration fixtures in integration/** - Tightly coupled to integration tests
- **Mirror main structure for tests** - Module tests under {module}/
- **Use static imports for builders** - `CreateCompanyDTOBuilder.aCreateCompanyDTO()`
- **Keep tests stateless** - No shared mutable state between tests

### ❌ DON'T

- **Don't use generic `data` packages** - Use specific builders/ or utils/
- **Don't scatter assertions** - Centralize in assertions/
- **Don't use "DataBuilder" suffix** - Just "Builder" (with context from package)
- **Don't duplicate builders** - Centralized builders work for unit + integration
- **Don't create containers manually** - Use `TestContainersManager` singleton
- **Don't use `@Transactional` rollback** - Incompatible with async listeners
- **Don't use `*IntegrationTest.java`** - Use `*IT.java` for Failsafe
- **Don't inject fixtures** - Create fresh instances in `@BeforeEach`
- **Don't put builders in test packages** - They're utilities, not tests

---

## Custom AssertJ Assertions

Custom assertions provide fluent, domain-specific validation that's easier to read and maintain than raw AssertJ.

### Pattern Structure

```java
package br.com.logistics.tms.assertions.jpa;

import org.assertj.core.api.AbstractAssert;
import static org.assertj.core.api.Assertions.assertThat;

public class CompanyAssert extends AbstractAssert<CompanyAssert, Company> {

    private CompanyAssert(final Company actual) {
        super(actual, CompanyAssert.class);
    }

    public static CompanyAssert assertThatCompany(final Company actual) {
        return new CompanyAssert(actual);
    }

    public CompanyAssert hasCompanyId(final CompanyId expected) {
        isNotNull();
        assertThat(actual.getCompanyId())
                .as("Company companyId")
                .isEqualTo(expected);
        return this;
    }

    public CompanyAssert hasName(final String expected) {
        isNotNull();
        assertThat(actual.getName())
                .as("Company name")
                .isEqualTo(expected);
        return this;
    }

    public CompanyAssert hasTypes(final CompanyType... expected) {
        isNotNull();
        assertThat(actual.getCompanyTypes())
                .as("Company types")
                .containsExactlyInAnyOrder(expected);
        return this;
    }
}
```

### Usage

```java
import static br.com.logistics.tms.assertions.jpa.CompanyAssert.assertThatCompany;

@Test
void shouldCreateCompanyWithCorrectAttributes() {
    final Company company = Company.createCompany("Test", "12345678901234", types, config);
    
    assertThatCompany(company)
        .hasCompanyId(expectedId)
        .hasName("Test")
        .hasTypes(CompanyType.SELLER);
}
```

### Key Design Points

- ✅ Extends `AbstractAssert<{Name}Assert, {Type}>`
- ✅ Static factory method `assertThat{Entity}(entity)`
- ✅ Each assertion method returns `this` for chaining
- ✅ Use `.as("description")` for clear failure messages
- ✅ All parameters declared `final`

**Location:** `assertions/{domain|jpa|outbox}/`

**See Also:** Existing examples in `assertions/jpa/` (CompanyEntityAssert, AgreementAssert)

---

## Test Data Builders

Builders reduce boilerplate and provide sensible defaults for test object creation.

### Pattern Structure

```java
package br.com.logistics.tms.builders.domain.company;

public class CompanyBuilder {
    
    private CompanyId companyId = CompanyId.unique();
    private String name = "Default Company";
    private Cnpj cnpj = new Cnpj("12345678901234");
    private Set<CompanyType> types = Set.of(CompanyType.SELLER);
    private Map<String, Object> configuration = Map.of();
    private Set<Agreement> agreements = Set.of();

    private CompanyBuilder() {}

    public static CompanyBuilder aCompany() {
        return new CompanyBuilder();
    }

    public CompanyBuilder withCompanyId(final CompanyId companyId) {
        this.companyId = companyId;
        return this;
    }

    public CompanyBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public CompanyBuilder withTypes(final CompanyType... types) {
        this.types = Set.of(types);
        return this;
    }

    public Company build() {
        return Company.createCompany(name, cnpj.value(), types, configuration);
    }
}
```

### Usage

```java
import static br.com.logistics.tms.builders.domain.company.CompanyBuilder.aCompany;

@Test
void myTest() {
    final Company company = aCompany()
        .withName("Test Company")
        .withTypes(CompanyType.SELLER, CompanyType.MARKETPLACE)
        .build();
    
    // Test uses company...
}
```

### Key Design Points

- ✅ Static factory method `a{Entity}()` or `an{Entity}()`
- ✅ Private constructor
- ✅ Sensible defaults for all fields
- ✅ Fluent `with{Field}()` methods returning `this`
- ✅ `build()` uses domain factory methods (NOT constructors)
- ✅ All parameters declared `final`
- ✅ Varargs for collections (`withTypes(CompanyType...)`)

**Location:** `builders/{dto|input|domain}/`

**Builder Suffix:** Use `Builder` (NOT `DataBuilder`)

**See Also:** `/doc/ai/prompts/test-data-builders.md`

---

## Fake Repositories

Fake repositories provide in-memory implementations for unit testing use cases without database dependencies.

### Pattern Structure

```java
package br.com.logistics.tms.company.application.repositories;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FakeCompanyRepository implements CompanyRepository {
    
    private final Map<UUID, Company> companies = new ConcurrentHashMap<>();

    @Override
    public Company create(final Company company) {
        companies.put(company.getCompanyId().value(), company);
        return company;
    }

    @Override
    public Optional<Company> getCompanyById(final CompanyId id) {
        return Optional.ofNullable(companies.get(id.value()));
    }

    @Override
    public Optional<Company> getCompanyByCnpj(final Cnpj cnpj) {
        return companies.values().stream()
            .filter(c -> c.getCnpj().equals(cnpj))
            .findFirst();
    }

    @Override
    public Company update(final Company company) {
        companies.put(company.getCompanyId().value(), company);
        return company;
    }

    public void clear() {
        companies.clear();
    }
}
```

### Usage

```java
class CreateCompanyUseCaseTest {
    
    private FakeCompanyRepository companyRepository;
    private CreateCompanyUseCase useCase;

    @BeforeEach
    void setUp() {
        companyRepository = new FakeCompanyRepository();
        useCase = new CreateCompanyUseCase(companyRepository);
    }

    @Test
    void shouldCreateCompany() {
        final Input input = new Input("Test", "12345678901234", types, config);
        
        final Output output = useCase.execute(input);
        
        assertThat(output.companyId()).isNotNull();
        assertThat(companyRepository.getCompanyById(new CompanyId(output.companyId())))
            .isPresent();
    }
}
```

### Key Design Points

- ✅ Implements repository interface from application layer
- ✅ Uses `ConcurrentHashMap` for thread-safety
- ✅ All CRUD operations in-memory
- ✅ `clear()` method for test cleanup
- ✅ Query methods match production repository
- ✅ All parameters declared `final`

**Location:** `{module}/application/repositories/Fake{Entity}Repository.java`

**See Also:** `/doc/ai/prompts/fake-repositories.md`

---

## Integration Fixtures

Fixtures encapsulate repetitive REST operations and waiting logic in integration tests.

### Pattern Structure

```java
package br.com.logistics.tms.integration.fixtures;

import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CompanyIntegrationFixture {
    
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final CompanyOutboxJpaRepository outboxRepository;
    private final ShipmentOrderCompanyJpaRepository syncRepository;

    public CompanyIntegrationFixture(final MockMvc mockMvc,
                                    final ObjectMapper objectMapper,
                                    final CompanyOutboxJpaRepository outboxRepository,
                                    final ShipmentOrderCompanyJpaRepository syncRepository) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.outboxRepository = outboxRepository;
        this.syncRepository = syncRepository;
    }

    public CompanyId createCompany(final CreateCompanyDTO dto) throws Exception {
        // 1. POST to REST endpoint
        final String responseJson = mockMvc.perform(post("/companies")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        final CreateCompanyResponseDTO response = objectMapper.readValue(
            responseJson, CreateCompanyResponseDTO.class
        );
        final CompanyId companyId = new CompanyId(response.companyId());

        // 2. Wait for outbox published
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> outboxRepository
                .findFirstByAggregateIdOrderByCreatedAtDesc(companyId.value())
                .map(outbox -> outbox.getStatus() == OutboxStatus.PUBLISHED)
                .orElse(false)
            );

        // 3. Wait for sync to other schema
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> syncRepository.existsById(companyId.value()));

        return companyId;
    }
}
```

### Usage

```java
class MyIntegrationTest extends AbstractIntegrationTest {
    
    @Test
    void myTest() throws Exception {
        // companyFixture already available from AbstractIntegrationTest
        
        final CompanyId id = companyFixture.createCompany(
            CreateCompanyDTOBuilder.aCreateCompanyDTO()
                .withName("Test")
                .build()
        );
        
        // Fixture handled: REST call + wait for outbox + wait for sync
        // Test can immediately assert the results
        assertThatCompany(companyJpaRepository.findById(id.value()).orElseThrow())
            .hasName("Test");
    }
}
```

### Key Design Points

- ✅ Encapsulates REST call + response parsing + waiting logic
- ✅ Returns typed domain IDs (NOT raw UUIDs)
- ✅ Uses Awaitility for async validation
- ✅ Default 30-second timeout for async operations
- ✅ NOT a `@Component` - manually instantiated per test
- ✅ All parameters declared `final`
- ✅ Throws `Exception` (test methods already declare it)

**Location:** `integration/fixtures/{Entity}IntegrationFixture.java`

**Key Principle:** One fixture method = one complete business operation (REST + events + sync)

---

## References

- **Integration Tests Guide:** [/doc/ai/INTEGRATION_TESTS.md](/doc/ai/INTEGRATION_TESTS.md)
- **Testcontainers Guide:** [/doc/ai/TESTCONTAINERS.md](/doc/ai/TESTCONTAINERS.md)
- **Fake Repositories Guide:** [/doc/ai/prompts/fake-repositories.md](/doc/ai/prompts/fake-repositories.md)
- **Test Data Builders Guide:** [/doc/ai/prompts/test-data-builders.md](/doc/ai/prompts/test-data-builders.md)
- **Code Style:** [/doc/ai/CODE_STYLE.md](/doc/ai/CODE_STYLE.md)

---

## Running Tests

### All Tests
```bash
mvn verify  # Runs both unit and integration tests
```

### Unit Tests Only
```bash
mvn test  # Surefire - 8 parallel threads
```

### Integration Tests Only
```bash
mvn failsafe:integration-test  # Failsafe - 4 parallel threads
```

### Single Test
```bash
mvn test -Dtest=CompanyTest           # Unit test
mvn verify -Dit.test=CompanyShipmentOrderIT  # Integration test
```

### Performance
- **Before parallel execution:** ~2 minutes
- **After parallel execution:** ~15 seconds
- **Speedup:** ~8x faster

---

**Last Updated:** 2025-11-19 (updated with Testcontainers singleton pattern and fixture management)
