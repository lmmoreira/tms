# Test Data Builders

**Pattern for creating test data builders to reduce repetition and improve test maintainability.**

---

## Overview

Test data builders use the fluent builder pattern to create test objects with sensible defaults, making tests more concise and focused on what's being tested rather than object construction.

## When to Use

- ✅ When creating domain aggregates in tests
- ✅ When creating use case Input records in tests
- ✅ When you find repetitive object construction across multiple tests
- ✅ When tests need variations of similar test data

## Where to Place Builders

### For Domain Aggregates
**Location:** `src/test/java/{module}/domain/`

**Rationale:** Test data builders for domain aggregates are placed in the domain package within the test structure, as they create domain objects.

Example: `src/test/java/br/com/logistics/tms/shipmentorder/domain/CompanyTestDataBuilder.java`

### For Use Case Inputs
**Location:** `src/test/java/{module}/application/usecases/data/`

**Rationale:** Input builders are placed alongside the use case tests they support, making them easy to discover and maintain.

Example: `src/test/java/br/com/logistics/tms/shipmentorder/application/usecases/data/SynchronizeCompanyUseCaseInputDataBuilder.java`

---

## Pattern Structure

### Basic Builder Template

```java
package br.com.logistics.tms.{module}.domain;

import java.util.*;

public class {Entity}TestDataBuilder {

    private UUID id = UUID.randomUUID();
    private String field1 = "default value";
    private Map<String, Object> data = new HashMap<>();

    public static {Entity}TestDataBuilder a{Entity}() {
        return new {Entity}TestDataBuilder();
    }

    public {Entity}TestDataBuilder withId(final UUID id) {
        this.id = id;
        return this;
    }

    public {Entity}TestDataBuilder withField1(final String field1) {
        this.field1 = field1;
        return this;
    }

    public {Entity}TestDataBuilder withData(final Map<String, Object> data) {
        this.data = new HashMap<>(data);
        return this;
    }

    public {Entity}TestDataBuilder withDataEntry(final String key, final Object value) {
        this.data.put(key, value);
        return this;
    }

    public {Entity} build() {
        return {Entity}.create{Entity}(id, field1, data);
    }
}
```

---

## Real Examples

### Aggregate Builder Example

**File:** `CompanyTestDataBuilder.java`

```java
package br.com.logistics.tms.shipmentorder.domain;

import br.com.logistics.tms.shipmentorder.application.usecases.SynchronizeCompanyUseCase;

import java.util.*;

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

    public CompanyTestDataBuilder withTypes(final List<String> types) {
        this.data.put(SynchronizeCompanyUseCase.TYPES_KEY, types);
        return this;
    }

    public CompanyTestDataBuilder withTypes(final String... types) {
        return withTypes(Arrays.asList(types));
    }

    public CompanyTestDataBuilder withData(final Map<String, Object> data) {
        this.data = new HashMap<>(data);
        return this;
    }

    public CompanyTestDataBuilder withDataEntry(final String key, final Object value) {
        this.data.put(key, value);
        return this;
    }

    public Company build() {
        if (data.isEmpty()) {
            data.put(SynchronizeCompanyUseCase.TYPES_KEY, List.of("DEFAULT"));
        }
        return Company.createCompany(companyId, data);
    }
}
```

### Use Case Input Builder Example

**File:** `SynchronizeCompanyUseCaseInputDataBuilder.java`

```java
package br.com.logistics.tms.shipmentorder.application.usecases.data;

import br.com.logistics.tms.shipmentorder.application.usecases.SynchronizeCompanyUseCase;

import java.util.*;

public class SynchronizeCompanyUseCaseInputDataBuilder {

    private UUID companyId = UUID.randomUUID();
    private Map<String, Object> data = new HashMap<>();

    public static SynchronizeCompanyUseCaseInputDataBuilder anInput() {
        return new SynchronizeCompanyUseCaseInputDataBuilder();
    }

    public SynchronizeCompanyUseCaseInputDataBuilder withCompanyId(final UUID companyId) {
        this.companyId = companyId;
        return this;
    }

    public SynchronizeCompanyUseCaseInputDataBuilder withTypes(final List<String> types) {
        this.data.put(SynchronizeCompanyUseCase.TYPES_KEY, types);
        return this;
    }

    public SynchronizeCompanyUseCaseInputDataBuilder withTypes(final String... types) {
        return withTypes(Arrays.asList(types));
    }

    public SynchronizeCompanyUseCaseInputDataBuilder withData(final Map<String, Object> data) {
        this.data = new HashMap<>(data);
        return this;
    }

    public SynchronizeCompanyUseCaseInputDataBuilder withNullData() {
        this.data = null;
        return this;
    }

    public SynchronizeCompanyUseCaseInputDataBuilder withEmptyData() {
        this.data = new HashMap<>();
        return this;
    }

    public SynchronizeCompanyUseCaseInputDataBuilder withDataEntry(final String key, final Object value) {
        if (this.data == null) {
            this.data = new HashMap<>();
        }
        this.data.put(key, value);
        return this;
    }

    public SynchronizeCompanyUseCase.Input build() {
        return new SynchronizeCompanyUseCase.Input(companyId, data);
    }
}
```

---

## Usage in Tests

### Before (Repetitive)

```java
@Test
void shouldCreateNewCompanyWhenCompanyDoesNotExist() {
    final UUID companyId = UUID.randomUUID();
    final List<String> types = List.of("LOGISTICS_PROVIDER", "CARRIER");
    final Map<String, Object> inputData = Map.of("types", types);
    final SynchronizeCompanyUseCase.Input input = new SynchronizeCompanyUseCase.Input(companyId, inputData);

    useCase.execute(input);

    // assertions...
}

@Test
void shouldUpdateExistingCompany() {
    final UUID companyId = UUID.randomUUID();
    final Map<String, Object> initialData = new HashMap<>();
    initialData.put("types", List.of("CARRIER"));
    initialData.put("name", "Test Company");
    final Company existingCompany = Company.createCompany(companyId, initialData);
    companyRepository.save(existingCompany);

    final List<String> newTypes = List.of("LOGISTICS_PROVIDER", "SHIPPER");
    final Map<String, Object> inputData = Map.of("types", newTypes);
    final SynchronizeCompanyUseCase.Input input = new SynchronizeCompanyUseCase.Input(companyId, inputData);
    
    // test logic...
}
```

### After (Concise with Builder)

```java
@Test
void shouldCreateNewCompanyWhenCompanyDoesNotExist() {
    final UUID companyId = UUID.randomUUID();
    final SynchronizeCompanyUseCase.Input input = SynchronizeCompanyUseCaseInputDataBuilder.anInput()
            .withCompanyId(companyId)
            .withTypes("LOGISTICS_PROVIDER", "CARRIER")
            .build();

    useCase.execute(input);

    // assertions...
}

@Test
void shouldUpdateExistingCompany() {
    final UUID companyId = UUID.randomUUID();
    final Company existingCompany = CompanyTestDataBuilder.aCompany()
            .withCompanyId(companyId)
            .withTypes("CARRIER")
            .withDataEntry("name", "Test Company")
            .build();
    companyRepository.save(existingCompany);

    final SynchronizeCompanyUseCase.Input input = SynchronizeCompanyUseCaseInputDataBuilder.anInput()
            .withCompanyId(companyId)
            .withTypes("LOGISTICS_PROVIDER", "SHIPPER")
            .build();
    
    // test logic...
}
```

---

## Key Principles

### ✅ DO

- **Provide sensible defaults** - Builder should work with just `build()` call
- **Use fluent interface** - All `withX()` methods return `this`
- **Use static factory method** - `aCompany()`, `anInput()`, etc.
- **Support varargs where appropriate** - `withTypes(String...)` in addition to `withTypes(List<String>)`
- **No JavaDoc comments** - Methods are self-documenting through clear naming
- **Keep builders simple** - No business logic, just object construction

### ❌ DON'T

- **Don't add validation** - Let the domain object validate
- **Don't add business logic** - Builders are for construction only
- **Don't add JavaDoc** - Class and method names should be self-explanatory
- **Don't make builders complex** - If builder is complex, the domain object may need refactoring

---

## Common Builder Methods

### For Domain Aggregates

```java
public static {Entity}TestDataBuilder a{Entity}()      // Factory method
public {Builder} withId(UUID id)                       // Set ID
public {Builder} with{Field}(Type field)               // Set specific field
public {Builder} withData(Map<String, Object> data)    // Set entire data map
public {Builder} withDataEntry(String key, Object val) // Add single data entry
public {Entity} build()                                // Build the object
```

### For Use Case Inputs

```java
public static {Input}DataBuilder anInput()             // Factory method
public {Builder} withCompanyId(UUID id)                // Set IDs
public {Builder} with{Field}(Type field)               // Set specific fields
public {Builder} withNullData()                        // Set null for testing
public {Builder} withEmptyData()                       // Set empty for testing
public {UseCase}.Input build()                         // Build the input record
```

---

## Naming Conventions

- **Class:** `{Entity}TestDataBuilder` or `{UseCase}InputDataBuilder`
- **Package (Aggregate):** `{module}.domain` (in test source root)
- **Package (Input):** `{module}.application.usecases.data` (in test source root)
- **Factory method:** `a{Entity}()` or `anInput()`
- **With methods:** `with{Field}()`
- **Build method:** `build()`

---

## Example Use Cases

### Testing Edge Cases

```java
@Test
void shouldHandleNullData() {
    final SynchronizeCompanyUseCase.Input input = SynchronizeCompanyUseCaseInputDataBuilder.anInput()
            .withNullData()
            .build();

    useCase.execute(input);

    assertEquals(0, companyRepository.count());
}

@Test
void shouldHandleEmptyTypes() {
    final SynchronizeCompanyUseCase.Input input = SynchronizeCompanyUseCaseInputDataBuilder.anInput()
            .withTypes(List.of())
            .build();

    useCase.execute(input);

    assertTrue(savedCompany.get().types().isEmpty());
}
```

### Testing with Specific IDs

```java
@Test
void shouldUpdateExistingCompany() {
    final UUID companyId = UUID.randomUUID();
    
    final Company existing = CompanyTestDataBuilder.aCompany()
            .withCompanyId(companyId)
            .build();
    companyRepository.save(existing);
    
    final SynchronizeCompanyUseCase.Input input = SynchronizeCompanyUseCaseInputDataBuilder.anInput()
            .withCompanyId(companyId)
            .withTypes("NEW_TYPE")
            .build();

    useCase.execute(input);
}
```

---

## Benefits

✅ **Reduced Repetition** - Write object construction logic once
✅ **Improved Readability** - Tests focus on what's being tested
✅ **Easy Maintenance** - Changes to object structure need updates in one place
✅ **Better Test Data** - Consistent defaults across tests
✅ **Flexible** - Easy to override defaults for specific tests
✅ **Self-Documenting** - Clear, fluent API tells you what you can configure

---

## Complete Example

See the `SynchronizeCompanyUseCaseTest` for a complete example of using test data builders:

- **Aggregate Builder:** `CompanyTestDataBuilder`
- **Input Builder:** `SynchronizeCompanyUseCaseInputDataBuilder`
- **Test File:** `src/test/java/br/com/logistics/tms/shipmentorder/application/usecases/SynchronizeCompanyUseCaseTest.java`

All 9 test methods demonstrate different uses of the builders for various testing scenarios.
