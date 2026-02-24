# Agreement Persistence Architecture Review

**Reviewer:** Coordinator (Leonardo Moreira)  
**Date:** 2026-02-24  
**Plan Reference:** 002-company-agreement-implementation.md  
**Critical Finding:** Missing repository layer specification

---

## Executive Summary

**Leonardo's Critical Question:** *"I did not find in the plan, where the agreements are saved"*

Plan 002 specifies domain behavior (Company.addAgreement/removeAgreement) and use cases but **completely omits the persistence layer architecture**. The codebase has partial infrastructure (AgreementEntity exists) but **NO mapping logic** between Agreement domain objects and AgreementEntity. This creates a critical implementation gap.

**Current State:**
- ✅ Domain layer complete: Agreement record, Company has Set<Agreement>
- ✅ JPA entities exist: AgreementEntity, AgreementConditionEntity
- ❌ **MISSING:** Mapping logic (Agreement ↔ AgreementEntity conversion)
- ❌ **MISSING:** Cascade persistence from Company to Agreement
- ❌ **MISSING:** Repository query methods for agreements

**Architecture Decision Required:** Should Agreement be:
- **Option A:** Part of Company aggregate (current domain structure)
- **Option B:** Separate aggregate with its own repository

**Recommendation:** **Option A** (Agreement as part of Company aggregate) with missing persistence layer added to Plan 002.

---

## 🔍 Analysis - Current State

### Domain Layer (✅ Complete)

**Company.java (lines 202-220):**
```java
private final Set<Agreement> agreements;

public Set<Agreement> getAgreements() {
    return Collections.unmodifiableSet(agreements);
}

public void addAgreement(final Agreement agreement) {
    if (agreements.contains(agreement)) {
        throw new ValidationException("Agreement already exists for this company");
    }
    agreements.add(agreement);  // ⚠️ Mutability issue (Plan 002 addresses this)
}

public void removeAgreement(final Agreement agreement) {
    if (!agreements.contains(agreement)) {
        throw new ValidationException("Agreement not found for this company");
    }
    agreements.remove(agreement);  // ⚠️ Mutability issue (Plan 002 addresses this)
}
```

**Agreement.java (lines 1-89):**
- Immutable `record` with all fields
- Value object pattern (no repository)
- Lives inside Company aggregate

---

### Infrastructure Layer (⚠️ Partial)

**CompanyEntity.java (lines 54-78) - THE GAP:**

```java
public static CompanyEntity of(final Company company) {
    return CompanyEntity.builder()
            .id(company.getCompanyId().value())
            .name(company.getName())
            .cnpj(company.getCnpj().value())
            .companyTypes(new HashSet<>(company.getCompanyTypes().value()))
            .configuration(new HashMap<>(company.getConfigurations().value()))
            .status(company.getStatus().value())
            .version((Integer) company.getPersistentMetadata().getOrDefault("version", null))
            .build();
            // ❌ NO AGREEMENT MAPPING
}

public Company toCompany() {
    return new Company(
            CompanyId.with(this.id),
            this.name,
            Cnpj.with(this.cnpj),
            CompanyTypes.with(this.companyTypes),
            Configurations.with(this.configuration),
            Collections.emptySet(),  // ❌ AGREEMENTS ALWAYS EMPTY
            Status.of(this.status),
            Collections.emptySet(),
            Map.of("version", this.version)
    );
}
```

**Critical Gaps:**
1. `CompanyEntity.of(company)` **does NOT map** `company.getAgreements()` to entity field
2. `CompanyEntity.toCompany()` **always returns** `Collections.emptySet()` for agreements
3. **NO OneToMany relationship** defined in CompanyEntity for agreements
4. **NO cascade logic** for saving agreements when Company is saved

---

**AgreementEntity.java (lines 14-43):**

```java
@Entity
@Table(name = "agreement", schema = CompanySchema.COMPANY_SCHEMA)
public class AgreementEntity {
    
    @Id
    private UUID id;
    
    @ManyToOne
    @JoinColumn(name = "source", nullable = false)
    private CompanyEntity from;  // ⚠️ Named "source" in DB, "from" in Java
    
    @ManyToOne
    @JoinColumn(name = "destination", nullable = false)
    private CompanyEntity to;
    
    @Column(name = "relation_type", nullable = false)
    private String relationType;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "configuration")
    private Map<String, Object> configuration;
    
    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;
    
    @Column(name = "valid_to")
    private Instant validTo;
    
    @OneToMany(mappedBy = "agreement", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<AgreementConditionEntity> conditions;
    
    // ❌ NO static of(Agreement) method
    // ❌ NO toAgreement() method
    // ❌ NO getters/setters (Lombok not used)
}
```

**Critical Gaps:**
1. **NO mapping methods:** `AgreementEntity.of(Agreement)` missing
2. **NO reconstruction:** `toAgreement()` method missing
3. **NO bidirectional setup** from CompanyEntity side

---

### CompanyRepositoryImpl.java (lines 45-49)

```java
@Override
public Company update(Company company) {
    final CompanyEntity companyEntity = CompanyEntity.of(company);
    companyJpaRepository.save(companyEntity);
    outboxGateway.save(CompanySchema.COMPANY_SCHEMA, company.getDomainEvents(), CompanyOutboxEntity.class);
    return company;
}
```

**What happens when agreement is added:**
1. Use case calls `company.addAgreement(agreement)`
2. Use case calls `companyRepository.update(company)`
3. `CompanyEntity.of(company)` is called
4. **Agreements are IGNORED** (not mapped)
5. JPA saves CompanyEntity **WITHOUT agreements**
6. Result: **Agreements never persisted**

---

## 🏗️ Architecture Decision

### Option A: Agreement as Part of Company Aggregate (RECOMMENDED)

**Domain Structure (Current):**
```
Company (aggregate root)
  └── Set<Agreement> (value objects, immutable records)
      └── Set<AgreementCondition> (nested value objects)
```

**Persistence Structure:**
```
CompanyEntity (JPA entity)
  └── @OneToMany Set<AgreementEntity> (cascade ALL, orphanRemoval)
      └── @OneToMany Set<AgreementConditionEntity>
```

**Pros:**
- ✅ Matches current domain design
- ✅ Single transaction boundary (Company + Agreements saved together)
- ✅ Single repository (CompanyRepository)
- ✅ Agreement lifecycle bound to Company (cannot exist without Company)
- ✅ Simpler: no coordination between aggregates
- ✅ Referential integrity via cascade

**Cons:**
- ❌ Must load full Company to query agreements
- ❌ Cannot query "all agreements with Loggi" without loading all companies
- ❌ Updating Shoppe→Loggi agreement requires loading Shoppe aggregate

**DDD Justification:**
- Agreement has NO independent lifecycle (cannot exist without source Company)
- Agreement changes are ALWAYS in context of Company business operations
- Transaction boundary is clear: Company + all its agreements
- Aggregate size manageable: companies typically have 1-10 agreements

---

### Option B: Agreement as Separate Aggregate (NOT RECOMMENDED)

**Domain Structure (Changed):**
```
Company (aggregate root)
  └── Set<AgreementId> (references only)

Agreement (new aggregate root)
  ├── AgreementId
  ├── CompanyId source
  ├── CompanyId destination
  └── own domain events
```

**Persistence Structure:**
```
CompanyEntity
  └── (no direct relationship to agreements)

AgreementEntity (separate table)
  └── source_company_id (FK)
  └── destination_company_id (FK)
```

**Pros:**
- ✅ Independent query: `AgreementRepository.findBySourceAndDestination(shoppeId, loggiId)`
- ✅ Update agreement without loading Company
- ✅ Query "all companies using Loggi" efficiently

**Cons:**
- ❌ **Breaks current domain design** (Company.agreements field would need removal)
- ❌ Two aggregates to coordinate
- ❌ Need new `AgreementRepository` + `AgreementRepositoryImpl`
- ❌ More complex consistency management
- ❌ Agreement lifecycle unclear: who owns creation/deletion?
- ❌ **Large refactor of existing code**

**DDD Issues:**
- Agreement has no meaningful lifecycle independent of Company
- "Update agreement discount" is fundamentally a Company operation (Company negotiates terms)
- Splitting creates artificial boundary

---

## 📊 Business Query Pattern Analysis

### Common Operations (Frequency Analysis)

| Operation | Option A | Option B | Frequency |
|-----------|----------|----------|-----------|
| **Get agreements for Company X** | Load Company → return agreements | `AgreementRepo.findBySource(x)` | **HIGH** (every order placement) |
| **Create agreement** | Load Company → addAgreement → save | Create Agreement → save via AgreementRepo | **LOW** (setup phase only) |
| **Update agreement discount** | Load Company → update → save | Load Agreement → update → save | **LOW** (renegotiation events) |
| **Remove agreement** | Load Company → remove → save | Delete via AgreementRepo | **LOW** (contract termination) |
| **Query "all companies with Loggi"** | Load ALL companies → filter | `AgreementRepo.findByDestination(loggi)` | **LOW** (admin reports) |
| **Check if agreement exists** | Load Company → check set | `AgreementRepo.findBySourceAndDestination` | **MEDIUM** (order routing logic) |

**Key Insight:** Most frequent operation is "get agreements for Company X" which happens **during order placement**. Option A (load Company) is acceptable because:
1. Company data is ALREADY needed for order placement (validation, configuration)
2. Loading Company + agreements is ONE query with JOIN
3. Result is cacheable at Company aggregate level

**Performance Consideration:** With indexes on `agreement.source_company_id`, both options have similar query cost for common case.

---

## ✅ Recommendation: Option A (Agreement Part of Company Aggregate)

**Rationale:**

1. **Matches Domain Design:** Current code already models Agreement as part of Company
2. **Simpler Implementation:** Single repository, single transaction boundary
3. **Business Alignment:** Agreements are negotiated/managed BY companies
4. **Manageable Size:** Companies have 1-10 agreements (not hundreds)
5. **Common Case Optimized:** Most queries are "get agreements for Company X"

**Trade-off Accepted:** Cross-company queries ("all agreements with Loggi") require loading multiple companies, BUT this is rare admin operation, not critical path.

---

## 🔧 Missing Implementation (Add to Plan 002)

### Task 3.1: Add @OneToMany Relationship to CompanyEntity

**File:** `src/main/java/br/com/logistics/tms/company/infrastructure/jpa/entities/CompanyEntity.java`

**Add field:**
```java
@OneToMany(mappedBy = "from", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
private Set<AgreementEntity> agreements;
```

**Update builder usage:**
```java
public static CompanyEntity of(final Company company) {
    final CompanyEntity entity = CompanyEntity.builder()
            .id(company.getCompanyId().value())
            .name(company.getName())
            .cnpj(company.getCnpj().value())
            .companyTypes(new HashSet<>(company.getCompanyTypes().value()))
            .configuration(new HashMap<>(company.getConfigurations().value()))
            .status(company.getStatus().value())
            .version((Integer) company.getPersistentMetadata().getOrDefault("version", null))
            .build();
    
    // Map agreements
    final Set<AgreementEntity> agreementEntities = company.getAgreements().stream()
            .map(agreement -> AgreementEntity.of(agreement, entity))
            .collect(Collectors.toSet());
    entity.setAgreements(agreementEntities);
    
    return entity;
}
```

---

### Task 3.2: Add Mapping Methods to AgreementEntity

**File:** `src/main/java/br/com/logistics/tms/company/infrastructure/jpa/entities/AgreementEntity.java`

**Add Lombok annotations:**
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
```

**Add static factory:**
```java
public static AgreementEntity of(final Agreement agreement, final CompanyEntity fromEntity) {
    final AgreementEntity entity = AgreementEntity.builder()
            .id(agreement.agreementId().value())
            .from(fromEntity)
            .to(null)  // ⚠️ Need to resolve destination CompanyEntity
            .relationType(agreement.type().name())
            .configuration(new HashMap<>(agreement.configurations().value()))
            .validFrom(agreement.validFrom())
            .validTo(agreement.validTo())
            .build();
    
    // Map conditions
    final Set<AgreementConditionEntity> conditionEntities = agreement.conditions().stream()
            .map(condition -> AgreementConditionEntity.of(condition, entity))
            .collect(Collectors.toSet());
    entity.setConditions(conditionEntities);
    
    return entity;
}
```

**⚠️ Problem:** `Agreement` has `CompanyId to` but `AgreementEntity` needs `CompanyEntity to`. This requires repository access to resolve the ID to entity.

---

### Task 3.3: Solve Foreign Key Resolution Problem

**Problem:** When creating `AgreementEntity` from `Agreement`, we have `CompanyId to` (UUID) but need `CompanyEntity to` (JPA entity reference).

**Solution Options:**

**Option 3.3a: Lazy Resolution (RECOMMENDED)**
```java
// In CompanyEntity.of(company):
final Set<AgreementEntity> agreementEntities = company.getAgreements().stream()
        .map(agreement -> {
            final AgreementEntity entity = AgreementEntity.of(agreement, companyEntity);
            // Set destination as lazy reference (JPA will resolve)
            final CompanyEntity destinationRef = new CompanyEntity();
            destinationRef.setId(agreement.to().value());
            entity.setTo(destinationRef);
            return entity;
        })
        .collect(Collectors.toSet());
```

**Pros:** No extra queries, JPA manages FK
**Cons:** Assumes destination company exists (handled by use case validation)

---

**Option 3.3b: Explicit Resolution**
```java
// In CompanyRepositoryImpl.update():
@Override
public Company update(final Company company) {
    final CompanyEntity companyEntity = companyJpaRepository.findById(company.getCompanyId().value())
            .orElseThrow();
    
    // Clear existing agreements
    companyEntity.getAgreements().clear();
    
    // Add new agreements with resolved destinations
    company.getAgreements().forEach(agreement -> {
        final CompanyEntity destinationEntity = companyJpaRepository.findById(agreement.to().value())
                .orElseThrow(() -> new NotFoundException("Destination company not found"));
        
        final AgreementEntity agreementEntity = AgreementEntity.of(agreement, companyEntity);
        agreementEntity.setTo(destinationEntity);
        companyEntity.getAgreements().add(agreementEntity);
    });
    
    companyJpaRepository.save(companyEntity);
    outboxGateway.save(CompanySchema.COMPANY_SCHEMA, company.getDomainEvents(), CompanyOutboxEntity.class);
    return company;
}
```

**Pros:** Explicit validation, clear FK resolution
**Cons:** N+1 query problem (one query per destination)

---

### Task 3.4: Update CompanyEntity.toCompany()

**File:** `src/main/java/br/com/logistics/tms/company/infrastructure/jpa/entities/CompanyEntity.java`

**Current (WRONG):**
```java
public Company toCompany() {
    return new Company(
            CompanyId.with(this.id),
            this.name,
            Cnpj.with(this.cnpj),
            CompanyTypes.with(this.companyTypes),
            Configurations.with(this.configuration),
            Collections.emptySet(),  // ❌ ALWAYS EMPTY
            Status.of(this.status),
            Collections.emptySet(),
            Map.of("version", this.version)
    );
}
```

**Fixed:**
```java
public Company toCompany() {
    final Set<Agreement> agreements = this.agreements == null ? Collections.emptySet() :
            this.agreements.stream()
                    .map(AgreementEntity::toAgreement)
                    .collect(Collectors.toSet());
    
    return new Company(
            CompanyId.with(this.id),
            this.name,
            Cnpj.with(this.cnpj),
            CompanyTypes.with(this.companyTypes),
            Configurations.with(this.configuration),
            agreements,  // ✅ RECONSTRUCTED
            Status.of(this.status),
            Collections.emptySet(),
            Map.of("version", this.version)
    );
}
```

---

### Task 3.5: Add AgreementEntity.toAgreement()

**File:** `src/main/java/br/com/logistics/tms/company/infrastructure/jpa/entities/AgreementEntity.java`

```java
public Agreement toAgreement() {
    final Set<AgreementCondition> conditions = this.conditions == null ? Set.of() :
            this.conditions.stream()
                    .map(AgreementConditionEntity::toAgreementCondition)
                    .collect(Collectors.toSet());
    
    return new Agreement(
            AgreementId.with(this.id),
            CompanyId.with(this.from.getId()),
            CompanyId.with(this.to.getId()),
            AgreementType.valueOf(this.relationType),
            Configurations.with(this.configuration),
            conditions,
            this.validFrom,
            this.validTo
    );
}
```

---

### Task 3.6: Add AgreementConditionEntity.of() and .toAgreementCondition()

**File:** `src/main/java/br/com/logistics/tms/company/infrastructure/jpa/entities/AgreementConditionEntity.java`

**Add Lombok annotations:**
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
```

**Add mapping methods:**
```java
public static AgreementConditionEntity of(final AgreementCondition condition, final AgreementEntity agreementEntity) {
    return AgreementConditionEntity.builder()
            .id(condition.agreementConditionId().value())
            .agreement(agreementEntity)
            .conditionType(condition.type().name())
            .conditions(new HashMap<>(condition.conditions()))
            .build();
}

public AgreementCondition toAgreementCondition() {
    return new AgreementCondition(
            AgreementConditionId.with(this.id),
            AgreementConditionType.valueOf(this.conditionType),
            this.conditions
    );
}
```

---

### Task 3.7: Add Repository Query Method

**Interface:** `src/main/java/br/com/logistics/tms/company/application/repositories/CompanyRepository.java`

```java
/**
 * Find company by agreement ID.
 * Used for RemoveAgreementUseCase.
 */
Optional<Company> findCompanyByAgreementId(AgreementId agreementId);
```

**Implementation:** `src/main/java/br/com/logistics/tms/company/infrastructure/repositories/CompanyRepositoryImpl.java`

```java
@Override
public Optional<Company> findCompanyByAgreementId(final AgreementId agreementId) {
    return companyJpaRepository.findByAgreementsId(agreementId.value())
            .map(CompanyEntity::toCompany);
}
```

**JPA Repository:** `src/main/java/br/com/logistics/tms/company/infrastructure/jpa/repositories/CompanyJpaRepository.java`

```java
@Query("SELECT c FROM CompanyEntity c JOIN c.agreements a WHERE a.id = :agreementId")
Optional<CompanyEntity> findByAgreementsId(@Param("agreementId") UUID agreementId);
```

---

## 📋 Plan 002 Updates Required

### New Section: "Persistence Layer (Critical Implementation)"

Add **before** "Domain Layer" section:

```markdown
## 🗄️ Persistence Layer (Critical Implementation)

**Context:** Current code has partial infrastructure (AgreementEntity exists) but NO mapping logic between domain and persistence layers. This section adds the missing cascade persistence.

### Task 3.1: Add @OneToMany to CompanyEntity
- Add `agreements` field with cascade ALL, orphanRemoval true
- Ensures agreements are saved when Company is saved

### Task 3.2: Add Lombok to AgreementEntity
- @Data, @NoArgsConstructor, @AllArgsConstructor, @Builder
- Required for mapping methods

### Task 3.3: Add AgreementEntity.of(Agreement, CompanyEntity)
- Static factory converting Agreement domain → AgreementEntity
- Handles CompanyId → CompanyEntity FK resolution
- Maps conditions via AgreementConditionEntity.of()

### Task 3.4: Add AgreementEntity.toAgreement()
- Reconstructs Agreement domain object from entity
- Maps conditions via AgreementConditionEntity.toAgreementCondition()

### Task 3.5: Update CompanyEntity.of(Company)
- Map company.getAgreements() → Set<AgreementEntity>
- Bidirectional relationship setup

### Task 3.6: Update CompanyEntity.toCompany()
- Reconstruct agreements from agreementEntities
- Currently returns Collections.emptySet() (BUG)

### Task 3.7: Add Lombok to AgreementConditionEntity
- @Data, @NoArgsConstructor, @AllArgsConstructor, @Builder

### Task 3.8: Add AgreementConditionEntity.of(AgreementCondition, AgreementEntity)
- Static factory for condition mapping

### Task 3.9: Add AgreementConditionEntity.toAgreementCondition()
- Reconstruct AgreementCondition domain object

### Task 3.10: Add CompanyRepository.findCompanyByAgreementId()
- Required for RemoveAgreementUseCase
- Query: JOIN agreements WHERE a.id = ?
```

---

### Update "Implementation Order"

**Replace Phase 1 with:**

```markdown
### Phase 1: Persistence Layer (MUST BE FIRST)
1. Task 3.2 — Add Lombok to AgreementEntity
2. Task 3.7 — Add Lombok to AgreementConditionEntity
3. Task 3.8 — Add AgreementConditionEntity.of()
4. Task 3.9 — Add AgreementConditionEntity.toAgreementCondition()
5. Task 3.3 — Add AgreementEntity.of()
6. Task 3.4 — Add AgreementEntity.toAgreement()
7. Task 3.1 — Add @OneToMany to CompanyEntity
8. Task 3.5 — Update CompanyEntity.of() to map agreements
9. Task 3.6 — Update CompanyEntity.toCompany() to reconstruct agreements
10. Task 3.10 — Add CompanyRepository.findCompanyByAgreementId()

### Phase 2: Critical Fixes (Domain Layer)
11. Task 1.3 — Expand AgreementConditionType enum
12. Task 1.4 — Create Agreement factory method
13. Task 4.1 — Create AgreementAdded event
14. Task 4.2 — Create AgreementRemoved event
15. Task 1.1 — Fix Company.addAgreement
16. Task 1.2 — Fix Company.removeAgreement

### Phase 3: Database (Parallel with Phase 2)
17. Migration V10 — Add indexes
18. Migration V11 — Add FK constraints
19. Migration V12 — Add unique constraint

### Phase 4: Use Cases
... (rest unchanged)
```

---

## ✅ Implementation Checklist

**Persistence Layer:**
- [ ] AgreementEntity has Lombok annotations
- [ ] AgreementConditionEntity has Lombok annotations
- [ ] AgreementConditionEntity.of() created
- [ ] AgreementConditionEntity.toAgreementCondition() created
- [ ] AgreementEntity.of() created with FK resolution
- [ ] AgreementEntity.toAgreement() created
- [ ] CompanyEntity has @OneToMany agreements field
- [ ] CompanyEntity.of() maps agreements to entities
- [ ] CompanyEntity.toCompany() reconstructs agreements from entities
- [ ] CompanyRepository.findCompanyByAgreementId() added

**Validation:**
- [ ] CompanyRepositoryImpl.update() saves agreements via cascade
- [ ] CompanyRepositoryImpl.getCompanyById() loads agreements via JOIN
- [ ] Integration test: create company, add agreement, verify in DB
- [ ] Integration test: load company, verify agreements reconstructed

---

## 📝 Additional Notes

### Cascade Behavior

With `cascade = CascadeType.ALL`:
- **INSERT:** New agreements saved when Company saved
- **UPDATE:** Agreement changes persisted when Company updated
- **DELETE:** Agreements deleted when Company deleted (orphanRemoval = true)

### FK Resolution Strategy

**Recommended:** Option 3.3a (Lazy Resolution)
- Use case validates destination company exists BEFORE creating agreement
- JPA manages FK constraint
- No extra queries during persistence

### Performance Implications

**Single Query Load:**
```sql
SELECT c.*, a.*, ac.*
FROM company.company c
LEFT JOIN company.agreement a ON c.id = a.source_company_id
LEFT JOIN company.agreement_condition ac ON a.id = ac.agreement_id
WHERE c.id = ?
```

**With indexes from Plan 002 migrations**, this query is efficient.

---

## 🎯 Conclusion

**Summary:**
- Plan 002 is architecturally sound (Option A: Agreement part of Company aggregate)
- Implementation gap: missing persistence layer (entity mapping)
- Add 10 new tasks (3.1-3.10) to Plan 002 BEFORE domain layer tasks
- Estimated effort: +1 day for persistence layer

**Next Steps:**
1. Leonardo reviews this architecture recommendation
2. If approved, add persistence tasks to Plan 002
3. Execute implementation order: Persistence → Domain → Database → Use Cases → Infrastructure → Tests

**Files Changed:**
- plans/002-company-agreement-implementation.md (add persistence section)
- plans/002-persistence-review.md (this document)

---

**Review Status:** READY FOR LEONARDO APPROVAL  
**Architecture Decision:** Option A (Agreement part of Company aggregate)  
**Implementation Impact:** +10 tasks, +1 day effort
