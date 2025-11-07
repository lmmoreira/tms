# Code Style and Documentation Guidelines

**Guidelines for writing clean, self-documenting code in the TMS project.**

---

## Code Comments Philosophy

### The Golden Rule

**Code should be self-documenting through clear naming and structure. Comments should be rare and only used when absolutely necessary.**

### ❌ DON'T Write Obvious Comments

#### Class-Level Comments

```java
// ❌ BAD - Stating the obvious
/**
 * Test data builder for creating Company instances using a fluent builder pattern.
 * Provides sensible defaults for testing.
 */
public class CompanyTestDataBuilder {
    // ...
}

// ✅ GOOD - No comment, class name is clear
public class CompanyTestDataBuilder {
    // ...
}
```

#### Method-Level Comments

```java
// ❌ BAD - Method name already tells you what it does
/**
 * Sets the company ID.
 */
public CompanyTestDataBuilder withCompanyId(final UUID companyId) {
    this.companyId = companyId;
    return this;
}

// ✅ GOOD - No comment needed
public CompanyTestDataBuilder withCompanyId(final UUID companyId) {
    this.companyId = companyId;
    return this;
}
```

```java
// ❌ BAD - Obviously returning the count
/**
 * Returns the total count of companies in storage.
 * Useful for test assertions.
 */
public int count() {
    return storage.size();
}

// ✅ GOOD - Method name is self-explanatory
public int count() {
    return storage.size();
}
```

#### Test Comments (Given/When/Then)

```java
// ❌ BAD - Test structure is obvious from the code
@Test
void shouldCreateNewCompany() {
    // Given
    final UUID companyId = UUID.randomUUID();
    final Input input = anInput().withCompanyId(companyId).build();
    
    // When
    useCase.execute(input);
    
    // Then
    assertEquals(1, repository.count());
}

// ✅ GOOD - No comments, structure is clear
@Test
@DisplayName("Should create a new company when company does not exist")
void shouldCreateNewCompany() {
    final UUID companyId = UUID.randomUUID();
    final Input input = anInput().withCompanyId(companyId).build();
    
    useCase.execute(input);
    
    assertEquals(1, repository.count());
}
```

---

## When Comments ARE Acceptable

### ✅ Complex Business Logic

```java
// ✅ GOOD - Explains WHY, not WHAT
public Company updateData(final Map<String, Object> newData) {
    // Merge strategy: new data overrides existing, but preserve other fields
    final Map<String, Object> merged = new HashMap<>(this.data.value());
    merged.putAll(newData);
    return new Company(this.companyId, CompanyData.with(merged), ...);
}
```

### ✅ Non-Obvious Workarounds

```java
// ✅ GOOD - Explains a workaround or gotcha
public void processEvent(final Event event) {
    // RabbitMQ may deliver the same message twice due to network issues
    // Check if we've already processed this event
    if (eventRegistry.wasProcessed(event.getId())) {
        return;
    }
    // ...
}
```

### ✅ TODO or FIXME

```java
// ✅ GOOD - Indicates future work
public List<Company> findAll() {
    // TODO: Add pagination support when we have > 1000 companies
    return storage.values().stream().toList();
}
```

### ✅ Complex Algorithms

```java
// ✅ GOOD - Explains algorithm choice
public Cnpj validate(final String value) {
    // Uses Luhn algorithm for CNPJ validation
    // See: https://en.wikipedia.org/wiki/Luhn_algorithm
    // ...
}
```

---

## Self-Documenting Code Principles

### 1. Use Descriptive Names

```java
// ❌ BAD
public class CRUD {
    public Object d(int i) { ... }
}

// ✅ GOOD
public class CompanyRepository {
    public Optional<Company> findById(final CompanyId id) { ... }
}
```

### 2. Use @DisplayName for Tests

```java
// ❌ BAD - Unclear test name
@Test
void test1() { ... }

// ✅ GOOD - Descriptive test name with display name
@Test
@DisplayName("Should create a new company when company does not exist")
void shouldCreateNewCompanyWhenCompanyDoesNotExist() { ... }
```

### 3. Small, Focused Methods

```java
// ❌ BAD - Needs comments to explain sections
public void processOrder(Order order) {
    // Validate order
    if (order == null) throw new ValidationException();
    
    // Check inventory
    if (!inventory.hasStock(order.items())) return;
    
    // Process payment
    payment.charge(order.total());
    
    // Ship order
    shipping.createLabel(order);
}

// ✅ GOOD - Each method is self-explanatory
public void processOrder(final Order order) {
    validateOrder(order);
    checkInventory(order);
    processPayment(order);
    shipOrder(order);
}
```

### 4. Use Constants for Magic Values

```java
// ❌ BAD - Magic string needs comment
public void sync(Map<String, Object> data) {
    // "types" is the key for company types in the data map
    if (!data.containsKey("types")) return;
}

// ✅ GOOD - Constant is self-documenting
public static final String TYPES_KEY = "types";

public void sync(final Map<String, Object> data) {
    if (!data.containsKey(TYPES_KEY)) return;
}
```

---

## Specific Guidelines by File Type

### Domain Layer

**NO comments** unless explaining complex business rules or algorithms.

```java
// ✅ GOOD - No comments needed
public class Company extends AbstractAggregateRoot {

    private final CompanyId companyId;
    private final String name;
    private final Cnpj cnpj;

    public static Company createCompany(final String name, final String cnpj, ...) {
        final Company company = new Company(CompanyId.unique(), name, new Cnpj(cnpj), ...);
        company.placeDomainEvent(new CompanyCreated(...));
        return company;
    }

    public Company updateName(final String name) {
        if (this.name.equals(name)) return this;
        final Company updated = new Company(this.companyId, name, this.cnpj, ...);
        updated.placeDomainEvent(new CompanyUpdated(...));
        return updated;
    }
}
```

### Application Layer (Use Cases)

**NO comments** for standard CRUD operations. Only for complex business orchestration.

```java
// ✅ GOOD - No comments, code is clear
@DomainService
@Cqrs(DatabaseRole.WRITE)
public class SynchronizeCompanyUseCase implements VoidUseCase<SynchronizeCompanyUseCase.Input> {

    public static final String TYPES_KEY = "types";

    private final CompanyRepository companyRepository;

    public SynchronizeCompanyUseCase(final CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public void execute(final Input input) {
        if (input.data() == null || !input.data().containsKey(TYPES_KEY)) {
            return;
        }

        final Map<String, Object> data = Map.of(TYPES_KEY, input.data().get(TYPES_KEY));
        companyRepository.save(companyRepository.findById(CompanyId.with(input.companyId()))
                .map(existing -> existing.updateData(data))
                .orElseGet(() -> Company.createCompany(input.companyId(), data)));
    }

    public record Input(UUID companyId, Map<String, Object> data) {}
}
```

### Test Data Builders

**NO comments** at all. Builders should be self-explanatory.

```java
// ✅ GOOD - No comments anywhere
public class CompanyTestDataBuilder {

    private UUID companyId = UUID.randomUUID();
    private Map<String, Object> data = new HashMap<>();

    public static CompanyTestDataBuilder aCompany() {
        return new CompanyTestDataBuilder();
    }

    public CompanyTestDataBuilder withCompanyId(final UUID companyId) {
        this.companyId = companyId;
        return this;
    }

    public CompanyTestDataBuilder withTypes(final String... types) {
        return withTypes(Arrays.asList(types));
    }

    public Company build() {
        if (data.isEmpty()) {
            data.put(SynchronizeCompanyUseCase.TYPES_KEY, List.of("DEFAULT"));
        }
        return Company.createCompany(companyId, data);
    }
}
```

### Fake Repositories

**NO comments** at all. Implementation is straightforward.

```java
// ✅ GOOD - No comments, methods are obvious
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

### Test Files

**NO Given/When/Then comments.** Use `@DisplayName` instead.

```java
// ✅ GOOD - No comments, uses @DisplayName
@Test
@DisplayName("Should create a new company when company does not exist")
void shouldCreateNewCompanyWhenCompanyDoesNotExist() {
    final UUID companyId = UUID.randomUUID();
    final Input input = anInput().withCompanyId(companyId).withTypes("TYPE").build();

    useCase.execute(input);

    assertTrue(repository.existsById(CompanyId.with(companyId)));
}
```

---

## Code Review Checklist

When reviewing code, check for:

- ❌ Class-level JavaDoc that just restates the class name
- ❌ Method-level JavaDoc that just restates the method name
- ❌ "Getter/Setter" comments
- ❌ Given/When/Then comments in tests
- ❌ Comments explaining what code does (the code should explain itself)
- ✅ Only comments that explain WHY, not WHAT
- ✅ Clear, descriptive names for classes, methods, and variables
- ✅ @DisplayName annotations on test methods

---

## Summary

### The Philosophy

**If you need a comment to explain WHAT the code does, the code needs to be refactored to be more clear.**

Comments should only explain:
- **WHY** (business reasoning, design decisions)
- **Workarounds** (temporary solutions, known issues)
- **Future work** (TODO, FIXME)
- **Complex algorithms** (with references)

Everything else should be obvious from the code itself.

---

## Examples in This Project

See these files for examples of clean, comment-free code:

- `src/test/java/br/com/logistics/tms/shipmentorder/data/CompanyTestDataBuilder.java`
- `src/test/java/br/com/logistics/tms/shipmentorder/data/FakeCompanyRepository.java`
- `src/test/java/br/com/logistics/tms/shipmentorder/application/usecases/data/SynchronizeCompanyUseCaseInputDataBuilder.java`
- `src/test/java/br/com/logistics/tms/shipmentorder/application/usecases/SynchronizeCompanyUseCaseTest.java`
- `src/main/java/br/com/logistics/tms/shipmentorder/application/usecases/SynchronizeCompanyUseCase.java`
