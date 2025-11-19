# Test Structure and Organization

**Guidelines for organizing test files in the TMS project.**

---

## Overview

The test directory structure mirrors the main source code structure but with specific locations for test utilities like fake repositories and test data builders.

### Parallel Test Execution

**Unit and integration tests run in parallel for optimal performance:**

- **Unit Tests (*Test.java):** Run via Surefire with 8 parallel threads
- **Integration Tests (*IT.java):** Run via Failsafe with 4 parallel threads  
- **Shared Testcontainers:** Single PostgreSQL + RabbitMQ instance via `TestcontainersManager` singleton
- **Test Isolation:** Each integration test gets fresh Spring context + transactional cleanup

---

## Directory Structure

```
src/test/java/br/com/logistics/tms/
├── AbstractIntegrationTest.java       # Base class for integration tests
├── TestcontainersManager.java         # Singleton managing shared containers
├── {module}/
│   ├── application/
│   │   ├── repositories/              # Fake repositories
│   │   │   └── Fake{Entity}Repository.java
│   │   └── usecases/
│   │       ├── {UseCase}Test.java     # Unit tests (*Test.java)
│   │       └── data/                  # Use case input builders
│   │           └── {UseCase}InputDataBuilder.java
│   └── domain/
│       ├── {Aggregate}Test.java       # Domain unit tests
│       └── {Entity}TestDataBuilder.java  # Aggregate builders
├── integration/
│   ├── {Flow}IT.java                  # Integration tests (*IT.java)
│   ├── fixtures/                      # Test helpers
│   ├── assertions/                    # Custom AssertJ assertions
│   └── data/                          # DTO builders
└── architecture/
    └── {ArchUnit tests}               # Architecture rules (*Test.java)
```

---

## Package Locations

### 1. Fake Repositories

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
| **Domain Builder** | `{Entity}TestDataBuilder` | - | `CompanyTestDataBuilder` |
| **Input Builder** | `{UseCase}InputDataBuilder` | - | `SynchronizeCompanyUseCaseInputDataBuilder` |
| **DTO Builder** | `{Operation}{Entity}DTODataBuilder` | - | `CreateCompanyDTODataBuilder` |
| **Integration Fixture** | `{Entity}IntegrationFixture` | - | `CompanyIntegrationFixture` |
| **Custom Assertion** | `{Entity}Assert` | - | `CompanyEntityAssert` |

**Critical:** Use `*IT.java` suffix for integration tests to ensure they run via Failsafe, not Surefire.

---

## Complete Example

### ShipmentOrder Module Test Structure

```
src/test/java/br/com/logistics/tms/
├── shipmentorder/
│   ├── application/
│   │   ├── repositories/
│   │   │   └── FakeCompanyRepository.java           # Fake repository
│   │   └── usecases/
│   │       ├── SynchronizeCompanyUseCaseTest.java   # Use case test
│   │       └── data/
│   │           └── SynchronizeCompanyUseCaseInputDataBuilder.java  # Input builder
│   └── domain/
│       └── CompanyTestDataBuilder.java              # Domain aggregate builder
└── integration/
    ├── CompanyShipmentOrderIntegrationTest.java     # Integration test
    ├── fixtures/
    │   ├── CompanyIntegrationFixture.java
    │   └── ShipmentOrderIntegrationFixture.java
    ├── assertions/
    │   └── CompanyEntityAssert.java
    └── data/
        └── CreateCompanyDTODataBuilder.java
```

### Imports in Tests

```java
// In SynchronizeCompanyUseCaseTest.java
import br.com.logistics.tms.shipmentorder.application.repositories.FakeCompanyRepository;
import br.com.logistics.tms.shipmentorder.application.usecases.data.SynchronizeCompanyUseCaseInputDataBuilder;
import br.com.logistics.tms.shipmentorder.domain.CompanyTestDataBuilder;
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

- **Use `*IT.java` for integration tests** - Ensures Failsafe execution
- **Use `*Test.java` for unit tests** - Ensures Surefire execution
- **Extend `AbstractIntegrationTest`** - Gets Testcontainers + cleanup
- **Place fakes in `application/repositories`** - Next to the interface they implement
- **Place domain builders in `domain`** - Next to the aggregates they build
- **Place input builders in `usecases/data`** - Next to the tests that use them
- **Mirror main structure** - Keep test packages aligned with main source
- **Use clear naming** - Follow the naming conventions
- **Keep tests stateless** - No shared mutable state between tests

### ❌ DON'T

- **Don't use generic `data` packages** - Be specific about utility purpose
- **Don't mix test utilities** - Each type has its own location
- **Don't diverge from main structure** - Keep test and main structures parallel
- **Don't add "Test" suffix to builders** - They're utilities, not test classes
- **Don't create containers manually** - Use `TestContainersManager` singleton
- **Don't use `@Transactional` rollback** - Incompatible with async listeners
- **Don't use `*IntegrationTest.java`** - Use `*IT.java` for Failsafe
- **Don't inject fixtures** - Create fresh instances in `@BeforeEach`

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
