# Integration Tests

**Broad integration tests that validate complete business flows across multiple REST endpoints and modules.**

---

## Quick Links

- 📖 **[Complete Documentation](/doc/ai/INTEGRATION_TESTS.md)** - Full integration test guide
- 📝 **[Quick Template](/doc/ai/prompts/new-integration-test.md)** - Template for new tests
- 📚 **[Test Structure](/doc/ai/TEST_STRUCTURE.md)** - Test organization

---

## What's Here

```
integration/
├── CompanyShipmentOrderIntegrationTest.java  # Example: Complete flow test
├── fixtures/                                  # Test helpers
│   ├── CompanyIntegrationFixture.java        # Company REST + sync helpers
│   └── ShipmentOrderIntegrationFixture.java  # ShipmentOrder REST + sync helpers
├── assertions/                                # Custom fluent assertions
│   ├── CompanyEntityAssert.java
│   ├── ShipmentOrderEntityAssert.java
│   ├── ShipmentOrderCompanyEntityAssert.java
│   └── OutboxAssert.java
└── data/                                      # DTO builders
    ├── CreateCompanyDTODataBuilder.java
    ├── UpdateCompanyDTODataBuilder.java
    └── CreateShipmentOrderDTODataBuilder.java
```

---

## Philosophy

### ✅ Prefer Broad Tests

```java
// ✅ GOOD - One test validates entire user journey
@Test
void shouldCreateCompanyUpdateItAndCreateShipmentOrderIncrementingCounter() {
    // Create company → Assert → Assert sync
    // Update company → Assert → Assert sync  
    // Create order → Assert → Assert counter
}
```

### ❌ Not Many Narrow Tests

```java
// ❌ AVOID - Too granular for integration tests
@Test void shouldCreateCompany() { }
@Test void shouldSyncCompany() { }
@Test void shouldUpdateCompany() { }
```

---

## Quick Start

### 1. Set Up Test

```java
@AutoConfigureMockMvc
class MyIntegrationTest extends AbstractIntegrationTest {
    
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CompanyJpaRepository companyJpaRepository;
    @Autowired private CompanyOutboxJpaRepository companyOutboxJpaRepository;
    @Autowired private ShipmentOrderCompanyJpaRepository shipmentOrderCompanyJpaRepository;
    
    private CompanyIntegrationFixture companyFixture;
    
    @BeforeEach
    void setUp() {
        companyFixture = new CompanyIntegrationFixture(
                mockMvc, objectMapper, 
                companyOutboxJpaRepository, shipmentOrderCompanyJpaRepository
        );
    }
}
```

**Note:** Fixtures are **NOT** `@Component` - manually instantiated in `@BeforeEach`.

### 2. Use Fixtures

```java
@Test
void myTest() throws Exception {
    // Fixtures handle REST + outbox + sync automatically
    final CompanyId id = companyFixture.createCompany(
            CreateCompanyDTODataBuilder.aCreateCompanyDTO()
                    .withName("Test Company")
                    .build()
    );
    
    // Custom assertions for readability
    final CompanyEntity company = companyJpaRepository.findById(id.value()).orElseThrow();
    assertThatCompany(company)
            .hasName("Test Company")
            .hasTypes(CompanyType.SELLER);
}
```

### 3. Test Cross-Module Sync

```java
// Assert company synchronized to shipmentorder schema
final ShipmentOrderCompanyEntity syncedCompany = 
        shipmentOrderCompanyJpaRepository.findById(id.value()).orElseThrow();

assertThatShipmentOrderCompany(syncedCompany)
        .hasNameInData("Test Company");
```

---

## What Fixtures Do

Fixtures **automatically**:
- ✅ Make REST calls via MockMvc
- ✅ Wait for outbox status = PUBLISHED
- ✅ Wait for cross-module synchronization
- ✅ Return typed domain IDs (not raw UUIDs)

**Example:**
```java
// This ONE line does:
// 1. POST /companies
// 2. Wait for outbox published
// 3. Wait for sync to shipmentorder schema
final CompanyId id = companyFixture.createCompany(dto);
```

---

## Custom Assertions

Fluent, domain-specific assertions:

```java
// Company assertions
assertThatCompany(entity)
    .hasName("Test")
    .hasCnpj("12345678901234")
    .hasTypes(CompanyType.SELLER, CompanyType.MARKETPLACE)
    .hasShipmentOrderCount(3);

// ShipmentOrder assertions
assertThatShipmentOrder(entity)
    .hasCompanyId(companyId)
    .hasExternalId("EXT-001")
    .isNotArchived();

// ShipmentOrderCompany (synced data) assertions
assertThatShipmentOrderCompany(entity)
    .hasNameInData("Updated Company")
    .hasDataEntry("types", List.of("SELLER"));

// Outbox assertions (for edge cases)
assertThatOutbox(entity)
    .isPublished()
    .hasEventType("CompanyCreated");
```

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

assertThatShipmentOrderCompany(shipmentOrderCompanyJpaRepository.findById(id.value()).orElseThrow())
    .hasNameInData("Updated");
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

## Complete Example

See: `CompanyShipmentOrderIntegrationTest.java`

This test validates:
1. Create company
2. Assert in company schema
3. Assert synchronized to shipmentorder schema
4. Create shipper company
5. Update first company
6. Assert update in both schemas
7. Create shipment order
8. Assert order created
9. Assert company counter incremented

All in **ONE test** that reads like a user story!

---

## Key Principles

1. **One broad test per flow** - Not many narrow tests
2. **Use fixtures** - Never write REST boilerplate
3. **Use custom assertions** - Fluent and readable
4. **Test event-driven flows** - Validate outbox + listeners
5. **Keep fixtures stateless** - Fresh instances per test
6. **Broad > Narrow** - One test = one user journey

---

## Need Help?

- 📖 Read: `/doc/ai/INTEGRATION_TESTS.md`
- 📝 Use template: `/doc/ai/prompts/new-integration-test.md`
- 🔍 See example: `CompanyShipmentOrderIntegrationTest.java`
- 📚 Test structure: `/doc/ai/TEST_STRUCTURE.md`
