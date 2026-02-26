# History — Trinity

## Project Context (Day 1)

**Product:** TMS (Transportation Management System)
**Tech Stack:** Java 21, Spring Boot 3.x, DDD/Hexagonal/CQRS/Event-Driven architecture
**Mission:** Documentation optimization for AI consumption
- Extract 6 reusable skills to .squad/skills/
- Consolidate docs: 260K → 118K tokens (55% reduction)
- Improve retrieval speed 5-10x (15-30s → 3-5s)

**Your Role:** Doc Engineer

**Owner:** Leonardo Moreira

---

## Learnings

### 2026-02-26 — JPA and REST API Patterns from E2E Testing

**Context:** Leonardo and Switch completed E2E testing for Company-Agreement agreement flow. Documented critical JPA and REST API patterns learned during implementation.

**JPA Bidirectional Relationships:**
- **ID-only equals/hashCode:** Essential for entities with bidirectional relationships (Company ↔ ShipmentOrder, Company ↔ Agreement). Prevents circular references and StackOverflowError.
- **Lombok @Data danger:** Never use @Data on entities with relationships — it generates equals/hashCode using ALL fields, causing circular traversal.
- **Resolver functions for FK references:** Always use `Function<UUID, Entity>` resolvers to fetch existing entities. Creating transient entities and assigning as FK causes `TransientObjectException`.

**REST API Patterns:**
- **Nested resource routing:** Use `/companies/{companyId}/agreements` for parent-child relationships.
- **Path variable requirement:** ALL path variables in `@RequestMapping` MUST appear in method signature as `@PathVariable`. Spring won't bind them otherwise.
- **DTO field validation:** Configuration fields validated in value objects (cannot be null/empty). Formatted value objects (CNPJ) receive raw input in DTOs, domain applies formatting.

**Environment Setup:**
- **Minimal Docker:** Use `make start-tms` for tests — starts only PostgreSQL + RabbitMQ (no OAuth2, no observability). Faster startup, lower resource usage.

**Documentation Updates:**
- Added "JPA Bidirectional Relationships" section to ARCHITECTURE.md (equals/hashCode, resolver functions, @Data dangers)
- Added "Nested Resource Routing Pattern" and "DTO Field Validation" sections to ARCHITECTURE.md (pattern 2)
- Added "DTO Field Validation" and "Environment Setup" to INTEGRATION_TESTS.md (best practices)
- Added "Common Pitfalls" section to QUICK_REFERENCE.md (JPA dangers, path variable requirements)
- All sections reference `.squad/skills/e2e-testing-tms/SKILL.md` (Switch is creating)

**Files Modified:**
- `/doc/ai/ARCHITECTURE.md` — Added JPA patterns and REST API patterns
- `/doc/ai/INTEGRATION_TESTS.md` — Added DTO validation and environment notes
- `/doc/ai/QUICK_REFERENCE.md` — Added quick lookups for common pitfalls

### 2026-02-26 — Test Infrastructure Patterns Documentation

**Context:** Created comprehensive test prompt template (`new-test.md`) to guide agents through creating complete test infrastructure for new entities.

**Test Infrastructure Components (10 files per entity):**
1. **Domain Custom Assertion** — `{Entity}Assert.java` in `assertions/domain/{module}/`
2. **Domain Test Data Builder** — `{Entity}Builder.java` in `builders/domain/{module}/`
3. **Fake Repository** — `Fake{Entity}Repository.java` in `{module}/application/repositories/`
4. **Use Case Input Builder** — `{UseCase}InputBuilder.java` in `builders/input/`
5. **DTO Builder** — `Create{Entity}DTOBuilder.java` in `builders/dto/`
6. **JPA Entity Assertion** — `{Entity}EntityAssert.java` in `assertions/jpa/`
7. **Integration Fixture** — `{Entity}IntegrationFixture.java` in `integration/fixtures/`
8. **Domain Unit Tests** — `{Entity}Test.java` (aggregate behavior, immutability, events)
9. **Use Case Unit Tests** — `{UseCase}Test.java` (with fake repositories, NO Spring)
10. **Integration Story Test** — `{Entity}IT.java` (story-driven E2E flows)

**Pattern Alignment:**
- Follows TEST_STRUCTURE.md centralized utilities pattern
- References fake-repository-pattern and test-data-builder-pattern skills
- Uses existing CompanyAssert and CompanyBuilder as reference examples
- Story-driven integration tests (complete business flows: create → update → delete)

**Key Testing Principles:**
- ✅ Custom assertions for fluent validation (`assertThatCompany(company).hasTypes("SELLER")`)
- ✅ Test builders with sensible defaults (builder works with just `.build()`)
- ✅ Fake repositories for stateful in-memory testing (no mock boilerplate)
- ✅ Integration fixtures encapsulate REST + async waiting (Awaitility)
- ✅ All parameters declared `final` (TMS coding standards)
- ✅ Domain tests verify immutability and domain events
- ✅ Use case tests use fakes (NO Spring context)
- ✅ Integration tests tell complete business stories

**Files Created:**
- `/doc/ai/prompts/new-test.md` — 580-line comprehensive test template

**Cross-References:**
- TEST_STRUCTURE.md (centralized utilities, naming conventions)
- fake-repository-pattern skill (in-memory repository pattern)
- test-data-builder-pattern skill (builder design principles)
- INTEGRATION_TESTS.md (integration testing guide)

**Context:** Leonardo requested comprehensive documentation of test infrastructure patterns learned during Agreement domain testing.

**What:** Updated `/doc/ai/TEST_STRUCTURE.md` and `/doc/ai/INTEGRATION_TESTS.md` with four critical test patterns:

1. **Custom AssertJ Assertions** - Domain-specific fluent assertions
   - Pattern: `AbstractAssert<{Name}Assert, {Type}>` with static factory
   - Example: `assertThatCompany(company).hasName("Test").hasTypes(SELLER)`
   - Location: `assertions/{domain|jpa|outbox}/`
   - Key: Chaining via `return this`, clear messages via `.as()`

2. **Test Data Builders** - Fluent object creation with sensible defaults
   - Pattern: Static factory `aCompany()`, private constructor, with methods
   - Example: `CompanyBuilder.aCompany().withName("Test").build()`
   - Location: `builders/{dto|input|domain}/`
   - Key: Use domain factory methods in `build()`, NOT constructors

3. **Fake Repositories** - In-memory implementations for unit tests
   - Pattern: `ConcurrentHashMap` storage, implements repository interface
   - Example: `FakeCompanyRepository` with `clear()` method
   - Location: `{module}/application/repositories/`
   - Key: Thread-safe, matches production repository signatures

4. **Integration Fixtures** - Encapsulate REST + wait logic
   - Pattern: REST call → extract ID → wait outbox → wait sync → return typed ID
   - Example: `companyFixture.createCompany(dto)` returns `CompanyId`
   - Location: `integration/fixtures/`
   - Key: One method = one complete business operation

**Story-Driven Philosophy Added:**
- Emphasized **NO layer tests without business context**
- Integration tests should tell business stories, NOT test technical layers
- Example: "shouldRegisterNewCompanyInPlatform" vs "repositoryShouldSaveCompany"
- One test = one complete user workflow (create → update → order → validate)

**Documentation Structure:**
- TEST_STRUCTURE.md: Added 4 new sections after "Complete Example" (line 405+)
- INTEGRATION_TESTS.md: Enhanced Philosophy section, expanded Fixtures explanation, updated Best Practices

**Key Principles Documented:**
- All variables `final` (TMS coding standard)
- Static factory methods for builders and assertions
- Fixtures are NOT `@Component` (manually instantiated)
- Builders use domain factory methods (respect immutability)
- Custom assertions provide `.as()` descriptions for failures

**Impact:**
- Developers can now implement these patterns consistently
- Clear examples for each pattern type
- Cross-references to existing code and prompts
- Story-driven testing philosophy now explicit

**Files Modified:**
- `/doc/ai/TEST_STRUCTURE.md` — Added 4 pattern sections (300+ lines)
- `/doc/ai/INTEGRATION_TESTS.md` — Enhanced philosophy, fixtures, best practices (100+ lines)

**Total Lines:** 918 (TEST_STRUCTURE.md) + 861 (INTEGRATION_TESTS.md) = 1,779 lines

{agents append here}
