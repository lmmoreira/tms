# Switch — History

## Core Context
- **Project:** TMS (Transportation Management System)
- **Owner:** Leonardo Moreira
- **Tech Stack:** Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ
- **Architecture:** Modular Monolith, DDD, Hexagonal, CQRS, Event-Driven
- **Focus:** Agreement domain structure review

## Learnings

### 2026-02-24: Phase 2 Domain Layer Implementation Complete

**Implemented Tasks:**
- 2.1: Fixed Company.addAgreement() — Now returns new Company instance, places AgreementAdded event
- 2.2: Fixed Company.removeAgreement() — Now returns new Company instance, places AgreementRemoved event
- 2.3: Expanded AgreementConditionType enum — Added DISCOUNT_PERCENTAGE and DELIVERY_SLA_DAYS
- 2.4: Added Agreement.createAgreement() factory method — Validates no self-reference
- 2.5: Added Agreement.updateValidTo() method — Returns new instance, validates date range
- 2.6: Added Agreement.updateConditions() method — Returns new instance, validates non-empty
- Added Company.updateAgreement() method — Returns new instance, validates no overlaps, places AgreementUpdated event
- Created three domain events: AgreementAdded, AgreementRemoved, AgreementUpdated

**Key Patterns Applied:**
- All update methods return NEW Company/Agreement instances (immutability)
- Domain events placed in aggregate methods (Company.addAgreement, removeAgreement, updateAgreement)
- Added overlap detection via Agreement.overlapsWith() method
- Self-reference validation in Agreement.createAgreement()
- All variables declared final
- Followed existing Company.updateName() pattern for consistency

**Architecture Decisions:**
- Agreement lifecycle managed entirely within Company aggregate
- Overlap detection compares destination, type, and date ranges
- Agreement.updateValidTo() accepts null (no expiry date)
- Agreement.updateConditions() requires at least one condition
- Company.updateAgreement() detects field changes for event payload

**Files Modified:**
- AgreementConditionType.java — Added two new enum values
- Agreement.java — Added factory method and three lifecycle methods
- Company.java — Fixed addAgreement/removeAgreement to return new instances, added updateAgreement

**Files Created:**
- AgreementAdded.java — Domain event for agreement creation
- AgreementRemoved.java — Domain event for agreement removal
- AgreementUpdated.java — Domain event for agreement updates

**Compilation Status:** ✅ SUCCESS (mvn clean compile passed)


### 2026-02-24: Phase 5-6 Application & Infrastructure Layer Implementation Complete

**Implemented Tasks (Plan 002 - Company Agreement):**

**Phase 5: Application Layer (Use Cases) - 5 Use Cases Created:**
- 5.1: CreateAgreementUseCase — POST operation, validates companies exist, calls Agreement.createAgreement, adds to Company
- 5.2: GetAgreementsByCompanyUseCase — GET list, returns agreement views with metadata
- 5.3: GetAgreementByIdUseCase — GET single, uses findCompanyByAgreementId repository method
- 5.4: UpdateAgreementUseCase — PUT operation, supports validTo and conditions updates
- 5.5: RemoveAgreementUseCase — DELETE operation, removes agreement from Company

**Phase 6: Infrastructure Layer (Controllers + DTOs) - 5 Controllers + 6 DTOs Created:**

**Controllers:**
- CreateAgreementController — POST /companies/{id}/agreements
- GetAgreementsByCompanyController — GET /companies/{id}/agreements
- GetAgreementByIdController — GET /agreements/{id}
- UpdateAgreementController — PUT /agreements/{id}
- RemoveAgreementController — DELETE /agreements/{id}

**DTOs:**
- CreateAgreementDTO — Request with destinationCompanyId, type, configuration, conditions, validFrom/To
- UpdateAgreementDTO — Request with validTo, conditions
- AgreementResponseDTO — Response with agreementId, sourceCompanyId, destinationCompanyId, agreementType
- AgreementUpdateResponseDTO — Response with agreementId, message
- AgreementDetailResponseDTO — Full agreement details for GET by ID
- AgreementsListResponseDTO — Response with companyId + list of agreement views

**Exception Created:**
- AgreementNotFoundException — Domain exception for agreement not found scenarios

**Key Patterns Applied:**
- All use cases: @DomainService + @Cqrs(DatabaseRole.WRITE/READ)
- Input/Output as nested records in use cases
- All controllers: RestUseCaseExecutor pattern + DefaultRestPresenter
- Zero business logic in controllers — pure delegation
- All variables declared final
- DTOs provide toInput() helper methods for mapping
- Proper HTTP status codes: 201 Created, 200 OK, 204 No Content

**Architecture Decisions:**
- CreateAgreementUseCase validates both source and destination companies exist
- UpdateAgreementUseCase applies updates conditionally (only non-null fields)
- RemoveAgreementController uses NO_CONTENT status (standard REST pattern)
- GetAgreementsByCompanyUseCase returns lightweight views, GetAgreementByIdUseCase returns full details
- All agreement lookups use findCompanyByAgreementId repository method (requires Phase 1 implementation)

**Files Created (16 total):**

**Use Cases (5):**
- CreateAgreementUseCase.java
- GetAgreementsByCompanyUseCase.java
- GetAgreementByIdUseCase.java
- UpdateAgreementUseCase.java
- RemoveAgreementUseCase.java

**Controllers (5):**
- CreateAgreementController.java
- GetAgreementsByCompanyController.java
- GetAgreementByIdController.java
- UpdateAgreementController.java
- RemoveAgreementController.java

**DTOs (6):**
- CreateAgreementDTO.java
- UpdateAgreementDTO.java
- AgreementResponseDTO.java
- AgreementUpdateResponseDTO.java
- AgreementDetailResponseDTO.java
- AgreementsListResponseDTO.java

**Exceptions (1):**
- AgreementNotFoundException.java

**Compilation Status:** ✅ SUCCESS (mvn clean compile passed)

**Remaining Work (Plan 002):**
- Phase 1: Persistence Layer (CRITICAL — agreements won't save without this)
- Phase 3: Database Migrations (indexes, constraints)
- Phase 7: Tests (domain, integration)
- Phase 8: HTTP Request Scenarios

**Notes:**
- Phase 5-6 implementation is complete and follows all TMS patterns
- Use cases depend on findCompanyByAgreementId() repository method from Phase 1
- Controllers are ready to receive HTTP requests once persistence layer is implemented
- All code compiles successfully and follows immutability patterns


### 2026-02-26: E2E Testing Skill Extracted

**Created Skill:**
- `.squad/skills/e2e-testing-tms/SKILL.md` — Generalized E2E REST API testing patterns for TMS

**Skill Metadata:**
- **Confidence:** Medium (validated through agreement E2E session, multiple bugs found and fixed)
- **Domain:** REST API testing, JPA relationships, Spring Boot
- **Title:** "E2E Testing in TMS"

**Patterns Extracted:**

1. **Environment Setup**
   - `make start-tms` runs PostgreSQL + RabbitMQ + app (port 8080)
   - Minimal vs full stack decision criteria
   - Health check verification commands

2. **HTTP Test File Structure**
   - Location pattern: `src/main/resources/{module}/{feature}-e2e-{variant}.http`
   - Template with phases: dependency setup, CRUD operations, negative cases
   - IntelliJ HTTP Client format with variable capture

3. **Value Object Formatting**
   - CNPJ: `"##.###.###/####-##"` (14 digits with formatting)
   - UUID: standard v4/v7 format
   - Instant: ISO 8601 UTC (`"yyyy-MM-ddTHH:mm:ssZ"`)

4. **DTO Field Validation Patterns**
   - Required vs optional field handling
   - Enum string-to-enum conversion and validation
   - Configuration map patterns (`Map<String, Object>`)

5. **JPA Bidirectional Relationship Patterns** (CRITICAL)
   - **Transient Entity Bug → Resolver Function Pattern**: Map domain IDs to JPA entities BEFORE save
   - **Circular hashCode → ID-Only equals/hashCode**: Use only ID field in equals/hashCode for bidirectional entities
   - **Lombok @Data Dangers**: Never use on JPA entities — generates circular hashCode
   - Recommended: `@Getter + @Setter + @NoArgsConstructor + @EqualsAndHashCode(onlyExplicitlyIncluded = true)`

6. **REST Controller Routing**
   - Nested resource path pattern: `/{parent}/{parentId}/{child}/{childId?}`
   - `@PathVariable` extraction from nested routes
   - Controller passes parent ID into use case input

7. **E2E Test Flow Template**
   - Phase 1: Create ALL dependencies first
   - Phase 2: CRUD operations (Create → List → Get → Update → Verify → Delete → Verify)
   - Phase 3: Negative cases (duplicate, self-reference, missing FK, invalid ranges, enum mismatch)

8. **Common Gotchas**
   - Empty configuration validation
   - Early enum validation in use cases
   - Path variable name matching
   - Transient entity exceptions
   - Circular hashCode in bidirectional relationships

**Why This Skill is Reusable:**
- NOT specific to agreements — applies to ANY TMS entity CRUD flow
- Resolver pattern works for ANY ManyToOne FK scenario
- ID-only equals/hashCode applies to ALL JPA bidirectional entities
- HTTP test template is entity-agnostic
- Debugging section covers common TMS error patterns

**Session Context (Today's Validation):**
- Fixed transient entity bug in agreement creation via resolver pattern
- Fixed circular hashCode in AgreementJpaEntity
- Validated full agreement E2E flow (create, list, get, update, delete)
- Tested negative cases (duplicate, self-reference, missing FK, invalid dates)
- All patterns documented in `agreement-e2e-simple.http`

**Related Skills:**
- `.squad/skills/immutable-aggregate-update/SKILL.md` — Domain immutability patterns
- `.squad/skills/fake-repository-pattern/SKILL.md` — Unit testing
- `.squad/skills/test-data-builder-pattern/SKILL.md` — Test data creation

**Future Applications:**
- When creating E2E tests for ShipmentOrder, Driver, Vehicle, Route entities
- When debugging JPA relationship issues in any module
- When onboarding new team members to TMS testing patterns
- When reviewing HTTP test files for consistency


### 2026-02-26: Test Infrastructure Patterns Skill Extracted

**Created Skill:**
- `.squad/skills/test-infrastructure-patterns/SKILL.md` — Complete test infrastructure guide for TMS

**Skill Metadata:**
- **Confidence:** High (validated across Company and Agreement entities, 70+ test cases)
- **Domain:** Testing, AssertJ, Builders, Fakes, Integration fixtures
- **Title:** "Test Infrastructure Patterns"

**Six Patterns Documented:**

1. **Custom AssertJ Assertions** — Domain-aware fluent assertions
   - Pattern: Extend AbstractAssert, static factory, fluent chainable methods
   - Location: `src/test/java/br/com/logistics/tms/assertions/domain/{module}/`
   - Example: AgreementAssert with 20+ assertion methods
   - When: Entity has 5+ common assertion patterns

2. **Test Data Builders** — Fluent builders with sensible defaults
   - Pattern: Static factory `an{Entity}()`, fluent withers, build() calls domain factory
   - Location: `src/test/java/br/com/logistics/tms/builders/domain/{module}/`
   - Example: AgreementBuilder with defaults for all fields
   - When: 3+ parameters, used in 3+ tests

3. **Fake Repositories** — In-memory implementations for unit tests
   - Pattern: HashMap storage, implements repository interface, NO Spring
   - Location: `src/test/java/br/com/logistics/tms/{module}/application/repositories/`
   - Example: FakeCompanyRepository with query methods and test helpers
   - When: Unit testing use cases without database

4. **Integration Fixtures** — Encapsulate REST calls + validation
   - Pattern: MockMvc + ObjectMapper, methods return IDs, includes basic validation
   - Location: `src/test/java/br/com/logistics/tms/integration/fixtures/`
   - Example: AgreementIntegrationFixture for CRUD operations
   - When: Same REST call pattern in 3+ integration tests

5. **Story-Driven Integration Tests** — Single test = complete business flow
   - Pattern: Extends AbstractIntegrationTest, story parts, realistic names, verify DB
   - Location: `src/test/java/br/com/logistics/tms/integration/`
   - Example: CompanyAgreementIT with 8-part story (create → update → delete → verify)
   - When: Testing complete business flows end-to-end

6. **Unit Test Structure** — Use case tests with fakes, builders, assertions
   - Pattern: NO Spring, fake repos, builders for data, custom assertions for verification
   - Location: `src/test/java/br/com/logistics/tms/{module}/application/usecases/`
   - Example: CreateAgreementUseCaseTest with fake repository
   - When: Testing use case business logic in isolation

**Key Principles Captured:**
- Layered approach: Domain tests (fast) → Use case tests (fast + fakes) → Integration tests (full stack)
- Consistent patterns across all entities
- Readable, fluent test code
- Sensible defaults reduce boilerplate
- Isolation at the right level (unit vs integration)
- Story-driven integration tests document behavior
- All test code follows TMS standards (final, immutability, value objects)

**Complete Testing Flow for New Entities:**
1. Create Custom Assertions → domain-specific validation
2. Create Test Data Builder → reduce test setup boilerplate
3. Create Fake Repository → enable fast use case testing
4. Write Use Case Unit Tests → validate business logic
5. Create Integration Fixture → encapsulate REST operations
6. Write Story-Driven Integration Tests → validate full flows

**Anti-Patterns Documented:**
- ❌ Don't use Mockito for repositories (use fakes instead)
- ❌ Don't inline test data creation (use builders)
- ❌ Don't use plain AssertJ for domain objects (use custom assertions)
- ❌ Don't mix multiple scenarios in one integration test
- ❌ Don't skip database verification in integration tests
- ❌ Don't use Spring context in use case tests

**Why This Skill is Reusable:**
- NOT specific to Agreement or Company — applies to ANY TMS entity
- Provides templates for all six patterns
- Includes real implementations from existing code
- Guides agents creating tests for NEW entities
- Documents "when to extract" decision criteria
- Cross-references related skills and docs

**Reference Implementations:**
- AgreementAssert.java — 20+ fluent assertions
- AgreementBuilder.java — Builder with sensible defaults
- FakeCompanyRepository.java — In-memory with query methods
- AgreementIntegrationFixture.java — REST call encapsulation
- CompanyAgreementIT.java — 8-part business story
- CreateAgreementUseCaseTest.java — Use case with fake repo

**Related Skills:**
- `.squad/skills/fake-repository-pattern/SKILL.md` — Detailed fake patterns
- `.squad/skills/test-data-builder-pattern/SKILL.md` — Builder best practices
- `.squad/skills/immutable-aggregate-update/SKILL.md` — Domain patterns
- `.squad/skills/e2e-testing-tms/SKILL.md` — HTTP E2E testing

**Future Applications:**
- When creating tests for ShipmentOrder, Driver, Vehicle, Route entities
- When reviewing test structure for consistency
- When onboarding new team members to TMS testing approach
- When deciding what test infrastructure to create for a new entity


### 2026-02-26: Agreement Domain Test Infrastructure Created

**Created Files:**
- `AgreementAssert.java` — Custom AssertJ assertion at `/src/test/java/br/com/logistics/tms/assertions/domain/company/AgreementAssert.java`
- `AgreementBuilder.java` — Test data builder at `/src/test/java/br/com/logistics/tms/builders/domain/company/AgreementBuilder.java`

**AgreementAssert Fluent Assertions:**
- Identity: `hasAgreementId(UUID)`
- Relationships: `hasFrom(CompanyId/UUID)`, `hasTo(CompanyId/UUID)`, `isBetween(from, to)`
- Type: `hasType(AgreementType)`
- Configurations: `hasConfigurationEntry(key, value)`, `hasConfigurationKey(key)`, `doesNotHaveConfigurationKey(key)`
- Conditions: `hasConditionsCount(int)`, `hasCondition(condition)`, `hasEmptyConditions()`
- Validity: `hasValidFrom(Instant)`, `hasValidTo(Instant)`, `hasNoValidTo()`
- Status: `isActive()`, `isNotActive()`, `isValidOn(date)`, `isNotValidOn(date)`
- Overlaps: `overlapsWith(other)`, `doesNotOverlapWith(other)`

**AgreementBuilder Fluent Withers:**
- Identity: `withFrom(CompanyId/UUID)`, `withTo(CompanyId/UUID)`
- Type: `withType(AgreementType)`
- Configuration: `withConfiguration(Map)`, `withConfigurationEntry(key, value)`
- Conditions: `withConditions(Set)`, `withCondition(condition)`
- Validity: `withValidFrom(Instant)`, `withValidTo(Instant)`, `withNoValidTo()`
- Factory: `anAgreement()` static factory method
- Build: `build()` calls `Agreement.createAgreement()` with sensible defaults

**Patterns Applied:**
- All variables declared final (constructor parameters, method parameters, local variables)
- Followed CompanyAssert/CompanyBuilder patterns EXACTLY
- AssertJ fluent assertion chain pattern
- Builder pattern with fluent withers
- Default configuration (`{"default": true}`) when empty
- Default type: `DELIVERS_WITH`
- Default validFrom: `Instant.now()`
- Default validTo: `null` (open-ended agreement)
- Two source company IDs: `CompanyId` (value object) and `UUID` (primitive) overloads

**Key Assertions for Agreement-Specific Behavior:**
- `isActive()` / `isNotActive()` — Tests Agreement.isActive() method
- `isValidOn(date)` / `isNotValidOn(date)` — Tests Agreement.isValidOn(date) method
- `isBetween(from, to)` / `isNotBetween(from, to)` — Tests Agreement.isBetween(from, to) method
- `overlapsWith(other)` / `doesNotOverlapWith(other)` — Tests Agreement.overlapsWith(other) method

**Compilation Status:** ✅ SUCCESS (mvn test-compile passed)

**Usage Examples:**
```java
// Builder
Agreement agreement = AgreementBuilder.anAgreement()
    .withFrom(sourceCompanyId)
    .withTo(destCompanyId)
    .withType(AgreementType.DELIVERS_WITH)
    .withConfigurationEntry("discountPercent", 10)
    .withValidFrom(Instant.now())
    .withValidTo(Instant.now().plus(365, ChronoUnit.DAYS))
    .build();

// Assertion
assertThatAgreement(agreement)
    .hasFrom(sourceCompanyId)
    .hasTo(destCompanyId)
    .hasType(AgreementType.DELIVERS_WITH)
    .hasConfigurationEntry("discountPercent", 10)
    .isActive();
```

**Related Test Infrastructure:**
- CompanyAssert/CompanyBuilder for Company aggregate testing
- Existing domain tests can now use these utilities for Agreement scenarios
- Integration tests can use builder for test data setup


### 2026-02-26: GitHub Copilot Instructions Updated — Test Infrastructure Requirements

**Requested by:** Leonardo Moreira

**What:** Expanded "Testing Approach" section in `.github/copilot-instructions.md` with comprehensive test requirements

**Why:** 
- Make test infrastructure mandatory for new entities (custom assertions, builders, fakes, fixtures)
- Document story-driven integration test pattern
- Define clear anti-patterns to prevent layer-based tests
- Provide concrete examples of unit and integration test structure
- Reference test-infrastructure-patterns skill for detailed patterns

**Changes Made:**

1. **Mandatory Test Infrastructure** — Four required components:
   - Custom AssertJ Assertions (domain-aware validation)
   - Test Data Builders (fluent API with defaults)
   - Fake Repositories (in-memory for unit tests)
   - Integration Fixtures (encapsulate REST calls)

2. **Unit Test Requirements:**
   - NO Spring context
   - Use fakes instead of Mockito
   - Use builders and custom assertions
   - Example with CreateAgreementUseCaseTest

3. **Integration Test Requirements:**
   - Story-driven (one test = complete flow)
   - Use fixtures for REST operations
   - Verify at database level
   - Example with CompanyAgreementStoryTest (7-part lifecycle)

4. **Anti-Patterns Section:**
   - 7 explicit "DO NOT DO" rules
   - Clear guidance on what to avoid

5. **Skills Table Update:**
   - Added `test-infrastructure-patterns` skill (🟢 High confidence)

6. **Quick Decision Tree Update:**
   - Updated test-related paths to reference "Testing Approach" section
   - Consolidated builder/fake references to test-infrastructure-patterns skill

**Key Patterns Documented:**

```java
// Unit test pattern
class CreateAgreementUseCaseTest {
    private FakeCompanyRepository companyRepository;  // ✅ Fake, not mock
    private CreateAgreementUseCase useCase;
    
    @Test
    void shouldCreateAgreement() {
        final Company source = anCompany().withName("Shoppe").build();  // ✅ Builder
        // ... test logic ...
        assertThatCompany(updated).hasAgreementsCount(1);  // ✅ Custom assertion
    }
}

// Integration test pattern
@SpringBootTest
class CompanyAgreementStoryTest {
    @Test
    void completeAgreementLifecycle() {
        // Part 1-7: Complete business story
        final UUID id = companyFixture.createCompany(...);  // ✅ Fixture
        // ... verify at DB level ...
        assertThatCompany(company).hasAgreementsCount(0);  // ✅ Custom assertion
    }
}
```

**Impact:**
- AI agents now have clear requirements for test infrastructure
- Examples show EXACTLY what "story-driven" means
- Anti-patterns prevent common mistakes (layer tests, Mockito repositories, etc.)
- Links to comprehensive skill for detailed patterns

**Decision:** Test infrastructure is now a FIRST-CLASS concern alongside domain/application/infrastructure layers. New entity checklist MUST include assertions, builders, fakes, and fixtures.

**Files Modified:**
- `.github/copilot-instructions.md` — Testing Approach section expanded from 15 lines to ~180 lines


### 2026-02-26: AgreementEntity JPA Mapping — Both UUID Strategy

**Requested by:** Leonardo Moreira
**Context:** Refactoring AgreementEntity mapping where `from` was @ManyToOne but `destination` was UUID

**Analysis from DDD Perspective:**

**1. Aggregate Boundary Violation:**
- Agreement references TWO Company aggregates (`from` and `to`)
- @ManyToOne creates a **hard dependency** on CompanyEntity in the infrastructure layer
- This couples AgreementEntity to CompanyEntity's **persistence lifecycle**
- Violates the principle: "Reference other aggregates by ID only, not by object reference"

**2. Domain Purity:**
- Domain model Agreement is **symmetric**: both `from` and `to` are `CompanyId` value objects
- Infrastructure asymmetry (`@ManyToOne` vs `UUID`) creates **conceptual mismatch**
- Hexagonal architecture principle: infrastructure should **mirror domain structure** where possible

**3. Infrastructure Layer Principle:**
- JPA adapters should use **IDs for inter-aggregate references**
- @ManyToOne forces Hibernate to manage Company entity lifecycle within Agreement persistence context
- This creates **implicit joins** and **lazy loading dependencies** that don't exist in the domain
- Eventual consistency pattern (already used in TMS) assumes **loose coupling via IDs**

**4. Hexagonal Architecture:**
- @ManyToOne **leaks persistence concerns** into what should be a pure mapping
- Domain says: "Agreement knows Company IDs", not "Agreement owns Company references"
- Infrastructure should be a **thin translation layer**, not add new relationships

**Decision:** ✅ **Both should be UUIDs**

**Rationale:**
1. **Consistency with domain model** — Both are CompanyId in domain, both UUID in JPA
2. **Respects aggregate boundaries** — No cross-aggregate object references
3. **Follows TMS eventual consistency pattern** — Other modules use UUID references (e.g., ShipmentOrder references Company by UUID)
4. **Simpler transaction management** — No cascade concerns, no lazy loading surprises
5. **Explicit over implicit** — Repository methods handle FK lookups explicitly when needed

**Recommended Refactoring:**

```java
@Entity
@Table(name = "agreement", schema = CompanySchema.COMPANY_SCHEMA)
public class AgreementEntity {
    @Id
    private UUID id;

    @Column(name = "source", nullable = false)
    private UUID fromCompanyId;  // ✅ Changed from @ManyToOne

    @Column(name = "destination", nullable = false)
    private UUID toCompanyId;     // ✅ Already UUID, just rename for symmetry

    // ... other fields ...

    public Agreement toAgreement() {
        return new Agreement(
            AgreementId.with(this.id),
            CompanyId.with(this.fromCompanyId),  // ✅ Symmetric mapping
            CompanyId.with(this.toCompanyId),    // ✅ Symmetric mapping
            // ... other fields ...
        );
    }
}
```

**Impact on CompanyEntity:**
- Remove `@OneToMany(mappedBy = "from")` — no longer valid
- Use `@OneToMany(cascade = ALL, orphanRemoval = true)` with **join column** strategy
- CompanyEntity owns the relationship via `@JoinColumn(name = "source")`

**Trade-offs Accepted:**
- ⚠️ Queries like "find all agreements where this company is destination" require explicit JOIN
- ✅ This is acceptable — it's a **repository concern**, not a domain concern
- ✅ Repository layer can provide helper methods if needed

**Alignment with TMS Patterns:**
- ✅ Matches ShipmentOrder → Company reference pattern (UUID, not @ManyToOne)
- ✅ Consistent with eventual consistency approach
- ✅ Respects hexagonal architecture boundaries
- ✅ Keeps aggregates loosely coupled

**Related Decisions:**
- 2026-02-24T18:52:00Z: Agreement remains part of Company aggregate (NOT separate root)
- 2026-02-26: Functional dependency injection for entity reference resolution
- 2026-02-26T14:09:00Z: JPA Entity Equals/HashCode Best Practice

**Files to Update:**
- AgreementEntity.java — Change `@ManyToOne CompanyEntity from` → `UUID fromCompanyId`
- CompanyEntity.java — Update `@OneToMany` mapping strategy
- CompanyRepositoryImpl.java — Adjust factory method resolver logic if needed
