# Prompt: Create New Test Infrastructure

## ⚡ TL;DR

- **When:** Creating complete test infrastructure for a new entity
- **Why:** Comprehensive testing from domain to integration with builders and assertions
- **Pattern:** Custom assertions + test builders + fake repositories + integration fixtures
- **See:** Read on for complete template

---

## Purpose
Template for creating complete test infrastructure for a new entity in a TMS module.

---

## Instructions for AI Assistant

Create a complete test infrastructure package following TMS testing patterns.

### Required Information

**Entity Details:**
- **Entity Name:** `[EntityName]` (e.g., Company, ShipmentOrder, Quotation)
- **Module:** `[company | shipmentorder | quotation | other]`
- **Description:** `[Brief description of the entity's purpose]`

**Entity Fields:**
```
- field1: Type (description)
- field2: Type (description)
- field3: Type (description)
```

**Business Operations (Use Cases):**
```
- Create{Entity}: Description
- Update{Entity}: Description
- Delete{Entity}: Description
```

---

## Implementation Checklist

Generate the following test infrastructure files:

### 1. Domain Custom Assertion
**Location:** `src/test/java/br/com/logistics/tms/assertions/domain/{module}/{Entity}Assert.java`

**Purpose:** Fluent domain object validation for unit tests

**Pattern:**
```java
package br.com.logistics.tms.assertions.domain.{module};

import br.com.logistics.tms.{module}.domain.{Entity};
import br.com.logistics.tms.{module}.domain.{Entity}Id;
import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.Assertions.assertThat;

public class {Entity}Assert extends AbstractAssert<{Entity}Assert, {Entity}> {

    private {Entity}Assert(final {Entity} actual) {
        super(actual, {Entity}Assert.class);
    }

    public static {Entity}Assert assertThat{Entity}(final {Entity} actual) {
        return new {Entity}Assert(actual);
    }

    public {Entity}Assert has{Entity}Id(final {Entity}Id expected) {
        isNotNull();
        assertThat(actual.get{Entity}Id())
                .as("{Entity} ID")
                .isEqualTo(expected);
        return this;
    }

    public {Entity}Assert has{Field}(final {Type} expected) {
        isNotNull();
        assertThat(actual.get{Field}())
                .as("{Entity} {field}")
                .isEqualTo(expected);
        return this;
    }

    // Add method for each domain field
    // For boolean checks: isSomething() / isNotSomething()
    // For collections: hasItems() / hasItemsCount() / hasEmpty{Field}()
    // For maps: hasDataEntry() / hasDataKey()
}
```

**Example (ShipmentOrder Company):**
```java
assertThatCompany(company)
    .hasCompanyId(expectedId)
    .hasTypes("LOGISTICS_PROVIDER")
    .isLogisticsProvider()
    .hasStatus('A')
    .isActive();
```

---

### 2. Domain Test Data Builder
**Location:** `src/test/java/br/com/logistics/tms/builders/domain/{module}/{Entity}Builder.java`

**Purpose:** Construct domain aggregates with sensible defaults for unit tests

**Pattern:**
```java
package br.com.logistics.tms.builders.domain.{module};

import br.com.logistics.tms.{module}.domain.{Entity};
import br.com.logistics.tms.{module}.domain.{Entity}Id;

import java.util.*;

public class {Entity}Builder {

    private {Entity}Id {entity}Id = {Entity}Id.unique();
    private {Type1} field1 = {sensible default};
    private {Type2} field2 = {sensible default};
    // ... (all fields with sensible defaults)

    public static {Entity}Builder a{Entity}() {
        return new {Entity}Builder();
    }

    public {Entity}Builder with{Entity}Id(final {Entity}Id {entity}Id) {
        this.{entity}Id = {entity}Id;
        return this;
    }

    public {Entity}Builder with{Field}(final {Type} {field}) {
        this.{field} = {field};
        return this;
    }

    // Varargs variant for collections
    public {Entity}Builder with{Field}s(final {Type}... items) {
        this.{field}s = Set.of(items);
        return this;
    }

    // Map helpers for complex data fields
    public {Entity}Builder withData(final Map<String, Object> data) {
        this.data = new HashMap<>(data);
        return this;
    }

    public {Entity}Builder withDataEntry(final String key, final Object value) {
        this.data.put(key, value);
        return this;
    }

    public {Entity} build() {
        // Use aggregate factory method, NOT constructor
        return {Entity}.create{Entity}({field1}, {field2}, ...);
    }
}
```

**Key Points:**
- ✅ Sensible defaults for ALL fields
- ✅ Builder works with just `.build()` call
- ✅ Use domain factory methods in `build()`
- ✅ All parameters declared `final`

---

### 3. Fake Repository
**Location:** `src/test/java/br/com/logistics/tms/{module}/application/repositories/Fake{Entity}Repository.java`

**Purpose:** In-memory repository implementation for unit testing use cases

**Pattern:**
```java
package br.com.logistics.tms.{module}.application.repositories;

import br.com.logistics.tms.{module}.domain.{Entity};
import br.com.logistics.tms.{module}.domain.{Entity}Id;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public List<{Entity}> findAll() {
        return List.copyOf(storage.values());
    }

    // Implement any custom query methods from interface
    @Override
    public Optional<{Entity}> findBy{CustomField}(final {Type} field) {
        return storage.values().stream()
            .filter(e -> e.get{Field}().equals(field))
            .findFirst();
    }

    // Test helper methods
    public void clear() {
        storage.clear();
    }

    public int count() {
        return storage.size();
    }
}
```

**Key Points:**
- ✅ Implements repository interface completely
- ✅ Use domain value objects as keys (NOT UUIDs)
- ✅ Provide `clear()` and `count()` helpers
- ✅ All parameters declared `final`

**See Also:** `.squad/skills/fake-repository-pattern/SKILL.md`

---

### 4. Use Case Input Builder
**Location:** `src/test/java/br/com/logistics/tms/builders/input/{UseCase}InputBuilder.java`

**Purpose:** Construct use case inputs for unit tests

**Pattern:**
```java
package br.com.logistics.tms.builders.input;

import br.com.logistics.tms.{module}.application.usecases.{UseCase};
import br.com.logistics.tms.{module}.domain.{Entity}Id;

import java.util.*;

public class {UseCase}InputBuilder {

    private {Type1} field1 = {default};
    private {Type2} field2 = {default};
    // ... (all input fields)

    public static {UseCase}InputBuilder anInput() {
        return new {UseCase}InputBuilder();
    }

    public {UseCase}InputBuilder with{Field}(final {Type} field) {
        this.{field} = field;
        return this;
    }

    // Edge case helpers
    public {UseCase}InputBuilder withNullData() {
        this.data = null;
        return this;
    }

    public {UseCase}InputBuilder withEmptyData() {
        this.data = Map.of();
        return this;
    }

    public {UseCase}.Input build() {
        return new {UseCase}.Input({field1}, {field2}, ...);
    }
}
```

---

### 5. DTO Builder (REST Layer)
**Location:** `src/test/java/br/com/logistics/tms/builders/dto/Create{Entity}DTOBuilder.java`

**Purpose:** Construct REST DTOs for integration tests

**Pattern:**
```java
package br.com.logistics.tms.builders.dto;

import br.com.logistics.tms.{module}.infrastructure.dto.Create{Entity}DTO;

import java.util.*;

public class Create{Entity}DTOBuilder {

    private {Type1} field1 = {sensible default};
    private {Type2} field2 = {sensible default};
    private Map<String, Object> configuration = new HashMap<>(Map.of(
            "default-key", "default-value"
    ));

    public static Create{Entity}DTOBuilder aCreate{Entity}DTO() {
        return new Create{Entity}DTOBuilder();
    }

    public Create{Entity}DTOBuilder with{Field}(final {Type} field) {
        this.{field} = field;
        return this;
    }

    public Create{Entity}DTOBuilder withConfiguration(final Map<String, Object> configuration) {
        this.configuration = new HashMap<>(configuration);
        return this;
    }

    public Create{Entity}DTOBuilder withConfigurationEntry(final String key, final Object value) {
        this.configuration.put(key, value);
        return this;
    }

    public Create{Entity}DTO build() {
        return new Create{Entity}DTO({field1}, {field2}, configuration);
    }
}
```

**See Also:** `.squad/skills/test-data-builder-pattern/SKILL.md`

---

### 6. JPA Entity Assertion
**Location:** `src/test/java/br/com/logistics/tms/assertions/jpa/{Entity}EntityAssert.java`

**Purpose:** Fluent JPA entity validation for integration tests

**Pattern:**
```java
package br.com.logistics.tms.assertions.jpa;

import br.com.logistics.tms.{module}.infrastructure.jpa.{Entity}Entity;
import org.assertj.core.api.AbstractAssert;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class {Entity}EntityAssert extends AbstractAssert<{Entity}EntityAssert, {Entity}Entity> {

    private {Entity}EntityAssert(final {Entity}Entity actual) {
        super(actual, {Entity}EntityAssert.class);
    }

    public static {Entity}EntityAssert assertThat{Entity}(final {Entity}Entity actual) {
        return new {Entity}EntityAssert(actual);
    }

    public {Entity}EntityAssert hasId(final UUID expected) {
        isNotNull();
        assertThat(actual.getId())
                .as("{Entity} ID")
                .isEqualTo(expected);
        return this;
    }

    public {Entity}EntityAssert has{Field}(final {Type} expected) {
        isNotNull();
        assertThat(actual.get{Field}())
                .as("{Entity} {field}")
                .isEqualTo(expected);
        return this;
    }

    // Add assertions for JPA-specific concerns:
    // - Timestamps (createdAt, updatedAt)
    // - Status/flags
    // - Relationships (@OneToMany, @ManyToOne)
}
```

---

### 7. Integration Fixture
**Location:** `src/test/java/br/com/logistics/tms/integration/fixtures/{Entity}IntegrationFixture.java`

**Purpose:** Encapsulate REST operations + async waiting for integration tests

**Pattern:**
```java
package br.com.logistics.tms.integration.fixtures;

import br.com.logistics.tms.{module}.domain.{Entity}Id;
import br.com.logistics.tms.{module}.infrastructure.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class {Entity}IntegrationFixture {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final {Entity}OutboxJpaRepository outboxRepository;

    public {Entity}IntegrationFixture(final MockMvc mockMvc,
                                     final ObjectMapper objectMapper,
                                     final {Entity}OutboxJpaRepository outboxRepository) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.outboxRepository = outboxRepository;
    }

    public {Entity}Id create{Entity}(final Create{Entity}DTO dto) throws Exception {
        // 1. POST to REST endpoint
        final String responseJson = mockMvc.perform(post("/{entities}")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        final Create{Entity}ResponseDTO response = objectMapper.readValue(
            responseJson, Create{Entity}ResponseDTO.class
        );
        final {Entity}Id id = new {Entity}Id(response.{entity}Id());

        // 2. Wait for outbox published
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> outboxRepository
                .findFirstByAggregateIdOrderByCreatedAtDesc(id.value())
                .map(outbox -> outbox.getStatus() == OutboxStatus.PUBLISHED)
                .orElse(false)
            );

        return id;
    }

    public void update{Entity}(final UUID id, final Update{Entity}DTO dto) throws Exception {
        mockMvc.perform(put("/{entities}/" + id)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk());

        // Wait for outbox if event-driven
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> outboxRepository
                .findFirstByAggregateIdOrderByCreatedAtDesc(id)
                .map(outbox -> outbox.getStatus() == OutboxStatus.PUBLISHED)
                .orElse(false)
            );
    }

    public void delete{Entity}(final UUID id) throws Exception {
        mockMvc.perform(delete("/{entities}/" + id))
            .andExpect(status().isNoContent());
    }
}
```

**Key Points:**
- ✅ Returns typed domain IDs (NOT raw UUIDs)
- ✅ Encapsulates REST + waiting logic
- ✅ Uses Awaitility for async validation
- ✅ NOT a `@Component` — instantiated manually

---

### 8. Domain Unit Tests
**Location:** `src/test/java/br/com/logistics/tms/{module}/domain/{Entity}Test.java`

**Purpose:** Test aggregate behavior, immutability, domain events

**Pattern:**
```java
package br.com.logistics.tms.{module}.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static br.com.logistics.tms.assertions.domain.{module}.{Entity}Assert.assertThat{Entity};
import static br.com.logistics.tms.builders.domain.{module}.{Entity}Builder.a{Entity};

class {Entity}Test {

    @Test
    void shouldCreate{Entity}WithValidData() {
        // Given
        final {Type1} field1 = {value};
        final {Type2} field2 = {value};

        // When
        final {Entity} entity = {Entity}.create{Entity}(field1, field2, ...);

        // Then
        assertThat{Entity}(entity)
            .has{Entity}Id({Entity}Id.unique())
            .has{Field1}(field1)
            .has{Field2}(field2);

        // Verify domain event
        assertEquals(1, entity.getDomainEvents().size());
        assertTrue(entity.getDomainEvents().stream()
            .anyMatch(e -> e instanceof {Entity}Created));
    }

    @Test
    void shouldThrowExceptionWhenCreatingWithInvalidData() {
        // When & Then
        assertThrows(ValidationException.class, () ->
            {Entity}.create{Entity}(null, ...)
        );
    }

    @Test
    void shouldUpdateFieldAndReturnNewInstance() {
        // Given
        final {Entity} original = a{Entity}().build();
        final {Type} newValue = {value};

        // When
        final {Entity} updated = original.update{Field}(newValue);

        // Then
        assertNotSame(original, updated);  // Different instances
        assertEquals({oldValue}, original.get{Field}());  // Original unchanged
        assertEquals(newValue, updated.get{Field}());  // Updated has new value

        // Verify domain event
        assertTrue(updated.getDomainEvents().stream()
            .anyMatch(e -> e instanceof {Entity}Updated));
    }

    @Test
    void shouldReturnSameInstanceWhenUpdateWithSameValue() {
        // Given
        final {Type} value = {value};
        final {Entity} original = a{Entity}()
            .with{Field}(value)
            .build();

        // When
        final {Entity} result = original.update{Field}(value);

        // Then
        assertSame(original, result);  // Same instance returned
    }
}
```

---

### 9. Use Case Unit Tests
**Location:** `src/test/java/br/com/logistics/tms/{module}/application/usecases/{UseCase}Test.java`

**Purpose:** Test use case logic with fake repository

**Pattern:**
```java
package br.com.logistics.tms.{module}.application.usecases;

import br.com.logistics.tms.{module}.application.repositories.Fake{Entity}Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static br.com.logistics.tms.builders.input.{UseCase}InputBuilder.anInput;
import static br.com.logistics.tms.builders.domain.{module}.{Entity}Builder.a{Entity};

class {UseCase}Test {

    private Fake{Entity}Repository repository;
    private {UseCase} useCase;

    @BeforeEach
    void setUp() {
        repository = new Fake{Entity}Repository();
        useCase = new {UseCase}(repository);
    }

    @Test
    void shouldExecuteSuccessfully() {
        // Given
        final {UseCase}.Input input = anInput()
            .with{Field}({value})
            .build();

        // When
        final {UseCase}.Output output = useCase.execute(input);

        // Then
        assertNotNull(output);
        assertNotNull(output.{entity}Id());
        assertTrue(repository.existsById(new {Entity}Id(output.{entity}Id())));
        assertEquals(1, repository.count());
    }

    @Test
    void shouldThrowExceptionWhenDuplicateFound() {
        // Given
        final {Entity} existing = a{Entity}().build();
        repository.save(existing);

        final {UseCase}.Input input = anInput()
            .with{Field}(existing.get{Field}())
            .build();

        // When & Then
        assertThrows(ValidationException.class, () ->
            useCase.execute(input)
        );
    }

    @Test
    void shouldUpdateExistingEntity() {
        // Given
        final {Entity} existing = a{Entity}().build();
        repository.save(existing);

        final {UseCase}.Input input = anInput()
            .with{Entity}Id(existing.get{Entity}Id())
            .with{Field}({newValue})
            .build();

        // When
        final {UseCase}.Output output = useCase.execute(input);

        // Then
        final {Entity} updated = repository.findById(existing.get{Entity}Id()).orElseThrow();
        assertEquals({newValue}, updated.get{Field}());
    }
}
```

---

### 10. Integration Story Test
**Location:** `src/test/java/br/com/logistics/tms/integration/{Entity}IT.java`

**Purpose:** End-to-end business flow validation (story-driven)

**Pattern:**
```java
package br.com.logistics.tms.integration;

import br.com.logistics.tms.AbstractIntegrationTest;
import br.com.logistics.tms.integration.fixtures.{Entity}IntegrationFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static br.com.logistics.tms.builders.dto.Create{Entity}DTOBuilder.aCreate{Entity}DTO;
import static br.com.logistics.tms.assertions.jpa.{Entity}EntityAssert.assertThat{Entity};

class {Entity}IT extends AbstractIntegrationTest {

    @Autowired
    private {Entity}IntegrationFixture {entity}Fixture;

    @Autowired
    private {Entity}JpaRepository {entity}Repository;

    @Test
    void shouldCompleteFullEntityLifecycle() throws Exception {
        // Story: User creates, updates, and deletes an entity

        // Given: User wants to create a new entity
        final Create{Entity}DTO createDto = aCreate{Entity}DTO()
            .with{Field1}({value1})
            .with{Field2}({value2})
            .build();

        // When: User creates the entity
        final {Entity}Id id = {entity}Fixture.create{Entity}(createDto);

        // Then: Entity exists with correct data
        final {Entity}Entity entity = {entity}Repository.findById(id.value()).orElseThrow();
        assertThat{Entity}(entity)
            .hasId(id.value())
            .has{Field1}({value1})
            .has{Field2}({value2});

        // When: User updates the entity
        final Update{Entity}DTO updateDto = anUpdate{Entity}DTO()
            .with{Field1}({newValue})
            .build();
        {entity}Fixture.update{Entity}(id.value(), updateDto);

        // Then: Entity updated with new data
        final {Entity}Entity updated = {entity}Repository.findById(id.value()).orElseThrow();
        assertThat{Entity}(updated)
            .has{Field1}({newValue});

        // When: User deletes the entity
        {entity}Fixture.delete{Entity}(id.value());

        // Then: Entity no longer exists
        assertFalse({entity}Repository.existsById(id.value()));
    }

    @Test
    void shouldPublishDomainEventsToOutbox() throws Exception {
        // Story: Entity creation triggers domain event publication

        // When: User creates an entity
        final {Entity}Id id = {entity}Fixture.create{Entity}(
            aCreate{Entity}DTO().build()
        );

        // Then: Domain event published to outbox
        final List<{Entity}OutboxEntity> events = outboxRepository
            .findAllByAggregateId(id.value());
        assertFalse(events.isEmpty());
        assertEquals(OutboxStatus.PUBLISHED, events.get(0).getStatus());
    }
}
```

**Key Points:**
- ✅ Story-driven naming and structure
- ✅ Complete business flows (create → update → delete)
- ✅ Validates REST + persistence + events
- ✅ Uses fixtures for repetitive operations

---

## Validation Checklist

Before marking test infrastructure complete, verify:

- [ ] **Domain assertion** created with fluent API
- [ ] **Domain builder** created with sensible defaults
- [ ] **Fake repository** implements all interface methods
- [ ] **Input builder** created for all use cases
- [ ] **DTO builders** created (Create, Update, etc.)
- [ ] **JPA assertion** created for integration tests
- [ ] **Integration fixture** encapsulates REST + waiting
- [ ] **Domain tests** cover creation, updates, validation
- [ ] **Use case tests** use fake repository (NO Spring)
- [ ] **Integration tests** tell complete business stories
- [ ] All test files use proper naming conventions (`*Test.java`, `*IT.java`)
- [ ] All parameters declared `final`
- [ ] Static imports used for builders and assertions

---

## Example Request

```
Entity: Quotation
Module: quotation
Description: Price estimate for transportation services

Fields:
- quotationId: QuotationId (aggregate root ID)
- origin: Address (pick-up location)
- destination: Address (delivery location)
- volume: Volume (weight + dimensions)
- price: Price (money amount)
- status: QuotationStatus (PENDING, APPROVED, REJECTED, EXPIRED)

Use Cases:
- CreateQuotation: Generate new quotation
- ApproveQuotation: Mark quotation as approved
- RejectQuotation: Mark quotation as rejected
- ExpireQuotation: Mark quotation as expired (time-based)
```

**Generated Files:**
1. `QuotationAssert.java` (domain assertion)
2. `QuotationBuilder.java` (domain builder)
3. `FakeQuotationRepository.java` (fake repository)
4. `CreateQuotationInputBuilder.java` (use case input)
5. `CreateQuotationDTOBuilder.java` (REST DTO)
6. `QuotationEntityAssert.java` (JPA assertion)
7. `QuotationIntegrationFixture.java` (integration fixture)
8. `QuotationTest.java` (domain tests)
9. `CreateQuotationUseCaseTest.java` (use case tests)
10. `QuotationIT.java` (integration story test)

---

## References

- **Test Structure:** `/doc/ai/TEST_STRUCTURE.md`
- **Fake Repository Pattern:** `.squad/skills/fake-repository-pattern/SKILL.md`
- **Test Data Builder Pattern:** `.squad/skills/test-data-builder-pattern/SKILL.md`
- **Integration Tests:** `/doc/ai/INTEGRATION_TESTS.md`
- **Existing Examples:** `src/test/java/br/com/logistics/tms/assertions/`, `builders/`, `integration/`
