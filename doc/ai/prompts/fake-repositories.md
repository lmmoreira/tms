# Fake Repository Pattern for Testing

**Pattern for creating in-memory fake repositories to test use cases without mocks.**

---

## Overview

Fake repositories implement the repository interface using in-memory data structures (typically `Map`) instead of real database connections. This approach provides faster, more maintainable tests without the complexity of mocking frameworks.

## Philosophy

**Prefer Fakes Over Mocks**

- ✅ **Fakes:** Real implementation using simple data structures
- ❌ **Mocks:** Framework-generated stubs with verification

### Why Fakes?

1. **Real Behavior** - Fakes behave like real repositories
2. **No Setup Boilerplate** - No `when()`, `verify()`, `thenReturn()` in every test
3. **Reusable** - One fake serves all tests
4. **Easier to Maintain** - Changes to interface require updates in one place
5. **Test Real Interactions** - Tests actually call real methods
6. **No Mock Framework** - Simpler dependencies and faster tests

---

## When to Use

- ✅ Unit testing use cases (application layer)
- ✅ Testing repository interactions
- ✅ When you need stateful repository behavior
- ✅ When testing sequences of operations

## When NOT to Use

- ❌ Integration tests (use real repository with Testcontainers)
- ❌ Testing infrastructure layer (test against real database)
- ❌ When you need to verify specific method calls (use mocks for this rare case)

---

## Pattern Structure

### Basic Fake Repository Template

```java
package br.com.logistics.tms.{module}.application.repositories;

import br.com.logistics.tms.{module}.application.repositories.{Entity}Repository;
import br.com.logistics.tms.{module}.domain.{Entity};
import br.com.logistics.tms.{module}.domain.{Entity}Id;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Fake{Entity}Repository implements {Entity}Repository {

    private final Map<{Entity}Id, {Entity}> storage = new HashMap<>();

    @Override
    public {Entity} save(final {Entity} entity) {
        storage.put(entity.get{Entity}Id(), entity);
        return entity;
    }

    @Override
    public Optional<{Entity}> findById(final {Entity}Id id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public boolean existsById(final {Entity}Id id) {
        return storage.containsKey(id);
    }

    public void clear() {
        storage.clear();
    }

    public int count() {
        return storage.size();
    }
}
```

---

## Real Example

**File:** `src/test/java/br/com/logistics/tms/shipmentorder/application/repositories/FakeCompanyRepository.java`

```java
package br.com.logistics.tms.shipmentorder.application.repositories;

import br.com.logistics.tms.shipmentorder.application.repositories.CompanyRepository;
import br.com.logistics.tms.shipmentorder.domain.Company;
import br.com.logistics.tms.shipmentorder.domain.CompanyId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FakeCompanyRepository implements CompanyRepository {

    private final Map<CompanyId, Company> storage = new HashMap<>();

    @Override
    public Company save(final Company company) {
        storage.put(company.getCompanyId(), company);
        return company;
    }

    @Override
    public Optional<Company> findById(final CompanyId companyId) {
        return Optional.ofNullable(storage.get(companyId));
    }

    @Override
    public boolean existsById(final CompanyId companyId) {
        return storage.containsKey(companyId);
    }

    public void clear() {
        storage.clear();
    }

    public int count() {
        return storage.size();
    }
}
```

---

## Usage in Tests

### Test Setup

```java
class SynchronizeCompanyUseCaseTest {

    private FakeCompanyRepository companyRepository;
    private SynchronizeCompanyUseCase useCase;

    @BeforeEach
    void setUp() {
        companyRepository = new FakeCompanyRepository();
        useCase = new SynchronizeCompanyUseCase(companyRepository);
    }

    @Test
    void shouldCreateNewCompany() {
        final SynchronizeCompanyUseCase.Input input = SynchronizeCompanyUseCaseInputDataBuilder.anInput()
                .withTypes("LOGISTICS_PROVIDER")
                .build();

        useCase.execute(input);

        assertEquals(1, companyRepository.count());
    }

    @Test
    void shouldUpdateExistingCompany() {
        final UUID companyId = UUID.randomUUID();
        final Company existing = CompanyTestDataBuilder.aCompany()
                .withCompanyId(companyId)
                .withTypes("CARRIER")
                .build();
        companyRepository.save(existing);

        final SynchronizeCompanyUseCase.Input input = SynchronizeCompanyUseCaseInputDataBuilder.anInput()
                .withCompanyId(companyId)
                .withTypes("LOGISTICS_PROVIDER")
                .build();

        useCase.execute(input);

        final Optional<Company> updated = companyRepository.findById(CompanyId.with(companyId));
        assertTrue(updated.isPresent());
        assertTrue(updated.get().types().contains("LOGISTICS_PROVIDER"));
    }
}
```

### Comparing to Mock-Based Tests

#### ❌ With Mocks (Not Recommended)

```java
@ExtendWith(MockitoExtension.class)
class SynchronizeCompanyUseCaseTest {

    @Mock
    private CompanyRepository companyRepository;

    private SynchronizeCompanyUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SynchronizeCompanyUseCase(companyRepository);
    }

    @Test
    void shouldCreateNewCompany() {
        final UUID companyId = UUID.randomUUID();
        
        when(companyRepository.findById(any())).thenReturn(Optional.empty());
        when(companyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        final SynchronizeCompanyUseCase.Input input = new SynchronizeCompanyUseCase.Input(
            companyId, 
            Map.of("types", List.of("LOGISTICS_PROVIDER"))
        );

        useCase.execute(input);

        verify(companyRepository).findById(CompanyId.with(companyId));
        verify(companyRepository).save(any(Company.class));
    }
}
```

#### ✅ With Fakes (Recommended)

```java
class SynchronizeCompanyUseCaseTest {

    private FakeCompanyRepository companyRepository;
    private SynchronizeCompanyUseCase useCase;

    @BeforeEach
    void setUp() {
        companyRepository = new FakeCompanyRepository();
        useCase = new SynchronizeCompanyUseCase(companyRepository);
    }

    @Test
    void shouldCreateNewCompany() {
        final UUID companyId = UUID.randomUUID();
        final SynchronizeCompanyUseCase.Input input = SynchronizeCompanyUseCaseInputDataBuilder.anInput()
                .withCompanyId(companyId)
                .withTypes("LOGISTICS_PROVIDER")
                .build();

        useCase.execute(input);

        assertTrue(companyRepository.existsById(CompanyId.with(companyId)));
        assertEquals(1, companyRepository.count());
    }
}
```

**Benefits of Fake:**
- No setup boilerplate (`when`, `thenReturn`)
- No verification boilerplate (`verify`)
- Tests actual repository behavior
- More readable and maintainable

---

## Key Principles

### ✅ DO

- **Use domain value objects as keys** - `Map<CompanyId, Company>` not `Map<UUID, Company>`
- **Implement all interface methods** - Complete implementation
- **Provide helper methods** - `clear()`, `count()` for test assertions
- **Keep it simple** - Just store and retrieve, no complex logic
- **No JavaDoc comments** - Class and methods are self-explanatory
- **Use final fields** - `private final Map<...> storage`

### ❌ DON'T

- **Don't add business logic** - Fakes are for storage, not validation
- **Don't add unnecessary complexity** - Simple in-memory map is enough
- **Don't add JavaDoc** - Method names are clear enough
- **Don't use primitives as keys** - Use value objects for type safety

---

## Common Methods

### Core Repository Methods

```java
public {Entity} save(final {Entity} entity)           // Save or update
public Optional<{Entity}> findById(final {Entity}Id)  // Find by ID
public boolean existsById(final {Entity}Id id)        // Check existence
```

### Test Helper Methods

```java
public void clear()                                   // Clear all data (cleanup)
public int count()                                    // Count entities (assertions)
public List<{Entity}> findAll()                       // Get all (if needed)
```

---

## Storage Considerations

### Use Value Objects as Keys

```java
// ✅ CORRECT - Value object as key
private final Map<CompanyId, Company> storage = new HashMap<>();

public Company save(final Company company) {
    storage.put(company.getCompanyId(), company);
    return company;
}

public Optional<Company> findById(final CompanyId companyId) {
    return Optional.ofNullable(storage.get(companyId));
}
```

```java
// ❌ WRONG - Primitive as key
private final Map<UUID, Company> storage = new HashMap<>();

public Company save(final Company company) {
    storage.put(company.getCompanyId().value(), company);  // Extracting value
    return company;
}

public Optional<Company> findById(final CompanyId companyId) {
    return Optional.ofNullable(storage.get(companyId.value()));  // Extracting value
}
```

**Why value objects?**
- Better type safety
- Simulates real database behavior
- Cleaner code in the fake implementation
- Leverages value object's `equals()` and `hashCode()`

---

## File Location

**Package:** `src/test/java/{module}/application/repositories/`

**Rationale:** Fake repositories implement repository interfaces from the application layer, so they are placed alongside the interface they implement in the test structure.

**Example:** `src/test/java/br/com/logistics/tms/shipmentorder/application/repositories/FakeCompanyRepository.java`

---

## Naming Conventions

- **Class:** `Fake{Entity}Repository`
- **Package:** `{module}.application.repositories` (in test source root)
- **No "Test" suffix** - It's a test utility, not a test class

---

## Complete Test Example

```java
package br.com.logistics.tms.shipmentorder.application.usecases;

import br.com.logistics.tms.shipmentorder.application.usecases.data.SynchronizeCompanyUseCaseInputDataBuilder;
import br.com.logistics.tms.shipmentorder.domain.CompanyTestDataBuilder;
import br.com.logistics.tms.shipmentorder.application.repositories.FakeCompanyRepository;
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
        final SynchronizeCompanyUseCase.Input input = SynchronizeCompanyUseCaseInputDataBuilder.anInput()
                .withCompanyId(companyId)
                .withTypes("LOGISTICS_PROVIDER", "CARRIER")
                .build();

        useCase.execute(input);

        final Optional<Company> savedCompany = companyRepository.findById(CompanyId.with(companyId));
        assertTrue(savedCompany.isPresent(), "Company should be saved");
        assertEquals(companyId, savedCompany.get().getCompanyId().value());
    }

    @Test
    @DisplayName("Should not save anything when data is null")
    void shouldNotSaveWhenDataIsNull() {
        final UUID companyId = UUID.randomUUID();
        final SynchronizeCompanyUseCase.Input input = SynchronizeCompanyUseCaseInputDataBuilder.anInput()
                .withCompanyId(companyId)
                .withNullData()
                .build();

        useCase.execute(input);

        assertEquals(0, companyRepository.count(), "No company should be saved");
        assertFalse(companyRepository.existsById(CompanyId.with(companyId)));
    }
}
```

---

## Benefits

✅ **Simpler Tests** - No mock setup boilerplate
✅ **Real Behavior** - Tests actual repository interactions
✅ **Reusable** - One fake for all tests
✅ **Fast** - In-memory, no database overhead
✅ **Maintainable** - One place to update when interface changes
✅ **Readable** - Tests focus on behavior, not mocking details
✅ **No Framework** - No Mockito or other mocking library needed

---

## When to Use Mocks Instead

Use mocks **only** when:

- You need to verify a specific method was called with specific arguments
- You need to test error scenarios that are hard to reproduce with fakes
- The dependency is a third-party service you can't fake easily

For repository testing in use cases, **always prefer fakes over mocks**.

---

## See Also

- **Test Data Builders:** `/doc/ai/prompts/test-data-builders.md`
- **Complete Example:** `src/test/java/br/com/logistics/tms/shipmentorder/application/usecases/SynchronizeCompanyUseCaseTest.java`
- **Fake Repository Example:** `src/test/java/br/com/logistics/tms/shipmentorder/application/repositories/FakeCompanyRepository.java`
