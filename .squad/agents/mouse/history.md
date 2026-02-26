# Mouse — History

## Core Context
- **Project:** TMS (Transportation Management System)
- **Owner:** Leonardo Moreira
- **Tech Stack:** Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ
- **Architecture:** Modular Monolith, DDD, Hexagonal, CQRS, Event-Driven
- **Focus:** Agreement domain structure review

## Learnings

### 2026-02-26: Fixed Hibernate transient entity issue in Agreement creation

**Problem:** Agreement creation failed with `TransientPropertyValueException` because `AgreementEntity.of()` created a detached `CompanyEntity` for the destination reference (lines 65-67).

**Root Cause:** 
```java
final CompanyEntity destinationRef = new CompanyEntity();  // ❌ Detached entity
destinationRef.setId(agreement.to().value());
entity.setTo(destinationRef);
```

This creates a new CompanyEntity instance that isn't managed by the persistence context. Hibernate requires all referenced entities to be either:
1. Already persisted (attached to session)
2. Fetched via EntityManager.getReference() (lazy proxy)
3. Loaded from repository

**Solution Applied:** Function-based dependency injection pattern
- Modified `CompanyEntity.of(Company, Function<UUID, CompanyEntity>)` to accept a destination resolver function
- Modified `AgreementEntity.of(Agreement, CompanyEntity, CompanyEntity)` to accept both source and destination entities
- Added `CompanyRepositoryImpl.resolveDestinationCompany(UUID)` private method that fetches from JpaRepository
- Repository passes `this::resolveDestinationCompany` method reference to `CompanyEntity.of()`

**Why This Pattern:**
- ✅ Keeps entity classes clean (no repository dependencies)
- ✅ Maintains immutable aggregate pattern
- ✅ Resolves destination company from database (attached to session)
- ✅ Function parameter enables testability (can pass mock resolver)
- ✅ Compile-time type safety

**Alternative Approaches Considered:**
1. EntityManager.getReference() — requires EntityManager injection into entity (breaks clean architecture)
2. Fetch in use case — breaks cascade persistence pattern (agreements wouldn't save)
3. Pass repository to entity — violates separation of concerns

**Impact:**
- Agreement creation now properly references destination companies from database
- Cascade persistence works correctly (agreements save with Company)
- No changes to domain layer (immutability preserved)

**Files Modified:**
- AgreementEntity.java — Changed method signature to accept destination entity
- CompanyEntity.java — Added Function parameter for destination resolution
- CompanyRepositoryImpl.java — Added resolveDestinationCompany() helper method

**Pattern:** Functional dependency injection for entity reference resolution


### 2026-02-26: Fixed StackOverflowError from circular hashCode/equals in bidirectional JPA relationships

**Problem:** Circular reference between `CompanyEntity` (has agreements Set) and `AgreementEntity` (has from/to CompanyEntity) caused infinite loop:
```
CompanyEntity.hashCode() → includes agreements Set
  → AgreementEntity.hashCode() → includes from/to CompanyEntity
    → CompanyEntity.hashCode() → INFINITE LOOP (StackOverflowError)
```

**Root Cause:** Lombok `@Data` generates `equals()` and `hashCode()` that include ALL fields by default, triggering circular traversal in bidirectional relationships.

**Solution Applied:** Replace `@Data` with explicit annotations that exclude relationship fields:
- CompanyEntity: `@EqualsAndHashCode(of = "id")`, `@ToString(exclude = {"agreements"})`
- AgreementEntity: `@EqualsAndHashCode(of = "id")`, `@ToString(exclude = {"from", "to", "conditions"})`
- AgreementConditionEntity: `@EqualsAndHashCode(of = "id")`, `@ToString(exclude = {"agreement"})`

Added separate `@Getter`, `@Setter` annotations to maintain field access.

**Why This Pattern:**
- ✅ Standard JPA best practice — equals/hashCode should ONLY use entity ID
- ✅ Prevents circular reference issues in bidirectional relationships
- ✅ Avoids database queries during hashCode/equals (lazy-loaded collections stay lazy)
- ✅ Consistent object identity across persistence contexts
- ✅ Safer for use in collections (HashSet, HashMap)

**JPA Best Practice Rule:**
For ALL JPA entities with relationships (especially bidirectional):
1. Use `@EqualsAndHashCode(of = "id")` — only compare IDs
2. Use `@ToString(exclude = {relationship fields})` — prevent circular toString
3. Never include relationship fields in equals/hashCode
4. Never use `@Data` on JPA entities — always explicit annotations

**Impact:**
- StackOverflowError eliminated
- Safe hashCode/equals for collection usage
- Lazy relationships remain lazy during equals/hashCode calls
- Consistent behavior across Hibernate sessions

**Files Modified:**
- CompanyEntity.java — Replaced @Data with explicit annotations
- AgreementEntity.java — Replaced @Data with explicit annotations
- AgreementConditionEntity.java — Replaced @Data with explicit annotations

**Pattern:** ID-only equals/hashCode for JPA entities with relationship exclusion in toString.



### 2026-02-26: Created Agreement JPA test infrastructure

**What:** Built custom AssertJ assertion (AgreementEntityAssert) and test data builder (CreateAgreementDTOBuilder) for Agreement domain testing

**Why:** Enable fluent, readable test assertions and reduce test data setup boilerplate following TMS test patterns

**Implementation:**

**AgreementEntityAssert** (`src/test/java/br/com/logistics/tms/assertions/jpa/AgreementEntityAssert.java`):
- Fluent assertions for all Agreement entity fields: id, from/to companies, relationType, configuration, validFrom/validTo, conditions
- Company reference assertions validate both entity presence and ID equality
- Configuration assertions support both full map and individual entry checks
- Condition assertions support count and empty state checks
- Pattern matches CompanyEntityAssert structure exactly

**CreateAgreementDTOBuilder** (`src/test/java/br/com/logistics/tms/builders/dto/CreateAgreementDTOBuilder.java`):
- Sensible defaults: valid destination UUID, SELLS_ON type, non-empty configuration map (priority, autoRenew)
- Default condition: DISCOUNT_PERCENTAGE with 10% value (proper AgreementCondition construction with AgreementConditionId + Conditions value object)
- Date handling: validFrom truncated to seconds (matches DB precision), validTo nullable for open-ended agreements
- All fluent withers support both individual values and collections
- Uses UUID v7 via `Id.unique()` for destination company ID generation

**Key Patterns Applied:**
- ✅ ALL variables declared final (coding standard)
- ✅ Custom AssertJ assertion with static factory method (`assertThatAgreement()`)
- ✅ Builder with static factory method (`aCreateAgreementDTO()`)
- ✅ Defensive copies for mutable collections (HashMap constructor)
- ✅ Proper value object construction (AgreementCondition requires AgreementConditionId + Conditions wrapper)
- ✅ Timestamp truncation for database comparison safety (`truncatedTo(ChronoUnit.SECONDS)`)
- ✅ Followed reference patterns from Company test infrastructure exactly

**Domain Knowledge Captured:**
- AgreementCondition constructor signature: `(AgreementConditionId, AgreementConditionType, Conditions)` — NOT raw Map
- AgreementType enum values: SELLS_ON, DELIVERS_WITH, OFFERS_PICKUP, OPERATES
- Id class location: `br.com.logistics.tms.commons.domain.Id` (NOT utils package)
- Configuration map needs concrete entries for realistic test scenarios (empty maps fail JSON validation in some tests)

**Files Created:**
- `src/test/java/br/com/logistics/tms/assertions/jpa/AgreementEntityAssert.java` (124 lines)
- `src/test/java/br/com/logistics/tms/builders/dto/CreateAgreementDTOBuilder.java` (93 lines)

**Compilation:** ✅ SUCCESS (`mvn test-compile` passed)
