# Test Data Builder Pattern

## ⚡ TL;DR

- **When:** Creating test data with variations (3+ parameters, used in 3+ tests)
- **Why:** Reduce boilerplate, provide sensible defaults, fluent API for readability
- **Pattern:** Builder class with `with{Field}()` methods returning `this`, `build()` returns object
- **Confidence:** `low` (only 5 builders exist, under-adopted — target: 20+)

---

## Pattern

### Basic Builder Template

```java
package br.com.logistics.tms.builders.dto;

import br.com.logistics.tms.company.domain.CompanyType;
import br.com.logistics.tms.company.infrastructure.dto.CreateCompanyDTO;
import br.com.logistics.tms.utils.CnpjGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CreateCompanyDTOBuilder {

    private String name = "Default Company";
    private String cnpj = CnpjGenerator.randomCnpj();
    private Set<CompanyType> types = Set.of(CompanyType.SELLER);
    private Map<String, Object> configuration = new HashMap<>(Map.of(
            "notification", true
    ));

    public static CreateCompanyDTOBuilder aCreateCompanyDTO() {
        return new CreateCompanyDTOBuilder();
    }

    public CreateCompanyDTOBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public CreateCompanyDTOBuilder withCnpj(final String cnpj) {
        this.cnpj = cnpj;
        return this;
    }

    public CreateCompanyDTOBuilder withTypes(final Set<CompanyType> types) {
        this.types = types;
        return this;
    }

    public CreateCompanyDTOBuilder withTypes(final CompanyType... types) {
        this.types = Set.of(types);
        return this;
    }

    public CreateCompanyDTOBuilder withConfiguration(final Map<String, Object> configuration) {
        this.configuration = new HashMap<>(configuration);
        return this;
    }

    public CreateCompanyDTOBuilder withConfigurationEntry(final String key, final Object value) {
        this.configuration.put(key, value);
        return this;
    }

    public CreateCompanyDTO build() {
        return new CreateCompanyDTO(name, cnpj, types, configuration);
    }
}
```

### Usage in Tests

```java
@Test
void shouldCreateCompanyWithCustomName() {
    final CreateCompanyDTO dto = CreateCompanyDTOBuilder.aCreateCompanyDTO()
            .withName("Custom Corp")  // Override default
            .build();
    
    final CreateCompanyUseCase.Output output = useCase.execute(
        new CreateCompanyUseCase.Input(dto.name(), dto.cnpj(), dto.types(), dto.configuration())
    );
    
    assertEquals("Custom Corp", output.name());
}

@Test
void shouldCreateCompanyWithDefaultValues() {
    // Builder provides sensible defaults — no overrides needed
    final CreateCompanyDTO dto = CreateCompanyDTOBuilder.aCreateCompanyDTO().build();
    
    assertNotNull(dto.name());
    assertNotNull(dto.cnpj());
    assertFalse(dto.types().isEmpty());
}
```

---

## Builder Composition (Nested Objects)

For complex aggregates with nested structures, builders can compose other builders:

```java
public class ShipmentOrderBuilder {
    private UUID orderId = UUID.randomUUID();
    private Company company;  // Complex nested object
    
    public ShipmentOrderBuilder withCompany(final Company company) {
        this.company = company;
        return this;
    }
    
    public ShipmentOrder build() {
        if (company == null) {
            // Use nested builder for default
            company = CompanyBuilder.aCompany().build();
        }
        return ShipmentOrder.create(orderId, company);
    }
}

// Usage:
ShipmentOrder order = ShipmentOrderBuilder.anOrder()
        .withCompany(CompanyBuilder.aCompany().withTypes("CARRIER").build())
        .build();
```

---

## Location Strategy

**Centralized locations (per TEST_STRUCTURE.md):**

- **Domain builders:** `src/test/java/br/com/logistics/tms/builders/domain/{module}/`
  - Example: `CompanyBuilder.java` → `builders/domain/shipmentorder/`
- **Input builders:** `src/test/java/br/com/logistics/tms/builders/input/`
  - Example: `SynchronizeCompanyInputBuilder.java` → `builders/input/`
- **DTO builders:** `src/test/java/br/com/logistics/tms/builders/dto/`
  - Example: `CreateCompanyDTOBuilder.java` → `builders/dto/`

**Why centralized:** Reusable across unit and integration tests. Avoids duplication. Single source of truth for test data construction.

---

## When to Create a Builder

**Create builders when:**

- ✅ Object construction has **3+ parameters**
- ✅ Same object created in **3+ tests**
- ✅ Tests need **variations** (one test changes name, another changes CNPJ, another changes types)
- ✅ Object has **complex nested structures** (Map<String, Object> data fields)

**Skip builders for:**

- ❌ Simple objects (**1-2 fields only**)
- ❌ **One-time usage** in a single test
- ❌ Trivial value objects (String, UUID) — just inline them

---

## Common Builder Methods

### For DTOs (REST Layer)

```java
public static {DTO}Builder a{DTO}()                 // Factory method
public {Builder} with{Field}(Type field)            // Set specific field
public {Builder} with{Field}(Type... varargs)       // Varargs variant for collections
public {Builder} withConfiguration(Map data)        // Replace entire config
public {Builder} withConfigurationEntry(key, val)   // Add single config entry
public {DTO} build()                                // Build the DTO
```

### For Domain Aggregates

```java
public static {Entity}Builder a{Entity}()           // Factory method
public {Builder} withId(UUID id)                    // Set aggregate ID
public {Builder} with{Field}(Type field)            // Set specific field
public {Builder} withData(Map<String, Object> data) // Replace entire data map
public {Builder} withDataEntry(String key, Object val) // Add single data entry
public {Entity} build()                             // Build via aggregate factory method
```

### For Use Case Inputs

```java
public static {Input}Builder anInput()              // Factory method
public {Builder} withCompanyId(UUID id)             // Set IDs
public {Builder} with{Field}(Type field)            // Set specific fields
public {Builder} withNullData()                     // Set null for edge case testing
public {Builder} withEmptyData()                    // Set empty for validation testing
public {UseCase}.Input build()                      // Build the input record
```

---

## Key Principles

### ✅ DO

- **Provide sensible defaults** — Builder should work with just `.build()` call
- **Use fluent interface** — All `with{Field}()` methods return `this`
- **Use static factory method** — `aCompany()`, `anInput()`, `aCreateCompanyDTO()`
- **Support varargs where appropriate** — `withTypes(CompanyType...)` in addition to `withTypes(Set<CompanyType>)`
- **Make all variables final** — `private final String name;` EXCEPT builder's mutable fields
- **Keep builders simple** — No business logic, just object construction

### ❌ DON'T

- **Don't add validation** — Let the domain object or use case validate
- **Don't add business logic** — Builders are for construction only
- **Don't add JavaDoc** — Method names should be self-explanatory
- **Don't make builders complex** — If builder needs 20+ methods, the domain object may need refactoring

---

## Naming Conventions

- **Class:** `{Type}Builder` (e.g., `CreateCompanyDTOBuilder`, `CompanyBuilder`, `SynchronizeCompanyInputBuilder`)
- **Package (DTO):** `builders/dto/` (in test source root)
- **Package (Domain):** `builders/domain/{module}/` (in test source root)
- **Package (Input):** `builders/input/` (in test source root)
- **Factory method:** `a{Type}()` or `anInput()` (article + type name)
- **With methods:** `with{Field}()` (fluent setter pattern)
- **Build method:** `build()` (constructs the final object)

---

## Benefits

✅ **Reduced Repetition** — Write object construction logic once, reuse across all tests
✅ **Improved Readability** — Tests focus on **what's being tested**, not how to construct objects
✅ **Easy Maintenance** — Changes to object structure need updates in **one place only**
✅ **Better Test Data** — Consistent defaults across tests prevent flaky tests from random data
✅ **Flexible** — Easy to override defaults for specific test scenarios
✅ **Self-Documenting** — Clear, fluent API shows exactly what can be configured

---

## Metadata

**Confidence:** `low`  
**Reason:** Only 5 builders exist in the codebase (3 DTO, 1 input, 1 domain). Under-adopted pattern. Target: 20+ builders across all modules.

**Applies To:** `[company, shipmentorder, all modules with tests]`  
**Replaces:** `[prompts/test-data-builders.md]` (this skill extracts and standardizes that guidance)  
**Token Cost:** `~110 lines` (compact, code-heavy)  
**Related Skills:** `[fake-repository-pattern]` (builders + fakes = full in-memory testing)  

**Created:** `2025-02-24`  
**Last Updated:** `2025-02-24`  
**Author:** Trinity (Documentation Engineer)
