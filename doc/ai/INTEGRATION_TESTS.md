# Integration Test Documentation

**Complete guide for creating and maintaining integration tests in the TMS project.**

---

## Table of Contents

1. [Overview](#overview)
2. [Philosophy](#philosophy)
3. [Structure](#structure)
4. [Test Fixtures](#test-fixtures)
5. [Custom Assertions](#custom-assertions)
6. [Test Data Builders](#test-data-builders)
7. [Writing Integration Tests](#writing-integration-tests)
8. [Best Practices](#best-practices)
9. [Common Patterns](#common-patterns)
10. [Troubleshooting](#troubleshooting)

---

## Overview

Integration tests in TMS validate **complete business flows** across multiple endpoints and modules, ensuring the event-driven architecture works correctly end-to-end.

### Key Characteristics

- ✅ **Broad scope** - Multiple REST endpoints in a single test
- ✅ **Event-driven validation** - Verifies outbox publishing and listener synchronization
- ✅ **Cross-module** - Tests communication between Company and ShipmentOrder modules
- ✅ **Database assertions** - Validates data in multiple schemas
- ✅ **Real infrastructure** - Uses Testcontainers for PostgreSQL and RabbitMQ

---

## Philosophy

### Story-Driven vs Layer-Based Testing

**TMS prefers STORY-DRIVEN integration tests.**

```java
// ✅ PREFERRED - Story-driven (business flow perspective)
@Test
void shouldCreateCompanyUpdateItAndCreateShipmentOrderIncrementingCounter() {
    // Complete user story from start to finish:
    // 1. Company registers in the platform
    // 2. Company updates their information
    // 3. Company receives a shipment order
    // 4. System tracks order count for the company
    
    // Each step validates domain behavior + persistence + events + cross-module sync
}

// ❌ AVOID - Layer-based (technical perspective)
@Test void shouldCreateCompany() { }
@Test void shouldSyncCompanyToShipmentOrderSchema() { }
@Test void shouldUpdateCompany() { }
@Test void shouldIncrementCounter() { }
```

### Why Story-Driven?

1. **Business context** - Tests describe what the system does, not how
2. **End-to-end validation** - Ensures event chains work correctly
3. **Realistic scenarios** - Mirrors actual user workflows
4. **Fewer setups** - One test validates multiple operations
5. **Single source of truth** - One test tells the complete story

### Key Principle

> **NO LAYER TESTS WITHOUT BUSINESS CONTEXT**

Integration tests should NOT test individual layers in isolation (repository, controller, listener). They should test complete business flows that exercise all layers together.

```java
// ❌ WRONG - Layer test without context
@Test
void repositoryShouldSaveCompany() {
    // Tests repository in isolation
}

// ✅ RIGHT - Business flow that uses repository
@Test
void shouldRegisterNewCompanyInPlatform() {
    // POST /companies → validates saved → validates events → validates sync
}
```

---

## Structure

### Directory Organization

```
src/test/java/br/com/logistics/tms/
├── AbstractIntegrationTest.java                 # Base class with Testcontainers setup
├── TestContainersManager.java                   # Singleton managing shared containers
└── integration/
    ├── CompanyShipmentOrderIT.java              # Integration tests (*IT.java suffix)
    ├── fixtures/                                # Test helpers
    │   ├── CompanyIntegrationFixture.java
    │   └── ShipmentOrderIntegrationFixture.java
    ├── assertions/                              # Custom AssertJ assertions
    │   ├── CompanyEntityAssert.java
    │   ├── ShipmentOrderEntityAssert.java
    │   ├── ShipmentOrderCompanyEntityAssert.java
    │   └── OutboxAssert.java
    └── data/                                    # DTO builders
        ├── CreateCompanyDTOBuilder.java
        ├── UpdateCompanyDTOBuilder.java
        ├── CreateShipmentOrderDTOBuilder.java
        └── CnpjGenerator.java
```

### Why This Structure?

- **`fixtures/`** - Encapsulates REST calls + event waiting logic
- **`assertions/`** - Provides fluent, domain-specific assertions
- **`data/`** - Builds DTOs with sensible defaults + test utilities

---

## Test Fixtures

### Purpose

Fixtures handle the **boring repetitive stuff** so tests can focus on business validation:
- Making REST calls via MockMvc
- Parsing responses and extracting IDs
- Waiting for outbox events to be published
- Waiting for cross-module synchronization
- Returning typed domain IDs (not raw UUIDs)

**Key Principle:** One fixture method = one complete business operation

### Fixture Pattern

```java
public CompanyId createCompany(final CreateCompanyDTO dto) throws Exception {
    // 1. REST call
    final String responseJson = mockMvc.perform(post("/companies")...)
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();

    // 2. Extract ID
    final CompanyId companyId = new CompanyId(parseResponse(responseJson).companyId());

    // 3. Wait for outbox published
    await().atMost(30, TimeUnit.SECONDS)
        .until(() -> outboxRepository
            .findFirstByAggregateIdOrderByCreatedAtDesc(companyId.value())
            .map(outbox -> outbox.getStatus() == OutboxStatus.PUBLISHED)
            .orElse(false)
        );

    // 4. Wait for cross-module sync
    await().atMost(30, TimeUnit.SECONDS)
        .until(() -> syncRepository.existsById(companyId.value()));

    return companyId;
}
```

### Why Fixtures Extract Setup Logic

Without fixtures, integration tests become verbose and hard to read:

```java
// ❌ WITHOUT FIXTURES - Repetitive, hard to read
@Test
void myTest() throws Exception {
    final String json = objectMapper.writeValueAsString(dto);
    final String response = mockMvc.perform(post("/companies")
        .contentType("application/json")
        .content(json))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    
    final UUID id = objectMapper.readValue(response, ResponseDTO.class).companyId();
    
    await().atMost(30, TimeUnit.SECONDS)
        .until(() -> outboxRepository
            .findFirstByAggregateIdOrderByCreatedAtDesc(id)
            .map(o -> o.getStatus() == OutboxStatus.PUBLISHED)
            .orElse(false));
    
    await().atMost(30, TimeUnit.SECONDS)
        .until(() -> syncRepository.existsById(id));
    
    // NOW the actual test assertion starts...
}

// ✅ WITH FIXTURES - Clean, focused on business validation
@Test
void myTest() throws Exception {
    final CompanyId id = companyFixture.createCompany(dto);
    
    // Test immediately focuses on business validation
    assertThatCompany(companyJpaRepository.findById(id.value()).orElseThrow())
        .hasName("Test");
}
```

### CompanyIntegrationFixture

**Location:** `src/test/java/br/com/logistics/tms/integration/fixtures/CompanyIntegrationFixture.java`

```java
public class CompanyIntegrationFixture {
    
    // Creates company + waits for outbox published + waits for sync to shipmentorder schema
    public CompanyId createCompany(CreateCompanyDTO dto) throws Exception
    
    // Just creates, doesn't wait (for testing failure scenarios)
    public CompanyId createCompanyWithoutWaiting(CreateCompanyDTO dto) throws Exception
    
    // Updates company + waits for outbox published + waits for sync
    public void updateCompany(CompanyId id, UpdateCompanyDTO dto) throws Exception
    
    // Just updates, doesn't wait
    public void updateCompanyWithoutWaiting(CompanyId id, UpdateCompanyDTO dto) throws Exception
}
```

**What it does:**
1. POST/PUT to REST endpoint via `MockMvc`
2. Assert HTTP status (201 Created, 200 OK)
3. Extract ID from response
4. Wait (with timeout) for outbox status = PUBLISHED
5. Wait (with timeout) for synchronization to other schemas
6. Return typed domain ID

### ShipmentOrderIntegrationFixture

**Location:** `src/test/java/br/com/logistics/tms/integration/fixtures/ShipmentOrderIntegrationFixture.java`

```java
public class ShipmentOrderIntegrationFixture {
    
    // Creates order + waits for outbox published + waits for company counter incremented
    public ShipmentOrderId createShipmentOrder(CreateShipmentOrderDTO dto) throws Exception
    
    // Just creates, doesn't wait
    public ShipmentOrderId createShipmentOrderWithoutWaiting(CreateShipmentOrderDTO dto) throws Exception
}
```

**What it does:**
1. POST to `/shipmentorders` endpoint
2. Wait for outbox published
3. Wait for company counter incremented (via IncrementShipmentOrderListener)
4. Return `ShipmentOrderId`

### Usage in Tests

```java
@BeforeEach
void setUp() {
    companyFixture = new CompanyIntegrationFixture(
            mockMvc,
            objectMapper,
            companyOutboxJpaRepository,
            shipmentOrderCompanyJpaRepository
    );
    
    shipmentOrderFixture = new ShipmentOrderIntegrationFixture(
            mockMvc,
            objectMapper,
            shipmentOrderOutboxJpaRepository,
            companyJpaRepository
    );
}

@Test
void myTest() throws Exception {
    final CompanyId companyId = companyFixture.createCompany(dto);
    final ShipmentOrderId orderId = shipmentOrderFixture.createShipmentOrder(dto);
}
```

**Why NOT `@Component`?**

- ❌ Fixtures are NOT Spring beans
- ❌ They should NOT be in application context
- ✅ Manually instantiated in `@BeforeEach` (via `AbstractIntegrationTest`)
- ✅ Fresh instance per test (no shared state)

**Note:** `AbstractIntegrationTest` automatically creates fixtures in `@BeforeEach` - you don't need to create them manually in most tests

---

## Testcontainers Setup

### TestContainersManager Singleton

All integration tests share a **single** PostgreSQL + RabbitMQ instance via the `TestContainersManager` singleton pattern.

**Key Features:**
- ✅ Single PostgreSQL container for all tests (shared via singleton)
- ✅ Single RabbitMQ container for all tests
- ✅ Flyway migrations run once on container startup
- ✅ Network shared between containers
- ✅ Shutdown hook ensures cleanup on JVM exit
- ✅ Thread-safe via synchronized getInstance()

**Benefits:**
- ⚡ **10x faster** test execution (no container restarts between tests)
- 💰 **Reduced resource usage** (one DB + RabbitMQ instance instead of N)
- 🔄 **Test isolation** via transactional cleanup in `AbstractIntegrationTest`

### AbstractIntegrationTest

Provides common setup for all integration tests:

```java
@SpringBootTest(classes = {TmsApplication.class})
@TestPropertySource(locations = "classpath:env-test")
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {
    
    // Automatically injects MockMvc, ObjectMapper, repositories
    // Automatically creates CompanyIntegrationFixture + ShipmentOrderIntegrationFixture
    // @BeforeEach cleans database + recreates fixtures
}
```

**What you get for free:**
- ✅ Testcontainers configured (shared singleton)
- ✅ MockMvc injected
- ✅ ObjectMapper injected
- ✅ All JPA repositories injected
- ✅ Fixtures automatically created in `@BeforeEach`
- ✅ Database cleaned between tests

---

## Custom Assertions

### Purpose

Provide **fluent, domain-specific assertions** that are easier to read and maintain than raw AssertJ.

### Available Assertions

#### CompanyEntityAssert

```java
import static br.com.logistics.tms.assertions.jpa.CompanyEntityAssert.assertThatCompany;

final CompanyEntity company = companyJpaRepository.findById(id).orElseThrow();

assertThatCompany(company)
    .hasName("Test Company")
    .hasCnpj("12345678901234")
    .hasTypes(CompanyType.SELLER, CompanyType.MARKETPLACE)
    .hasExactlyTypes(CompanyType.SELLER) // Order-independent exact match
    .hasConfigurationEntry("webhook", "http://...")
    .hasShipmentOrderCount(3);
```

#### ShipmentOrderEntityAssert

```java
import static br.com.logistics.tms.assertions.jpa.ShipmentOrderEntityAssert.assertThatShipmentOrder;

assertThatShipmentOrder(order)
    .hasCompanyId(companyId)
    .hasShipperId(shipperId)
    .hasExternalId("EXT-ORDER-001")
    .isNotArchived()
    .wasCreatedAfter(startTime)
    .wasCreatedBefore(endTime);
```

#### ShipmentOrderCompanyEntityAssert

```java
import static br.com.logistics.tms.assertions.jpa.ShipmentOrderCompanyEntityAssert.assertThatShipmentOrderCompany;

assertThatShipmentOrderCompany(entity)
    .hasCompanyId(companyId)
    .hasData()
    .hasNameInData("Updated Company")
    .hasDataEntry("types", List.of("SELLER"))
    .dataContainsKey("cnpj");
```

#### OutboxAssert

```java
import static br.com.logistics.tms.assertions.jpa.OutboxAssert.assertThatOutbox;

assertThatOutbox(outboxEntity)
    .isPublished()
    .hasEventType("CompanyCreated")
    .hasAggregateId(companyId)
    .hasContent()
    .contentContains("Test Company");
```

### Creating New Assertions

**Template:**

```java
package br.com.logistics.tms.assertions.jpa;

import org.assertj.core.api.AbstractAssert;
import static org.assertj.core.api.Assertions.assertThat;

public class {Entity}Assert extends AbstractAssert<{Entity}Assert, {Entity}> {

    private {Entity}Assert(final {Entity} actual) {
        super(actual, {Entity}Assert.class);
    }

    public static {Entity}Assert assertThat{Entity}(final {Entity} actual) {
        return new {Entity}Assert(actual);
    }

    public {Entity}Assert has{Field}(final String expected) {
        isNotNull();
        assertThat(actual.get{Field}())
                .as("{Entity} {field}")
                .isEqualTo(expected);
        return this;
    }
}
```

---

## Test Data Builders

Use existing test data builders from `/builders/dto/`:

```java
CreateCompanyDTOBuilder.aCreateCompanyDTO()
    .withName("Test Company")
    .withCnpj("12345678901234")
    .withTypes(CompanyType.SELLER)
    .withConfigurationEntry("key", "value")
    .build();

UpdateCompanyDTOBuilder.anUpdateCompanyDTO()
    .withName("Updated Company")
    .withTypes(CompanyType.SELLER, CompanyType.MARKETPLACE)
    .build();

CreateShipmentOrderDTOBuilder.aCreateShipmentOrderDTO()
    .withCompanyId(companyId.value())
    .withShipperId(shipperId.value())
    .withExternalId("EXT-ORDER-001")
    .build();
```

**See:** `/doc/ai/prompts/test-data-builders.md` for creating new builders.

---

## Writing Integration Tests

### Basic Template

```java
package br.com.logistics.tms.integration;

import br.com.logistics.tms.AbstractIntegrationTest;
import br.com.logistics.tms.company.domain.CompanyId;
import br.com.logistics.tms.company.infrastructure.jpa.entities.CompanyEntity;
import br.com.logistics.tms.builders.dto.CreateCompanyDTOBuilder;
import org.junit.jupiter.api.Test;

import static br.com.logistics.tms.assertions.jpa.CompanyEntityAssert.assertThatCompany;

class MyIntegrationTest extends AbstractIntegrationTest {

    @Test
    void shouldDoSomething() throws Exception {
        // Fixtures (companyFixture, shipmentOrderFixture) already available from AbstractIntegrationTest
        // All repositories already injected
        
        final CompanyId companyId = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withName("Test")
                        .build()
        );
        
        final CompanyEntity company = companyJpaRepository.findById(companyId.value()).orElseThrow();
        assertThatCompany(company)
                .hasName("Test");
    }
}
```

**Key Points:**
- ✅ Extends `AbstractIntegrationTest` - gets everything for free
- ✅ No `@Autowired` needed - repositories/fixtures already available
- ✅ No `@BeforeEach` needed - AbstractIntegrationTest handles it
- ✅ Use `*IT.java` suffix for Failsafe execution

### Complete Example

See: `src/test/java/br/com/logistics/tms/integration/CompanyShipmentOrderIT.java`

```java
@Test
void shouldCreateAndUpdateCompanyThenCreateShipmentOrderAndIncrementCompanyOrders() throws Exception {
    // Create company
    final CompanyId companyId = companyFixture.createCompany(
            CreateCompanyDTOBuilder.aCreateCompanyDTO()
                    .withName("Test Company")
                    .build()
    );
    
    // Assert company in company schema
    assertThatCompany(companyJpaRepository.findById(companyId.value()).orElseThrow())
            .hasName("Test Company");
    
    // Assert company synchronized to shipmentorder schema
    assertThatShipmentOrderCompany(shipmentOrderCompanyJpaRepository.findById(companyId.value()).orElseThrow())
            .hasNameInData("Test Company");
    
    // Create shipper
    final CompanyId shipperId = companyFixture.createCompany(...);
    
    // Update company
    companyFixture.updateCompany(companyId, updateDTO);
    
    // Assert update synchronized
    assertThatShipmentOrderCompany(shipmentOrderCompanyJpaRepository.findById(companyId.value()).orElseThrow())
            .hasNameInData("Updated Company");
    
    // Create shipment order
    final ShipmentOrderId orderId = shipmentOrderFixture.createShipmentOrder(...);
    
    // Assert order created
    assertThatShipmentOrder(shipmentOrderJpaRepository.findById(orderId.value()).orElseThrow())
            .hasCompanyId(companyId.value());
    
    // Assert company counter incremented
    assertThatCompany(companyJpaRepository.findById(companyId.value()).orElseThrow())
            .hasShipmentOrderCount(1);
}
```

---

## Best Practices

### 1. Story-Driven Tests (One Test = One Business Flow)

✅ **DO:**
```java
@Test
void shouldHandleCompanyLifecycleAndOrderCreation() {
    // Complete business story:
    // Create → Update → Create Order → Validate all steps
    // This tells: "A company registers, updates info, receives an order"
}
```

❌ **DON'T:**
```java
@Test void shouldCreateCompany() { }  // Layer test
@Test void shouldUpdateCompany() { }  // Layer test
@Test void shouldCreateOrder() { }    // Layer test
// These have NO business context
```

### 2. Use Fixtures for Actions

✅ **DO:**
```java
final CompanyId id = companyFixture.createCompany(dto);
```

❌ **DON'T:**
```java
final String json = objectMapper.writeValueAsString(dto);
final String response = mockMvc.perform(post("/companies")...
// ... 20 more lines of boilerplate
```

### 3. Use Custom Assertions

✅ **DO:**
```java
assertThatCompany(company)
    .hasName("Test")
    .hasTypes(SELLER);
```

❌ **DON'T:**
```java
assertThat(company.getName()).isEqualTo("Test");
assertThat(company.getCompanyTypes()).contains(SELLER);
```

### 4. Test Event-Driven Flows

✅ **DO:**
```java
companyFixture.createCompany(dto); // Waits for outbox published

// Assert synchronized to other schema
assertThatShipmentOrderCompany(...)
    .hasNameInData("Test Company");
```

### 5. Use Descriptive Test Names

✅ **DO:**
```java
void shouldIncrementCompanyCounterWhenMultipleOrdersCreated()
void shouldSynchronizeCompanyUpdatesToShipmentOrderSchema()
```

### 6. DTO Field Validation

**Formatted Value Objects:**
```java
// ✅ CORRECT - Use raw values in builders, domain applies formatting
CreateAgreementDTOBuilder.aCreateAgreementDTO()
    .withCnpj("12345678901234")  // Raw CNPJ
    .withConfiguration(Map.of("key", "value"))  // Must not be null/empty
    .build();

// Domain value object applies formatting
// Input: "12345678901234" → Output: "12.345.678/9012-34"
```

**Configuration Field Cannot Be Null/Empty:**
```java
// ❌ This will throw ValidationException
CreateAgreementDTOBuilder.aCreateAgreementDTO()
    .withConfiguration(null)  // Domain rejects null
    .build();

CreateAgreementDTOBuilder.aCreateAgreementDTO()
    .withConfiguration(Map.of())  // Domain rejects empty
    .build();

// ✅ Must have at least one entry
CreateAgreementDTOBuilder.aCreateAgreementDTO()
    .withConfiguration(Map.of("key", "value"))
    .build();
```

**Key Points:**
- ✅ Use raw values in DTOs (e.g., `"12345678901234"` for CNPJ)
- ✅ Domain layer applies formatting during validation
- ✅ Configuration fields validated in value objects (cannot be null/empty)
- ✅ Test validation failures when appropriate

### 7. Environment Setup

**Use Minimal Docker Setup:**
```bash
# ✅ CORRECT - Use make command for minimal setup
make start-tms

# This starts ONLY:
# - PostgreSQL (write + read databases)
# - RabbitMQ (message broker)
# - No OAuth2-Proxy
# - No observability stack

# ❌ AVOID - Full docker-compose for tests
docker-compose up  # Starts unnecessary services
```

**Why Minimal Setup?**
- ⚡ Faster startup (2 services vs 8+)
- 💰 Lower resource usage
- ✅ Tests don't need OAuth or observability
- ✅ Testcontainers handles container management

**See Also:** `.squad/skills/e2e-testing-tms/SKILL.md` for E2E testing patterns

❌ **DON'T:**
```java
void testCompany()
void test1()
```

### 6. Keep Fixtures Stateless

✅ **DO:**
```java
@BeforeEach
void setUp() {
    companyFixture = new CompanyIntegrationFixture(...); // Fresh instance
}
```

❌ **DON'T:**
```java
@Component // ❌ NO! Creates shared state
public class CompanyIntegrationFixture { }
```

---

## Common Patterns

### Pattern 1: Create → Assert → Sync Assert

```java
final CompanyId id = companyFixture.createCompany(dto);

assertThatCompany(companyJpaRepository.findById(id.value()).orElseThrow())
    .hasName("Test");

assertThatShipmentOrderCompany(shipmentOrderCompanyJpaRepository.findById(id.value()).orElseThrow())
    .hasNameInData("Test");
```

### Pattern 2: Update → Assert → Sync Assert

```java
companyFixture.updateCompany(id, updateDTO);

assertThatCompany(companyJpaRepository.findById(id.value()).orElseThrow())
    .hasName("Updated");

assertThatShipmentOrderCompany(shipmentOrderCompanyJpaRepository.findById(id.value()).orElseThrow())
    .hasNameInData("Updated");
```

### Pattern 3: Multiple Operations → Counter Assert

```java
shipmentOrderFixture.createShipmentOrder(...);
shipmentOrderFixture.createShipmentOrder(...);
shipmentOrderFixture.createShipmentOrder(...);

assertThatCompany(companyJpaRepository.findById(id.value()).orElseThrow())
    .hasShipmentOrderCount(3);
```

### Pattern 4: Testing Without Waiting (Edge Cases)

```java
final CompanyId id = companyFixture.createCompanyWithoutWaiting(dto);

// Immediately assert before events processed
assertThatOutbox(companyOutboxJpaRepository.findFirstByAggregateIdOrderByCreatedAtDesc(id.value()).orElseThrow())
    .isNew(); // Status is still NEW, not PUBLISHED
```

---

## Troubleshooting

### Test Hangs/Timeouts

**Problem:** Test waits forever for outbox or synchronization

**Solutions:**
1. Check RabbitMQ container is running
2. Check listeners are not `@Lazy(false)`
3. Increase timeout in fixtures (default 30s)
4. Check database schemas exist (Flyway migrations)

### Flaky Tests

**Problem:** Tests pass/fail randomly

**Solutions:**
1. Ensure `@BeforeEach` creates fresh fixtures
2. Don't use static state
3. Check for hardcoded UUIDs
4. Use Awaitility for async assertions

### Outbox Not Published

**Problem:** `companyOutboxJpaRepository` returns empty

**Solutions:**
1. Check domain events are placed in aggregate
2. Check repository saves outbox entities
3. Check outbox publisher is running
4. Check transaction boundaries

### Cross-Module Sync Not Working

**Problem:** Company created but not in shipmentorder schema

**Solutions:**
1. Check listener queue names match
2. Check RabbitMQ definitions.json
3. Check listener is `@Lazy(false)`
4. Check for exceptions in listener

---

## Testing Checklist

When creating a new integration test:

- [ ] Extends `AbstractIntegrationTest`
- [ ] Has `@AutoConfigureMockMvc`
- [ ] Injects `MockMvc` and `ObjectMapper`
- [ ] Injects required repositories
- [ ] Creates fixtures in `@BeforeEach`
- [ ] Uses fixtures for REST operations
- [ ] Uses custom assertions
- [ ] Uses test data builders
- [ ] Tests cross-module synchronization
- [ ] Tests outbox publishing
- [ ] Tests business logic end-to-end
- [ ] Has descriptive test method name

---

## Summary

### Key Takeaways

1. **Story-driven tests** - One test = one business flow, NOT layer tests
2. **Business context required** - Tests should describe WHAT the system does for users
3. **Use fixtures** - Encapsulate REST + waiting logic so tests focus on validation
4. **Use custom assertions** - Fluent, readable, maintainable
5. **Test event-driven architecture** - Validate outbox + listeners + cross-module sync
6. **Keep fixtures stateless** - Fresh instances per test
7. **Single test tells complete story** - From user action to final state

### Quick Start

```java
class MyIT extends AbstractIntegrationTest {
    
    @Test
    void shouldDoSomething() throws Exception {
        // companyFixture already available from AbstractIntegrationTest
        // companyJpaRepository already injected
        
        final CompanyId id = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO().build()
        );
        
        assertThatCompany(companyJpaRepository.findById(id.value()).orElseThrow())
                .hasName("Default Company");
    }
}
```

---

## See Also

- `/doc/ai/TEST_STRUCTURE.md` - Overall test organization
- `/doc/ai/prompts/test-data-builders.md` - Creating test builders
- `/doc/ai/ARCHITECTURE.md` - Event-driven architecture
- `AbstractIntegrationTest.java` - Base class with Testcontainers
- `TestContainersManager.java` - Singleton container management
- Example: `CompanyShipmentOrderIT.java`
