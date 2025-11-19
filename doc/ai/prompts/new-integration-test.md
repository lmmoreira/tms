# Integration Test Guide (Prompt Template)

**Use this template when creating a new integration test.**

---

## Context

You are creating a new integration test for the TMS project. Integration tests validate complete business flows across multiple REST endpoints and modules.

**Read first:** `/doc/ai/INTEGRATION_TESTS.md`

---

## Template

```java
package br.com.logistics.tms.integration;

import br.com.logistics.tms.AbstractIntegrationTest;
import br.com.logistics.tms.integration.fixtures.CompanyIntegrationFixture;
import br.com.logistics.tms.integration.fixtures.ShipmentOrderIntegrationFixture;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static br.com.logistics.tms.integration.assertions.CompanyEntityAssert.assertThatCompany;

@AutoConfigureMockMvc
class {Flow}IntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // Inject repositories needed for assertions
    @Autowired
    private CompanyJpaRepository companyJpaRepository;
    
    @Autowired
    private CompanyOutboxJpaRepository companyOutboxJpaRepository;
    
    @Autowired
    private ShipmentOrderCompanyJpaRepository shipmentOrderCompanyJpaRepository;
    
    // Fixtures (manually instantiated)
    private CompanyIntegrationFixture companyFixture;
    private ShipmentOrderIntegrationFixture shipmentOrderFixture;

    @BeforeEach
    void setUp() {
        // Instantiate fixtures with required dependencies
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
    void shouldDoSomething() throws Exception {
        // 1. Create entities via fixtures
        final CompanyId companyId = companyFixture.createCompany(
                CreateCompanyDTODataBuilder.aCreateCompanyDTO()
                        .withName("Test Company")
                        .build()
        );
        
        // 2. Assert using custom assertions
        final CompanyEntity company = companyJpaRepository.findById(companyId.value()).orElseThrow();
        assertThatCompany(company)
                .hasName("Test Company");
        
        // 3. Assert cross-module synchronization
        final ShipmentOrderCompanyEntity syncedCompany = shipmentOrderCompanyJpaRepository
                .findById(companyId.value())
                .orElseThrow();
        assertThatShipmentOrderCompany(syncedCompany)
                .hasNameInData("Test Company");
    }
}
```

---

## Checklist

Before creating the test:

- [ ] Read `/doc/ai/INTEGRATION_TESTS.md`
- [ ] Identify the business flow to test
- [ ] Determine which fixtures are needed
- [ ] Identify which repositories to inject
- [ ] Plan assertions (use custom assertions)
- [ ] Consider cross-module synchronization

When writing the test:

- [ ] Extends `AbstractIntegrationTest`
- [ ] Has `@AutoConfigureMockMvc`
- [ ] Injects `MockMvc` and `ObjectMapper`
- [ ] Injects required repositories
- [ ] Creates fixtures in `@BeforeEach` (NOT `@Autowired`)
- [ ] Uses fixtures for REST operations
- [ ] Uses custom assertions (`assertThatCompany()`)
- [ ] Uses test data builders (`CreateCompanyDTODataBuilder`)
- [ ] Tests outbox publishing
- [ ] Tests cross-module synchronization
- [ ] Has descriptive test method name
- [ ] Tests complete business flow (not individual operations)

---

## Common Patterns

### Create → Assert → Sync

```java
final CompanyId id = companyFixture.createCompany(dto);

assertThatCompany(companyJpaRepository.findById(id.value()).orElseThrow())
    .hasName("Test");

assertThatShipmentOrderCompany(shipmentOrderCompanyJpaRepository.findById(id.value()).orElseThrow())
    .hasNameInData("Test");
```

### Update → Assert → Sync

```java
companyFixture.updateCompany(id, updateDTO);

assertThatCompany(companyJpaRepository.findById(id.value()).orElseThrow())
    .hasName("Updated");
```

### Multiple Operations → Counter

```java
shipmentOrderFixture.createShipmentOrder(...);
shipmentOrderFixture.createShipmentOrder(...);
shipmentOrderFixture.createShipmentOrder(...);

assertThatCompany(companyJpaRepository.findById(id.value()).orElseThrow())
    .hasShipmentOrderCount(3);
```

---

## Need New Fixture?

If you need a fixture for a new module:

1. Create in `src/test/java/br/com/logistics/tms/integration/fixtures/`
2. Follow pattern from `CompanyIntegrationFixture`
3. Include methods:
   - `create{Entity}(DTO dto)` - Creates + waits for sync
   - `create{Entity}WithoutWaiting(DTO dto)` - Just creates
4. Return typed domain IDs (not raw UUIDs)
5. Use Awaitility for waiting

---

## Need New Custom Assertion?

If you need a custom assertion for a new entity:

1. Create in `src/test/java/br/com/logistics/tms/integration/assertions/`
2. Extend `AbstractAssert`
3. Follow pattern from `CompanyEntityAssert`
4. Provide fluent methods: `has{Field}()`, `is{State}()`
5. Use `.as()` for better error messages

---

## Examples

See: `src/test/java/br/com/logistics/tms/integration/CompanyShipmentOrderIntegrationTest.java`

---

## Key Principles

1. **One test validates entire business flow** - Not individual operations
2. **Use fixtures** - Never write REST boilerplate in tests
3. **Use custom assertions** - Fluent and readable
4. **Test event-driven flows** - Validate outbox + listeners
5. **Keep fixtures stateless** - Fresh instances per test
6. **Broad > Narrow** - One test = one user journey
