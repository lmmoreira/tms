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

### 2026-02-26: Analysis of symmetric UUID mapping for Agreement source/destination

**Question:** Should both `source` and `destination` in `AgreementEntity` be UUIDs instead of having `source` as `@ManyToOne` relationship?

**Current State:**
- `AgreementEntity.from` → `@ManyToOne(fetch = FetchType.LAZY)` with `@JoinColumn(name = "source")`
- `AgreementEntity.destinationId` → `@Column(name = "destination", nullable = false)` (UUID)
- Domain `Agreement` → symmetric: both `from` and `to` are `CompanyId` value objects

**Database Schema:**
```sql
CREATE TABLE company.agreement (
    source UUID NOT NULL REFERENCES company.company(id),
    destination UUID NOT NULL REFERENCES company.company(id)
);
-- Both have foreign key constraints with ON DELETE RESTRICT
-- Both have indexes (idx_agreement_source, idx_agreement_destination)
```

**Analysis:**

**1. Consistency:**
- ✅ **Domain is symmetric** — `Agreement(from: CompanyId, to: CompanyId)` treats both sides equally
- ❌ **JPA is asymmetric** — source is navigable relationship, destination is raw UUID
- This mismatch creates cognitive overhead: developers must remember which side has navigation vs which is just an ID

**2. Coupling:**
- ❌ **@ManyToOne introduces bidirectional coupling:**
  - `AgreementEntity` → requires `CompanyEntity` reference for source
  - `CompanyEntity.agreements` → `@OneToMany(mappedBy = "from")` owns the relationship
  - Cascade behavior flows from Company → Agreement (line 54 in CompanyEntity)
  - This is correct for the aggregate pattern: Company is the aggregate root, Agreement is part of it
- ✅ **UUID-only would be one-way:**
  - Agreement table only stores UUIDs
  - No JPA-level navigation from Agreement → Company
  - Cascade still works via `@OneToMany` on CompanyEntity side
  - Referential integrity enforced at database level (foreign keys)

**3. Query Implications:**
- **N+1 Risk:** 
  - Current: When loading agreements and accessing `from.getName()`, triggers lazy-load SELECT per agreement
  - UUID-only: No lazy-load risk — you must explicitly join if you need Company data
- **Index Usage:**
  - Both approaches use the same indexes (idx_agreement_source, idx_agreement_destination)
  - Query patterns remain identical at SQL level
- **Performance:**
  - Current: Slight overhead from proxy management, potential N+1 if not careful
  - UUID-only: Leaner — no proxy overhead, forces explicit joins

**4. Cascade Impact:**
- **Critical:** `CompanyEntity.agreements` uses `@OneToMany(mappedBy = "from")` 
  - `mappedBy` references the Java field name in `AgreementEntity`, NOT the column name
  - If we change `from` field to `sourceId` (UUID), we MUST update `mappedBy`:
    ```java
    // Before: @OneToMany(mappedBy = "from", ...)
    // After:  @OneToMany(mappedBy = "agreement", ...) ← NO! Can't map to non-relationship
    ```
- **JPA Constraint:** `mappedBy` MUST reference a `@ManyToOne` or `@OneToOne` field. It cannot reference a plain UUID column.
- **Workaround:** Use `@JoinColumn` on the owning side:
  ```java
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "source", referencedColumnName = "id")
  private Set<AgreementEntity> agreements;
  ```
  This maintains unidirectional mapping: Company owns the relationship, Agreement doesn't navigate back.

**5. Recommendation:**

**✅ REFACTOR to symmetric UUIDs** for the following reasons:

**Pros:**
1. **Domain alignment** — JPA mirrors domain structure (both sides are IDs)
2. **Simpler mental model** — no need to remember which side is navigable
3. **Prevents N+1 bugs** — forces explicit joins, making query costs visible
4. **Cleaner entity mapping** — Agreement doesn't need CompanyEntity reference
5. **Testability** — `AgreementEntity.of()` doesn't need resolver function or entity parameters
6. **Current cascade already works** — `CompanyEntity.agreements` with `@JoinColumn` maintains ownership

**Cons:**
1. **Requires `@JoinColumn` change** — must shift from `mappedBy` to explicit `@JoinColumn` in `CompanyEntity`
2. **No navigation from Agreement** — if you need source Company data, must join explicitly
3. **Slight API change** — `AgreementEntity.of()` signature changes (no longer needs `fromEntity` parameter)

**Action Items:**
1. Change `AgreementEntity.from` from `@ManyToOne CompanyEntity` to `@Column private UUID sourceId`
2. Update `CompanyEntity.agreements` from `mappedBy = "from"` to `@JoinColumn(name = "source")`
3. Update `AgreementEntity.of()` to accept only `Agreement` (remove `fromEntity` parameter)
4. Update `AgreementEntity.toAgreement()` to use `sourceId` directly (line 81)
5. Verify cascade behavior with tests (should be unchanged)

**Pattern Established:** For value-object relationships in domain (e.g., CompanyId), use UUID columns in JPA rather than navigable relationships, unless navigation is actually needed for business queries. Aggregate ownership is maintained via `@OneToMany` with `@JoinColumn` on the parent side.


### 2026-02-26: Refactored AgreementEntity to symmetric UUID mapping

**What Changed:**
1. `AgreementEntity.from` (`@ManyToOne CompanyEntity`) → `sourceId` (`@Column UUID`)
2. Both source and destination are now plain UUID columns
3. Updated `CompanyEntity.agreements` cascade from `mappedBy = "from"` to `@JoinColumn(name = "source")`
4. Modified `AgreementEntity.of()` signature from `(Agreement, CompanyEntity)` → `(Agreement, UUID)` 
5. Updated `AgreementEntity.toAgreement()` to construct domain Agreement with `CompanyId.with(sourceId)` instead of `CompanyId.with(from.getId())`
6. Fixed `AgreementEntityAssert.hasFrom()` to check `sourceId` directly instead of navigating through entity reference
7. Removed `from` field from `@ToString` exclude list (no longer exists)

**Why This Matters:**
- ✅ **Domain alignment** — JPA now mirrors domain symmetry (both source and destination are CompanyId value objects)
- ✅ **Prevents N+1 queries** — no lazy-loaded Company reference means no accidental proxy triggers
- ✅ **Simpler mapping** — `AgreementEntity.of()` no longer needs entity parameter, just the source UUID
- ✅ **Cascade still works** — `@OneToMany` with explicit `@JoinColumn` maintains Company → Agreement ownership
- ✅ **Cleaner assertions** — test code doesn't need to check entity presence, just UUID equality

**Technical Details:**
- **Cascade pattern change:** From bidirectional `mappedBy` to unidirectional `@JoinColumn`
  ```java
  // Before:
  @OneToMany(mappedBy = "from", cascade = CascadeType.ALL, ...)
  
  // After:
  @OneToMany(cascade = CascadeType.ALL, ...)
  @JoinColumn(name = "source", nullable = false)
  ```
- **Why this works:** JPA allows `@OneToMany` to own the relationship via `@JoinColumn` on the parent side. The child (`AgreementEntity`) doesn't need a back-reference field — the foreign key in the database is sufficient for cascade operations.

**Database Schema:**
No changes required — both `source` and `destination` columns already existed as UUID with foreign key constraints. The refactor only changed the JPA mapping layer.

**Impact:**
- `AgreementEntity` no longer depends on `CompanyEntity` class (only UUID)
- Query patterns unchanged — indexes on `source` and `destination` still used
- Cascade delete/orphan removal still works (tested with compilation)
- Test infrastructure updated (AgreementEntityAssert now checks `sourceId` field)

**Files Modified:**
- `AgreementEntity.java` — Changed field type, updated mapping methods, removed from @ToString exclude
- `CompanyEntity.java` — Changed `@OneToMany` from `mappedBy` to `@JoinColumn`
- `AgreementEntityAssert.java` — Updated `hasFrom()` to check `sourceId` directly

**Compilation:** ✅ SUCCESS (`mvn clean compile` passed)

**Pattern Learned:** When domain uses value objects for relationships (e.g., CompanyId), prefer UUID columns in JPA entities over navigable `@ManyToOne` relationships. Aggregate ownership is maintained via `@OneToMany` with explicit `@JoinColumn` on the parent side, avoiding bidirectional coupling while preserving cascade behavior.

