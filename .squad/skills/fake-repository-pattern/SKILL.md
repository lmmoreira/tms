# Fake Repository Pattern

## ⚡ TL;DR

- **When:** Unit testing use cases without database
- **Why:** Faster tests, reusable, no mock boilerplate (when().thenReturn())
- **Pattern:** In-memory Map-based repository implementation
- **Confidence:** `low` (only 1 implementation exists, under-adopted)

---

## Pattern

### Fake Repository Template

```java
package br.com.logistics.tms.{module}.application.repositories;

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

### Real Implementation Example

```java
package br.com.logistics.tms.shipmentorder.application.repositories;

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

### Usage in Tests

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

    @Test
    void shouldUpdateExistingCompany() {
        final UUID companyId = UUID.randomUUID();
        final Company existing = CompanyBuilder.aCompany()
                .withCompanyId(companyId)
                .build();
        companyRepository.save(existing);

        final SynchronizeCompanyUseCase.Input input = SynchronizeCompanyUseCaseInputDataBuilder.anInput()
                .withCompanyId(companyId)
                .withTypes("CARRIER")
                .build();

        useCase.execute(input);

        final Optional<Company> updated = companyRepository.findById(CompanyId.with(companyId));
        assertTrue(updated.isPresent());
    }
}
```

---

## When to Use vs NOT Use

### ✅ Use Fakes

- Unit testing use cases (application layer)
- Need stateful behavior (save then find)
- Testing sequences of operations
- Want reusable test doubles
- Eliminate mock boilerplate (`when()`, `verify()`, `thenReturn()`)

### ❌ Use Real Repository

- Integration tests (use Testcontainers)
- Testing infrastructure layer
- Testing JPA mappings or SQL queries

### ❌ Use Mocks

- Rare: when you need to verify specific method call order
- Testing error scenarios hard to reproduce with fakes
- Third-party services you can't fake easily

---

## Key Principles

### ✅ DO

- **Use domain value objects as keys:** `Map<CompanyId, Company>` not `Map<UUID, Company>`
- **Implement all interface methods:** Complete implementation
- **Provide helper methods:** `clear()`, `count()` for test assertions
- **Keep it simple:** Just store and retrieve, no complex logic
- **Use final fields:** `private final Map<...> storage`

### ❌ DON'T

- **Don't add business logic:** Fakes are for storage, not validation
- **Don't add JavaDoc:** Method names are self-explanatory
- **Don't use primitives as keys:** Use value objects for type safety

---

## Concurrency Handling

**Current implementation is single-threaded only.** HashMap is not thread-safe. Use only in single-threaded unit tests. For concurrent test scenarios, upgrade to `ConcurrentHashMap`.

---

## Location

**Fake repositories live in:**  
`src/test/java/br/com/logistics/tms/{module}/application/repositories/Fake{Entity}Repository.java`

**Naming Convention:**  
`Fake{Entity}Repository` — no "Test" suffix (it's a test utility, not a test class)

---

## Related Patterns

- **test-data-builder-pattern:** Pair with builders for creating test inputs
- **Application layer tests:** Fakes are designed for use case testing
- **Domain tests:** Don't need repositories (pure domain logic)

---

## Metadata

**Confidence:** `low`  
**Applies To:** `[company, shipmentorder, all modules]`  
**Replaces:** `[prompts/fake-repositories.md (454 lines)]`  
**Token Cost:** `~120 lines (74% reduction)`  
**Related Skills:** `[test-data-builder-pattern]`  
**Created:** `2025-02-24`  
**Last Updated:** `2025-02-24`
