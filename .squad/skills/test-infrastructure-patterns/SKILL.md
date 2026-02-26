# Test Infrastructure Patterns

## ⚡ TL;DR

- **When:** Creating tests for NEW entities in TMS (aggregates, use cases, REST APIs)
- **Why:** Consistent, readable, maintainable tests across domain, unit, and integration layers
- **Pattern:** Six complementary patterns working together as a complete test infrastructure
- **Confidence:** 🟢 High (validated across Company and Agreement entities, 70+ test cases)

---

## Overview

TMS uses a **layered test infrastructure** that provides reusable components for testing at all levels:

1. **Custom AssertJ Assertions** — Domain-aware fluent assertions
2. **Test Data Builders** — Fluent builders with sensible defaults
3. **Fake Repositories** — In-memory implementations for unit tests (NO Spring)
4. **Integration Fixtures** — Encapsulate REST calls + validation
5. **Story-Driven Integration Tests** — Single test = complete business flow
6. **Unit Test Structure** — Use case tests with fakes, builders, assertions

**Key Principle:** Each layer provides the RIGHT tools for its level of testing. Domain tests use builders + assertions. Use case tests add fake repositories. Integration tests add fixtures + real infrastructure.

---

## Pattern 1: Custom AssertJ Assertions

### When to Use

- Testing domain objects (aggregates, value objects, events)
- Need readable, chainable assertions
- Want domain-specific validation methods
- Have 5+ common assertion patterns for an entity

### Structure

```java
package br.com.logistics.tms.assertions.domain.{module};

import br.com.logistics.tms.{module}.domain.{Entity};
import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.Assertions.assertThat;

public class {Entity}Assert extends AbstractAssert<{Entity}Assert, {Entity}> {

    private {Entity}Assert(final {Entity} actual) {
        super(actual, {Entity}Assert.class);
    }

    public static {Entity}Assert assertThat{Entity}(final {Entity} actual) {
        return new {Entity}Assert(actual);
    }

    // Identity assertions
    public {Entity}Assert has{Entity}Id(final UUID expectedId) {
        isNotNull();
        assertThat(actual.get{Entity}Id().value())
                .as("{Entity} ID")
                .isEqualTo(expectedId);
        return this;
    }

    // Relationship assertions
    public {Entity}Assert hasRelatedEntity(final RelatedEntityId expected) {
        isNotNull();
        assertThat(actual.getRelatedEntity())
                .as("Related entity")
                .isEqualTo(expected);
        return this;
    }

    // State assertions
    public {Entity}Assert isActive() {
        isNotNull();
        assertThat(actual.isActive())
                .as("{Entity} is active")
                .isTrue();
        return this;
    }

    // Collection assertions
    public {Entity}Assert hasItemsCount(final int expectedCount) {
        isNotNull();
        assertThat(actual.getItems())
                .as("{Entity} items count")
                .hasSize(expectedCount);
        return this;
    }

    // Complex behavior assertions
    public {Entity}Assert overlapsWith(final {Entity} other) {
        isNotNull();
        assertThat(actual.overlapsWith(other))
                .as("{Entity} overlaps with other")
                .isTrue();
        return this;
    }
}
```

### Real Example: AgreementAssert

```java
public class AgreementAssert extends AbstractAssert<AgreementAssert, Agreement> {

    private AgreementAssert(final Agreement actual) {
        super(actual, AgreementAssert.class);
    }

    public static AgreementAssert assertThatAgreement(final Agreement actual) {
        return new AgreementAssert(actual);
    }

    public AgreementAssert hasAgreementId(final UUID expectedAgreementId) {
        isNotNull();
        assertThat(actual.agreementId().value())
                .as("Agreement ID")
                .isEqualTo(expectedAgreementId);
        return this;
    }

    public AgreementAssert hasFrom(final CompanyId expectedFrom) {
        isNotNull();
        assertThat(actual.from())
                .as("Agreement source company")
                .isEqualTo(expectedFrom);
        return this;
    }

    public AgreementAssert hasType(final AgreementType expectedType) {
        isNotNull();
        assertThat(actual.type())
                .as("Agreement type")
                .isEqualTo(expectedType);
        return this;
    }

    public AgreementAssert isActive() {
        isNotNull();
        assertThat(actual.isActive())
                .as("Agreement is active")
                .isTrue();
        return this;
    }

    public AgreementAssert overlapsWith(final Agreement other) {
        isNotNull();
        assertThat(actual.overlapsWith(other))
                .as("Agreement overlaps with other agreement")
                .isTrue();
        return this;
    }
}
```

### Usage in Tests

```java
final Agreement agreement = AgreementBuilder.anAgreement()
    .withFrom(sourceCompanyId)
    .withTo(destCompanyId)
    .withType(AgreementType.DELIVERS_WITH)
    .build();

assertThatAgreement(agreement)
    .hasFrom(sourceCompanyId)
    .hasTo(destCompanyId)
    .hasType(AgreementType.DELIVERS_WITH)
    .isActive()
    .doesNotOverlapWith(otherAgreement);
```

### Key Points

- ✅ Extends `AbstractAssert<TAssert, TActual>`
- ✅ Private constructor + public static factory method
- ✅ All assertion methods return `this` for chaining
- ✅ Use `.as("description")` for clear failure messages
- ✅ All parameters declared `final`
- ✅ Location: `src/test/java/br/com/logistics/tms/assertions/domain/{module}/`

---

## Pattern 2: Test Data Builders

### When to Use

- Creating test data with variations (3+ parameters)
- Need sensible defaults to avoid boilerplate
- Want fluent API for test readability
- Used in 3+ tests for the same entity

### Structure

```java
package br.com.logistics.tms.builders.domain.{module};

import br.com.logistics.tms.{module}.domain.*;

import java.util.*;

public class {Entity}Builder {

    // Default values
    private FieldType field1 = defaultValue1();
    private FieldType field2 = defaultValue2();
    private Set<Item> items = new HashSet<>();
    private Map<String, Object> configuration = new HashMap<>();

    public static {Entity}Builder an{Entity}() {
        return new {Entity}Builder();
    }

    public {Entity}Builder withField1(final FieldType field1) {
        this.field1 = field1;
        return this;
    }

    public {Entity}Builder withField2(final FieldType field2) {
        this.field2 = field2;
        return this;
    }

    public {Entity}Builder withItems(final Set<Item> items) {
        this.items = new HashSet<>(items);
        return this;
    }

    public {Entity}Builder withItem(final Item item) {
        this.items.add(item);
        return this;
    }

    public {Entity}Builder withConfiguration(final Map<String, Object> configuration) {
        this.configuration = new HashMap<>(configuration);
        return this;
    }

    public {Entity}Builder withConfigurationEntry(final String key, final Object value) {
        this.configuration.put(key, value);
        return this;
    }

    public {Entity} build() {
        // Apply minimal default if empty
        if (configuration.isEmpty()) {
            configuration.put("default", true);
        }
        return {Entity}.create{Entity}(field1, field2, items, configuration);
    }
}
```

### Real Example: AgreementBuilder

```java
public class AgreementBuilder {

    private CompanyId from = CompanyId.unique();
    private CompanyId to = CompanyId.unique();
    private AgreementType type = AgreementType.DELIVERS_WITH;
    private Map<String, Object> configuration = new HashMap<>();
    private Set<AgreementCondition> conditions = new HashSet<>();
    private Instant validFrom = Instant.now();
    private Instant validTo = null;

    public static AgreementBuilder anAgreement() {
        return new AgreementBuilder();
    }

    public AgreementBuilder withFrom(final CompanyId from) {
        this.from = from;
        return this;
    }

    public AgreementBuilder withFrom(final UUID fromId) {
        this.from = CompanyId.with(fromId);
        return this;
    }

    public AgreementBuilder withTo(final CompanyId to) {
        this.to = to;
        return this;
    }

    public AgreementBuilder withType(final AgreementType type) {
        this.type = type;
        return this;
    }

    public AgreementBuilder withConfigurationEntry(final String key, final Object value) {
        this.configuration.put(key, value);
        return this;
    }

    public AgreementBuilder withCondition(final AgreementCondition condition) {
        this.conditions.add(condition);
        return this;
    }

    public AgreementBuilder withValidFrom(final Instant validFrom) {
        this.validFrom = validFrom;
        return this;
    }

    public AgreementBuilder withValidTo(final Instant validTo) {
        this.validTo = validTo;
        return this;
    }

    public AgreementBuilder withNoValidTo() {
        this.validTo = null;
        return this;
    }

    public Agreement build() {
        if (configuration.isEmpty()) {
            configuration.put("default", true);
        }
        return Agreement.createAgreement(from, to, type, configuration, conditions, validFrom, validTo);
    }
}
```

### Usage in Tests

```java
// Minimal test data with defaults
final Agreement agreement1 = AgreementBuilder.anAgreement().build();

// Customized test data
final Agreement agreement2 = AgreementBuilder.anAgreement()
    .withFrom(companyA)
    .withTo(companyB)
    .withType(AgreementType.DELIVERS_WITH)
    .withConfigurationEntry("discountPercent", 10)
    .withValidTo(Instant.now().plus(365, ChronoUnit.DAYS))
    .build();
```

### Key Points

- ✅ Private fields with sensible defaults
- ✅ Static factory method `an{Entity}()`
- ✅ Fluent `with{Field}()` methods returning `this`
- ✅ Support for both value objects and primitives (convenience overloads)
- ✅ Defensive copies for collections/maps
- ✅ `build()` calls domain factory method (NOT constructor)
- ✅ All parameters declared `final`
- ✅ Location: `src/test/java/br/com/logistics/tms/builders/domain/{module}/`

---

## Pattern 3: Fake Repositories

### When to Use

- Unit testing use cases WITHOUT database
- Need fast, predictable repository behavior
- Want to avoid mock boilerplate (`when().thenReturn()`)
- Testing use case logic in isolation

### Structure

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
    public Optional<{Entity}> get{Entity}ById(final {Entity}Id id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public {Entity} create(final {Entity} entity) {
        storage.put(entity.get{Entity}Id(), entity);
        return entity;
    }

    @Override
    public {Entity} update(final {Entity} entity) {
        storage.put(entity.get{Entity}Id(), entity);
        return entity;
    }

    @Override
    public void delete(final {Entity} entity) {
        storage.remove(entity.get{Entity}Id());
    }

    // Test helper methods
    public void clear() {
        storage.clear();
    }

    public int count() {
        return storage.size();
    }

    public boolean existsById(final {Entity}Id id) {
        return storage.containsKey(id);
    }
}
```

### Real Example: FakeCompanyRepository

```java
public class FakeCompanyRepository implements CompanyRepository {

    private final Map<CompanyId, Company> storage = new HashMap<>();

    @Override
    public Optional<Company> getCompanyById(final CompanyId id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<Company> getCompanyByCnpj(final Cnpj cnpj) {
        return storage.values().stream()
                .filter(company -> company.getCnpj().equals(cnpj))
                .findFirst();
    }

    @Override
    public Optional<Company> findCompanyByAgreementId(final AgreementId agreementId) {
        return storage.values().stream()
                .filter(company -> company.getAgreements().stream()
                        .anyMatch(agreement -> agreement.agreementId().equals(agreementId)))
                .findFirst();
    }

    @Override
    public Company create(final Company company) {
        storage.put(company.getCompanyId(), company);
        return company;
    }

    @Override
    public Company update(final Company company) {
        storage.put(company.getCompanyId(), company);
        return company;
    }

    @Override
    public void delete(final Company company) {
        storage.remove(company.getCompanyId());
    }

    public void clear() {
        storage.clear();
    }

    public int count() {
        return storage.size();
    }

    public boolean existsById(final CompanyId companyId) {
        return storage.containsKey(companyId);
    }
}
```

### Usage in Use Case Tests

```java
class CreateAgreementUseCaseTest {

    private FakeCompanyRepository companyRepository;
    private CreateAgreementUseCase useCase;

    @BeforeEach
    void setUp() {
        companyRepository = new FakeCompanyRepository();
        useCase = new CreateAgreementUseCase(companyRepository);
    }

    @Test
    void shouldCreateAgreementBetweenTwoCompanies() {
        // Setup: Create test data
        final Company sourceCompany = Company.createCompany(
                "Shoppe Logistics",
                "12345678901234",
                Set.of(CompanyType.SELLER),
                Map.of("region", "SP")
        );
        final Company destinationCompany = Company.createCompany(
                "Loggi Transportes",
                "98765432109876",
                Set.of(CompanyType.LOGISTICS_PROVIDER),
                Map.of("region", "SP")
        );
        companyRepository.create(sourceCompany);
        companyRepository.create(destinationCompany);

        // Execute: Call use case
        final CreateAgreementUseCase.Input input = new CreateAgreementUseCase.Input(
                sourceCompany.getCompanyId().value(),
                destinationCompany.getCompanyId().value(),
                AgreementType.DELIVERS_WITH,
                Map.of("discount", 15.0),
                Set.of(),
                Instant.now(),
                Instant.now().plus(365, ChronoUnit.DAYS)
        );
        final CreateAgreementUseCase.Output output = useCase.execute(input);

        // Verify: Check results
        assertThat(output.agreementId()).isNotNull();
        final Company updatedCompany = companyRepository.getCompanyById(sourceCompany.getCompanyId())
                .orElseThrow();
        assertThat(updatedCompany.getAgreements()).hasSize(1);
    }
}
```

### Key Points

- ✅ NO Spring annotations — pure Java class
- ✅ Implements repository interface from application layer
- ✅ In-memory `Map` storage (key = entity ID)
- ✅ Query methods use stream operations
- ✅ Test helper methods (`clear()`, `count()`, `existsById()`)
- ✅ Fast execution (no database roundtrip)
- ✅ All parameters declared `final`
- ✅ Location: `src/test/java/br/com/logistics/tms/{module}/application/repositories/`

---

## Pattern 4: Integration Fixtures

### When to Use

- Integration tests need to make REST calls
- Want to encapsulate HTTP request + validation
- Need to extract response data (IDs) for chaining
- Reduce boilerplate in integration test setup

### Structure

```java
package br.com.logistics.tms.integration.fixtures;

import br.com.logistics.tms.{module}.domain.{Entity}Id;
import br.com.logistics.tms.{module}.infrastructure.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class {Entity}IntegrationFixture {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    public {Entity}IntegrationFixture(final MockMvc mockMvc,
                                      final ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    public {Entity}Id create{Entity}(final Create{Entity}DTO dto) throws Exception {
        final String json = objectMapper.writeValueAsString(dto);

        final String response = mockMvc.perform(post("/{entities}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.{entity}Id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        return {Entity}Id.with((String) responseMap.get("{entity}Id"));
    }

    public void update{Entity}(final UUID {entity}Id,
                               final Update{Entity}DTO dto) throws Exception {
        final String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put("/{entities}/" + {entity}Id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.{entity}Id").value({entity}Id.toString()));
    }

    public void remove{Entity}(final UUID {entity}Id) throws Exception {
        mockMvc.perform(delete("/{entities}/" + {entity}Id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    public Map<String, Object> get{Entity}(final UUID {entity}Id) throws Exception {
        final String response = mockMvc.perform(get("/{entities}/" + {entity}Id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.{entity}Id").value({entity}Id.toString()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, Map.class);
    }
}
```

### Real Example: AgreementIntegrationFixture

```java
public class AgreementIntegrationFixture {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    public AgreementIntegrationFixture(final MockMvc mockMvc,
                                       final ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    public AgreementId createAgreement(final UUID sourceCompanyId,
                                       final CreateAgreementDTO dto) throws Exception {
        final String json = objectMapper.writeValueAsString(dto);

        final String response = mockMvc.perform(post("/companies/" + sourceCompanyId + "/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.agreementId").exists())
                .andExpect(jsonPath("$.sourceCompanyId").value(sourceCompanyId.toString()))
                .andExpect(jsonPath("$.destinationCompanyId").value(dto.destinationCompanyId().toString()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        final Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        return AgreementId.with((String) responseMap.get("agreementId"));
    }

    public void updateAgreement(final UUID sourceCompanyId,
                                final UUID agreementId,
                                final UpdateAgreementDTO dto) throws Exception {
        final String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put("/companies/" + sourceCompanyId + "/agreements/" + agreementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agreementId").value(agreementId.toString()));
    }

    public void removeAgreement(final UUID sourceCompanyId,
                                final UUID agreementId) throws Exception {
        mockMvc.perform(delete("/companies/" + sourceCompanyId + "/agreements/" + agreementId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    public Map<String, Object> getAgreement(final UUID sourceCompanyId,
                                            final UUID agreementId) throws Exception {
        final String response = mockMvc.perform(get("/companies/" + sourceCompanyId + "/agreements/" + agreementId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agreementId").value(agreementId.toString()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, Map.class);
    }
}
```

### Key Points

- ✅ Encapsulates MockMvc + ObjectMapper
- ✅ Methods return extracted IDs for chaining
- ✅ Includes basic JSON validation (`.andExpect(jsonPath(...))`)
- ✅ Handles serialization/deserialization
- ✅ All parameters declared `final`
- ✅ Location: `src/test/java/br/com/logistics/tms/integration/fixtures/`

---

## Pattern 5: Story-Driven Integration Tests

### When to Use

- Testing complete business flows end-to-end
- Need to validate REST → Use Case → Repository → Database
- Want readable tests that document system behavior
- Testing persistence, cascade operations, events

### Structure

```java
package br.com.logistics.tms.integration;

import br.com.logistics.tms.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class {Entity}{Feature}IT extends AbstractIntegrationTest {

    @Test
    void shouldExecuteCompleteBusinessFlowFromCreationToDeletion() throws Exception {
        // Story Part 1: Setup dependencies
        final DependencyId dep1 = dependencyFixture.createDependency(...);
        final DependencyId dep2 = dependencyFixture.createDependency(...);

        // Story Part 2: Create main entity
        final {Entity}Id entityId = {entity}Fixture.create{Entity}(...);

        // Story Part 3: Verify persistence
        final {Entity}Entity persisted = entityJpaRepository.findById(entityId.value()).orElseThrow();
        assertThat{Entity}(persisted)
            .hasExpectedField1(...)
            .hasExpectedField2(...);

        // Story Part 4: Update entity
        {entity}Fixture.update{Entity}(entityId.value(), ...);

        // Story Part 5: Verify update in database
        final {Entity}Entity updated = entityJpaRepository.findById(entityId.value()).orElseThrow();
        assertThat{Entity}(updated)
            .hasUpdatedField(...);

        // Story Part 6: Remove entity
        {entity}Fixture.remove{Entity}(entityId.value());

        // Story Part 7: Verify deletion (entity gone, dependencies remain)
        assertThat(entityJpaRepository.findById(entityId.value())).isEmpty();
        assertThat(dependencyJpaRepository.findById(dep1.value())).isPresent();
        assertThat(dependencyJpaRepository.findById(dep2.value())).isPresent();
    }
}
```

### Real Example: CompanyAgreementIT

```java
class CompanyAgreementIT extends AbstractIntegrationTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void shouldCreateUpdateAndRemoveAgreementBetweenCompaniesInCompleteBusinessFlow() throws Exception {
        // Story Part 1: Create Shoppe marketplace
        final CompanyId shoppeId = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withName("Shoppe")
                        .withTypes(CompanyType.MARKETPLACE)
                        .build()
        );

        final CompanyEntity shoppe = companyJpaRepository.findById(shoppeId.value()).orElseThrow();
        assertThatCompany(shoppe)
                .hasName("Shoppe")
                .hasTypes(CompanyType.MARKETPLACE)
                .isActive();

        // Story Part 2: Create Loggi logistics provider
        final CompanyId loggiId = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withName("Loggi")
                        .withTypes(CompanyType.LOGISTICS_PROVIDER)
                        .build()
        );

        final CompanyEntity loggi = companyJpaRepository.findById(loggiId.value()).orElseThrow();
        assertThatCompany(loggi)
                .hasName("Loggi")
                .hasTypes(CompanyType.LOGISTICS_PROVIDER)
                .isActive();

        // Story Part 3: Create agreement (Shoppe delivers with Loggi)
        final Instant validFrom = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        final Instant initialValidTo = Instant.now().plus(90, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
        
        final AgreementId agreementId = agreementFixture.createAgreement(
                shoppeId.value(),
                CreateAgreementDTOBuilder.aCreateAgreementDTO()
                        .withDestinationCompanyId(loggiId.value())
                        .withType(AgreementType.DELIVERS_WITH)
                        .withValidTo(initialValidTo)
                        .withValidFrom(validFrom)
                        .build()
        );

        // Story Part 4: Verify agreement persisted in database
        final CompanyEntity shoppeWithAgreement = entityManager.createQuery(
                "SELECT c FROM CompanyEntity c LEFT JOIN FETCH c.agreements WHERE c.id = :id",
                CompanyEntity.class
        ).setParameter("id", shoppeId.value()).getSingleResult();
        
        assertThatCompany(shoppeWithAgreement).hasName("Shoppe");
        assertThat(shoppeWithAgreement.getAgreements()).hasSize(1);
        
        final AgreementEntity agreement = shoppeWithAgreement.getAgreements().iterator().next();
        assertThatAgreement(agreement)
                .hasId(agreementId.value())
                .hasFrom(shoppeId.value())
                .hasTo(loggiId.value())
                .hasRelationType("DELIVERS_WITH")
                .hasValidFrom(validFrom)
                .hasValidTo(initialValidTo);

        // Story Part 5: Update agreement validity
        final Instant validTo = Instant.now().plus(30, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
        
        agreementFixture.updateAgreement(
                shoppeId.value(),
                agreementId.value(),
                new UpdateAgreementDTO(validTo, null)
        );

        // Story Part 6: Verify update in database
        final CompanyEntity shoppeWithUpdatedAgreement = entityManager.createQuery(
                "SELECT c FROM CompanyEntity c LEFT JOIN FETCH c.agreements WHERE c.id = :id",
                CompanyEntity.class
        ).setParameter("id", shoppeId.value()).getSingleResult();
        
        assertThat(shoppeWithUpdatedAgreement.getAgreements()).hasSize(1);

        final AgreementEntity updatedAgreement = shoppeWithUpdatedAgreement.getAgreements().iterator().next();
        assertThatAgreement(updatedAgreement)
                .hasId(agreementId.value())
                .hasValidFrom(validFrom)
                .hasValidTo(validTo);

        // Story Part 7: Remove agreement
        agreementFixture.removeAgreement(shoppeId.value(), agreementId.value());

        // Story Part 8: Verify deletion (agreement gone, companies still exist)
        final CompanyEntity shoppeAfterRemoval = entityManager.createQuery(
                "SELECT c FROM CompanyEntity c LEFT JOIN FETCH c.agreements WHERE c.id = :id",
                CompanyEntity.class
        ).setParameter("id", shoppeId.value()).getSingleResult();
        
        assertThatCompany(shoppeAfterRemoval)
                .hasName("Shoppe")
                .isActive();

        assertThat(shoppeAfterRemoval.getAgreements()).isEmpty();
    }
}
```

### Key Points

- ✅ Extends `AbstractIntegrationTest` (@SpringBootTest + Testcontainers)
- ✅ Single test = complete business narrative
- ✅ Clear story parts with comments
- ✅ Realistic entity names (Shoppe, Loggi, not "Company A/B")
- ✅ Verify persistence after each operation
- ✅ Use EntityManager or JPA repositories for database validation
- ✅ Test cleanup operations (deletion doesn't cascade incorrectly)
- ✅ All variables declared `final`
- ✅ Location: `src/test/java/br/com/logistics/tms/integration/`

---

## Pattern 6: Unit Test Structure

### When to Use

- Testing use case business logic
- Need fast, isolated tests (NO database, NO Spring)
- Want to validate domain rules and state transitions
- Testing exception scenarios

### Structure

```java
package br.com.logistics.tms.{module}.application.usecases;

import br.com.logistics.tms.{module}.application.repositories.Fake{Entity}Repository;
import br.com.logistics.tms.{module}.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static br.com.logistics.tms.assertions.domain.{module}.{Entity}Assert.assertThat{Entity};
import static org.assertj.core.api.Assertions.*;

class {UseCase}Test {

    private Fake{Entity}Repository {entity}Repository;
    private {UseCase} useCase;

    @BeforeEach
    void setUp() {
        {entity}Repository = new Fake{Entity}Repository();
        useCase = new {UseCase}({entity}Repository);
    }

    @Test
    @DisplayName("Should execute happy path operation")
    void shouldExecuteHappyPathOperation() {
        // Given: Setup test data
        final {Entity} entity = {Entity}Builder.an{Entity}()
            .withField1(...)
            .withField2(...)
            .build();
        {entity}Repository.create(entity);

        // When: Execute use case
        final {UseCase}.Input input = new {UseCase}.Input(...);
        final {UseCase}.Output output = useCase.execute(input);

        // Then: Verify results
        assertThat(output.field1()).isEqualTo(...);
        assertThat(output.field2()).isEqualTo(...);
        
        final {Entity} updated = {entity}Repository.get{Entity}ById(entity.get{Entity}Id()).orElseThrow();
        assertThat{Entity}(updated)
            .hasField1(...)
            .hasField2(...);
    }

    @Test
    @DisplayName("Should throw exception when validation fails")
    void shouldThrowExceptionWhenValidationFails() {
        final {UseCase}.Input input = new {UseCase}.Input(...);

        assertThatThrownBy(() -> useCase.execute(input))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Expected error message");
    }
}
```

### Real Example: CreateAgreementUseCaseTest

```java
class CreateAgreementUseCaseTest {

    private FakeCompanyRepository companyRepository;
    private CreateAgreementUseCase useCase;

    @BeforeEach
    void setUp() {
        companyRepository = new FakeCompanyRepository();
        useCase = new CreateAgreementUseCase(companyRepository);
    }

    @Test
    @DisplayName("Should create agreement between two companies")
    void shouldCreateAgreementBetweenTwoCompanies() {
        final Company sourceCompany = Company.createCompany(
                "Shoppe Logistics",
                "12345678901234",
                Set.of(CompanyType.SELLER),
                Map.of("region", "SP")
        );
        final Company destinationCompany = Company.createCompany(
                "Loggi Transportes",
                "98765432109876",
                Set.of(CompanyType.LOGISTICS_PROVIDER),
                Map.of("region", "SP")
        );
        companyRepository.create(sourceCompany);
        companyRepository.create(destinationCompany);

        final Instant validFrom = Instant.now();
        final Instant validTo = validFrom.plus(365, ChronoUnit.DAYS);
        final Map<String, Object> configuration = new HashMap<>();
        configuration.put("discount", 15.0);
        final Set<AgreementCondition> conditions = Set.of(
                new AgreementCondition(
                        AgreementConditionId.unique(),
                        AgreementConditionType.DISCOUNT_PERCENTAGE,
                        Conditions.with(Map.of("value", 15.0))
                )
        );

        final CreateAgreementUseCase.Input input = new CreateAgreementUseCase.Input(
                sourceCompany.getCompanyId().value(),
                destinationCompany.getCompanyId().value(),
                AgreementType.DELIVERS_WITH,
                configuration,
                conditions,
                validFrom,
                validTo
        );

        final CreateAgreementUseCase.Output output = useCase.execute(input);

        assertThat(output.agreementId()).isNotNull();
        assertThat(output.sourceCompanyId()).isEqualTo(sourceCompany.getCompanyId().value());
        assertThat(output.destinationCompanyId()).isEqualTo(destinationCompany.getCompanyId().value());
        assertThat(output.agreementType()).isEqualTo("DELIVERS_WITH");

        final Company updatedCompany = companyRepository.getCompanyById(sourceCompany.getCompanyId())
                .orElseThrow();
        assertThat(updatedCompany.getAgreements()).hasSize(1);
        
        final Agreement agreement = updatedCompany.getAgreements().iterator().next();
        assertThatAgreement(agreement)
                .hasFrom(sourceCompany.getCompanyId())
                .hasTo(destinationCompany.getCompanyId())
                .hasType(AgreementType.DELIVERS_WITH)
                .hasConditionsCount(1);
    }

    @Test
    @DisplayName("Should throw exception when source company not found")
    void shouldThrowExceptionWhenSourceCompanyNotFound() {
        final UUID sourceCompanyId = UUID.randomUUID();
        final UUID destCompanyId = UUID.randomUUID();

        final CreateAgreementUseCase.Input input = new CreateAgreementUseCase.Input(
                sourceCompanyId,
                destCompanyId,
                AgreementType.DELIVERS_WITH,
                Map.of(),
                Set.of(),
                Instant.now(),
                null
        );

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(CompanyNotFoundException.class)
                .hasMessage("Company not found: " + sourceCompanyId);
    }
}
```

### Key Points

- ✅ NO Spring annotations — pure JUnit tests
- ✅ Use Fake repositories (not Mockito)
- ✅ Use Builders for test data setup
- ✅ Use Custom Assertions for verification
- ✅ `@BeforeEach` initializes fake + use case
- ✅ `@DisplayName` provides readable test descriptions
- ✅ Given-When-Then structure (comments optional but helpful)
- ✅ All variables declared `final`
- ✅ Location: `src/test/java/br/com/logistics/tms/{module}/application/usecases/`

---

## Complete Testing Flow for New Entities

When creating tests for a NEW entity, follow this sequence:

### Step 1: Create Custom Assertions

```bash
src/test/java/br/com/logistics/tms/assertions/domain/{module}/{Entity}Assert.java
```

- Extend `AbstractAssert<{Entity}Assert, {Entity}>`
- Add domain-specific assertion methods
- Focus on entity behavior (isActive, overlaps, etc.)

### Step 2: Create Test Data Builder

```bash
src/test/java/br/com/logistics/tms/builders/domain/{module}/{Entity}Builder.java
```

- Static factory method `an{Entity}()`
- Sensible defaults for all fields
- Fluent `with{Field}()` methods
- `build()` calls domain factory method

### Step 3: Create Fake Repository

```bash
src/test/java/br/com/logistics/tms/{module}/application/repositories/Fake{Entity}Repository.java
```

- Implement repository interface
- Use `Map<{Entity}Id, {Entity}>` for storage
- Add test helper methods (clear, count, existsById)

### Step 4: Write Use Case Unit Tests

```bash
src/test/java/br/com/logistics/tms/{module}/application/usecases/{UseCase}Test.java
```

- Use Fake repository
- Use Builder for test data
- Use Custom assertions for verification
- Cover happy path + exceptions

### Step 5: Create Integration Fixture

```bash
src/test/java/br/com/logistics/tms/integration/fixtures/{Entity}IntegrationFixture.java
```

- Encapsulate MockMvc REST calls
- Return extracted IDs
- Include basic JSON validation

### Step 6: Write Story-Driven Integration Tests

```bash
src/test/java/br/com/logistics/tms/integration/{Entity}{Feature}IT.java
```

- Extend `AbstractIntegrationTest`
- Create complete business flow
- Verify database persistence
- Test cascade operations

---

## Anti-Patterns (DO NOT DO)

### ❌ Don't: Use Mockito for Repositories in Unit Tests

```java
// ❌ WRONG
@Mock
private CompanyRepository companyRepository;

@Test
void testSomething() {
    when(companyRepository.findById(any())).thenReturn(Optional.of(company));
    // ... tons of mock configuration boilerplate
}
```

**Why:** Brittle, verbose, couples tests to implementation details.

**Instead:** Use Fake repositories with real behavior.

### ❌ Don't: Inline Test Data Creation

```java
// ❌ WRONG
final Agreement agreement = Agreement.createAgreement(
    CompanyId.with(UUID.randomUUID()),
    CompanyId.with(UUID.randomUUID()),
    AgreementType.DELIVERS_WITH,
    Map.of("key1", "value1", "key2", "value2", "key3", "value3"),
    Set.of(/* 5 conditions with complex setup */),
    Instant.now(),
    Instant.now().plus(365, ChronoUnit.DAYS)
);
```

**Why:** Unreadable, repetitive, hard to maintain.

**Instead:** Use builders with defaults and customize only what matters.

### ❌ Don't: Use Plain AssertJ for Domain Objects

```java
// ❌ WRONG
assertThat(agreement.from().value()).isEqualTo(sourceCompanyId.value());
assertThat(agreement.to().value()).isEqualTo(destCompanyId.value());
assertThat(agreement.type()).isEqualTo(AgreementType.DELIVERS_WITH);
assertThat(agreement.isActive()).isTrue();
```

**Why:** Verbose, not domain-aware, poor failure messages.

**Instead:** Use custom assertions for fluent, readable validation.

### ❌ Don't: Mix Multiple Scenarios in One Integration Test

```java
// ❌ WRONG
@Test
void testEverything() {
    // Create scenario 1
    // Update scenario 2
    // Delete scenario 3
    // Edge case scenario 4
    // ... hundreds of lines
}
```

**Why:** Hard to debug, unclear what failed, brittle.

**Instead:** One test = one business story. Keep it focused.

### ❌ Don't: Skip Database Verification in Integration Tests

```java
// ❌ WRONG
@Test
void shouldCreateAgreement() {
    agreementFixture.createAgreement(...);
    // ❌ Test ends here — no verification!
}
```

**Why:** Test passes even if persistence fails silently.

**Instead:** Always verify database state after operations.

### ❌ Don't: Use Spring Context in Use Case Tests

```java
// ❌ WRONG
@SpringBootTest
class CreateAgreementUseCaseTest {
    @Autowired
    private CompanyRepository companyRepository;
    
    @Test
    void testSomething() { ... }
}
```

**Why:** Slow (Spring bootstrap), couples to infrastructure, not a unit test.

**Instead:** Use fake repositories, keep use case tests pure and fast.

---

## Related Files and Skills

**Reference Implementations:**
- `src/test/java/br/com/logistics/tms/assertions/domain/company/AgreementAssert.java`
- `src/test/java/br/com/logistics/tms/builders/domain/company/AgreementBuilder.java`
- `src/test/java/br/com/logistics/tms/company/application/repositories/FakeCompanyRepository.java`
- `src/test/java/br/com/logistics/tms/integration/fixtures/AgreementIntegrationFixture.java`
- `src/test/java/br/com/logistics/tms/integration/CompanyAgreementIT.java`
- `src/test/java/br/com/logistics/tms/company/application/usecases/CreateAgreementUseCaseTest.java`

**Related Skills:**
- `.squad/skills/fake-repository-pattern/SKILL.md` — Detailed fake repository patterns
- `.squad/skills/test-data-builder-pattern/SKILL.md` — Builder pattern best practices
- `.squad/skills/immutable-aggregate-update/SKILL.md` — Domain immutability patterns
- `.squad/skills/e2e-testing-tms/SKILL.md` — HTTP E2E testing with .http files

**Documentation:**
- `doc/ai/TEST_STRUCTURE.md` — Overall test architecture and guidelines
- `doc/ai/INTEGRATION_TESTS.md` — Integration test patterns and Testcontainers setup
- `doc/ai/prompts/test-data-builders.md` — Test data builder creation guide
- `doc/ai/prompts/fake-repositories.md` — Fake repository creation guide

---

## When to Extract New Test Infrastructure

Extract NEW test infrastructure components when:

1. **Custom Assertion:** Entity has 5+ common assertion patterns across tests
2. **Test Builder:** Creating entity with 3+ parameters in 3+ different tests
3. **Fake Repository:** Unit testing a use case that depends on a new repository
4. **Integration Fixture:** Making the same REST call pattern in 3+ integration tests

**Process:**
1. Extract the component following the template above
2. Refactor existing tests to use the new component
3. Document in this skill if the pattern is reusable across modules

---

## Summary

TMS test infrastructure is a **layered system** where each layer provides tools for its testing level:

| Layer | Tools | Purpose |
|-------|-------|---------|
| Domain Tests | Assertions + Builders | Fast, isolated domain logic testing |
| Use Case Tests | Assertions + Builders + Fake Repos | Fast, isolated use case testing without database |
| Integration Tests | Assertions + Fixtures + Real Infra | Full-stack validation with database and REST |

**Key Principles:**
- ✅ Consistent patterns across all entities
- ✅ Readable, fluent test code
- ✅ Sensible defaults reduce boilerplate
- ✅ Isolation at the right level (unit vs integration)
- ✅ Story-driven integration tests document behavior
- ✅ All test code follows TMS coding standards (all finals, immutability, value objects)

This infrastructure makes creating tests for NEW entities **fast, consistent, and maintainable**.
