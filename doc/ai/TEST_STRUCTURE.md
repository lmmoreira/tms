# Test Structure and Organization

**Guidelines for organizing test files in the TMS project.**

---

## Overview

The test directory structure mirrors the main source code structure but with specific locations for test utilities like fake repositories and test data builders.

---

## Directory Structure

```
src/test/java/br/com/logistics/tms/
├── {module}/
│   ├── application/
│   │   ├── repositories/              # Fake repositories
│   │   │   └── Fake{Entity}Repository.java
│   │   └── usecases/
│   │       ├── {UseCase}Test.java
│   │       └── data/                  # Use case input builders
│   │           └── {UseCase}InputDataBuilder.java
│   └── domain/
│       ├── {Aggregate}Test.java       # Domain unit tests
│       └── {Entity}TestDataBuilder.java  # Aggregate builders
└── architecture/
    └── {ArchUnit tests}
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

**Why:** Integration tests validate complete business flows across modules. They are placed in a separate `integration` package to distinguish them from unit tests.

**Example:**
```
src/test/java/br/com/logistics/tms/integration/
├── CompanyShipmentOrderIntegrationTest.java
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

| Type | Naming Pattern | Example |
|------|----------------|---------|
| **Fake Repository** | `Fake{Entity}Repository` | `FakeCompanyRepository` |
| **Domain Builder** | `{Entity}TestDataBuilder` | `CompanyTestDataBuilder` |
| **Input Builder** | `{UseCase}InputDataBuilder` | `SynchronizeCompanyUseCaseInputDataBuilder` |
| **DTO Builder** | `{Operation}{Entity}DTODataBuilder` | `CreateCompanyDTODataBuilder` |
| **Integration Fixture** | `{Entity}IntegrationFixture` | `CompanyIntegrationFixture` |
| **Custom Assertion** | `{Entity}Assert` | `CompanyEntityAssert` |
| **Domain Test** | `{Entity}Test` | `CompanyTest` |
| **Use Case Test** | `{UseCase}Test` | `SynchronizeCompanyUseCaseTest` |
| **Integration Test** | `{Flow}IntegrationTest` | `CompanyShipmentOrderIntegrationTest` |

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

## Key Principles

### ✅ DO

- **Place fakes in `application/repositories`** - Next to the interface they implement
- **Place domain builders in `domain`** - Next to the aggregates they build
- **Place input builders in `usecases/data`** - Next to the tests that use them
- **Mirror main structure** - Keep test packages aligned with main source
- **Use clear naming** - Follow the naming conventions

### ❌ DON'T

- **Don't use generic `data` packages** - Be specific about utility purpose
- **Don't mix test utilities** - Each type has its own location
- **Don't diverge from main structure** - Keep test and main structures parallel
- **Don't add "Test" suffix to builders** - They're utilities, not test classes

---

## References

- **Fake Repositories Guide:** [/doc/ai/prompts/fake-repositories.md](/doc/ai/prompts/fake-repositories.md)
- **Test Data Builders Guide:** [/doc/ai/prompts/test-data-builders.md](/doc/ai/prompts/test-data-builders.md)
- **Testing Patterns:** [/doc/ai/examples/testing-patterns.md](/doc/ai/examples/testing-patterns.md)
- **Code Style:** [/doc/ai/CODE_STYLE.md](/doc/ai/CODE_STYLE.md)

---

**Last Updated:** 2025-11-07 (after package structure reorganization)
