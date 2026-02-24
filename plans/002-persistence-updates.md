# Plan 002 Updates: Persistence Layer Addition

**Status:** DRAFT  
**Based On:** 002-persistence-review.md  
**Action:** Add these sections to 002-company-agreement-implementation.md

---

## 🎯 Executive Summary

Plan 002 is **missing the persistence layer specification**. The domain layer (Agreement, Company.addAgreement) exists, but there's **NO mapping logic** between domain objects and JPA entities.

**Critical Gap:**
- `CompanyEntity.of(company)` does NOT map agreements
- `CompanyEntity.toCompany()` always returns `Collections.emptySet()` for agreements
- `AgreementEntity` has NO mapping methods (`of()`, `toAgreement()`)

**Impact:** Use cases will compile but **agreements will never be saved to database**.

---

## 📦 New Section to Add: "Persistence Layer"

**Insert this section BEFORE the current "Domain Layer" section:**

```markdown
## 🗄️ Persistence Layer (Critical Implementation)

**Context:** Current code has partial infrastructure (AgreementEntity, AgreementConditionEntity exist) but NO mapping logic between domain and persistence layers. This section adds the missing cascade persistence that enables Agreement saving when Company is saved.

**Architecture Decision:** Agreement remains part of Company aggregate (Option A from architecture review). Single repository (CompanyRepository), single transaction boundary.

---

### Task 3.1: Add @OneToMany Relationship to CompanyEntity

**File:** `src/main/java/br/com/logistics/tms/company/infrastructure/jpa/entities/CompanyEntity.java`

**Add field (after existing fields):**
```java
@OneToMany(mappedBy = "from", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
private Set<AgreementEntity> agreements = new HashSet<>();
```

**Why:** Enables cascade persistence. When Company is saved, agreements are automatically saved/updated/deleted.

---

### Task 3.2: Add Lombok Annotations to AgreementEntity

**File:** `src/main/java/br/com/logistics/tms/company/infrastructure/jpa/entities/AgreementEntity.java`

**Add to class:**
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
```

**Remove:** Manual getters/setters if any exist.

**Why:** Required for mapping methods and builder pattern.

---

### Task 3.3: Add Mapping Methods to AgreementEntity

**File:** `src/main/java/br/com/logistics/tms/company/infrastructure/jpa/entities/AgreementEntity.java`

**Add static factory:**
```java
public static AgreementEntity of(final Agreement agreement, final CompanyEntity fromEntity) {
    final AgreementEntity entity = AgreementEntity.builder()
            .id(agreement.agreementId().value())
            .from(fromEntity)
            .relationType(agreement.type().name())
            .configuration(new HashMap<>(agreement.configurations().value()))
            .validFrom(agreement.validFrom())
            .validTo(agreement.validTo())
            .build();
    
    // Set destination as lazy reference (JPA resolves FK)
    final CompanyEntity destinationRef = new CompanyEntity();
    destinationRef.setId(agreement.to().value());
    entity.setTo(destinationRef);
    
    // Map conditions
    final Set<AgreementConditionEntity> conditionEntities = agreement.conditions().stream()
            .map(condition -> AgreementConditionEntity.of(condition, entity))
            .collect(Collectors.toSet());
    entity.setConditions(conditionEntities);
    
    return entity;
}
```

**Add reconstruction method:**
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

**Why:** Converts between domain (Agreement) and persistence (AgreementEntity) layers.

---

### Task 3.4: Update CompanyEntity.of() to Map Agreements

**File:** `src/main/java/br/com/logistics/tms/company/infrastructure/jpa/entities/CompanyEntity.java`

**Replace current `of()` method:**
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
    
    // Map agreements (NEW)
    final Set<AgreementEntity> agreementEntities = company.getAgreements().stream()
            .map(agreement -> AgreementEntity.of(agreement, entity))
            .collect(Collectors.toSet());
    entity.setAgreements(agreementEntities);
    
    return entity;
}
```

**Why:** Fixes the critical bug where agreements are ignored during persistence.

---

### Task 3.5: Update CompanyEntity.toCompany() to Reconstruct Agreements

**File:** `src/main/java/br/com/logistics/tms/company/infrastructure/jpa/entities/CompanyEntity.java`

**Replace current `toCompany()` method:**
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
            agreements,  // FIXED: was Collections.emptySet()
            Status.of(this.status),
            Collections.emptySet(),
            Map.of("version", this.version)
    );
}
```

**Why:** Fixes the bug where loaded companies always have empty agreement sets.

---

### Task 3.6: Add Lombok to AgreementConditionEntity

**File:** `src/main/java/br/com/logistics/tms/company/infrastructure/jpa/entities/AgreementConditionEntity.java`

**Add to class:**
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
```

---

### Task 3.7: Add Mapping Methods to AgreementConditionEntity

**File:** `src/main/java/br/com/logistics/tms/company/infrastructure/jpa/entities/AgreementConditionEntity.java`

**Add static factory:**
```java
public static AgreementConditionEntity of(final AgreementCondition condition, final AgreementEntity agreementEntity) {
    return AgreementConditionEntity.builder()
            .id(condition.agreementConditionId().value())
            .agreement(agreementEntity)
            .conditionType(condition.type().name())
            .conditions(new HashMap<>(condition.conditions()))
            .build();
}
```

**Add reconstruction method:**
```java
public AgreementCondition toAgreementCondition() {
    return new AgreementCondition(
            AgreementConditionId.with(this.id),
            AgreementConditionType.valueOf(this.conditionType),
            this.conditions
    );
}
```

---

### Task 3.8: Add Repository Query Method

**Interface:** `src/main/java/br/com/logistics/tms/company/application/repositories/CompanyRepository.java`

**Add method:**
```java
/**
 * Find company that owns the specified agreement.
 * Used by RemoveAgreementUseCase to locate source company.
 */
Optional<Company> findCompanyByAgreementId(AgreementId agreementId);
```

**Implementation:** `src/main/java/br/com/logistics/tms/company/infrastructure/repositories/CompanyRepositoryImpl.java`

**Add method:**
```java
@Override
public Optional<Company> findCompanyByAgreementId(final AgreementId agreementId) {
    return companyJpaRepository.findByAgreementsId(agreementId.value())
            .map(CompanyEntity::toCompany);
}
```

**JPA Repository:** `src/main/java/br/com/logistics/tms/company/infrastructure/jpa/repositories/CompanyJpaRepository.java`

**Add method:**
```java
@Query("SELECT c FROM CompanyEntity c JOIN c.agreements a WHERE a.id = :agreementId")
Optional<CompanyEntity> findByAgreementsId(@Param("agreementId") UUID agreementId);
```

**Why:** RemoveAgreementUseCase needs to find which company owns an agreement given only the agreementId.

---

### Task 3.9: Add Integration Test for Agreement Persistence

**File:** `src/test/java/br/com/logistics/tms/company/infrastructure/repositories/CompanyAgreementPersistenceTest.java`

**Test scenario:**
```java
@SpringBootTest
@Testcontainers
class CompanyAgreementPersistenceTest {
    
    @Test
    void shouldPersistAgreementsWhenSavingCompany() {
        // 1. Create two companies
        // 2. Create agreement between them
        // 3. Add agreement to source company
        // 4. Save via companyRepository.update()
        // 5. Clear persistence context
        // 6. Load company again
        // 7. Assert agreements were persisted and reconstructed
        // 8. Verify agreement in database via native query
    }
    
    @Test
    void shouldCascadeDeleteAgreementsWhenDeletingCompany() {
        // Test orphanRemoval behavior
    }
}
```

**Why:** Validates that the persistence layer actually works end-to-end.

---
```

---

## 📋 Updated Implementation Order

**Replace the current "Implementation Order" section with:**

```markdown
## 📋 Implementation Order

Execute in this sequence to maintain clean build state:

### Phase 1: Persistence Layer (MUST BE FIRST)
1. Task 3.2 — Add Lombok to AgreementEntity
2. Task 3.6 — Add Lombok to AgreementConditionEntity
3. Task 3.7 — Add AgreementConditionEntity mapping methods
4. Task 3.3 — Add AgreementEntity mapping methods
5. Task 3.1 — Add @OneToMany to CompanyEntity
6. Task 3.4 — Update CompanyEntity.of() to map agreements
7. Task 3.5 — Update CompanyEntity.toCompany() to reconstruct agreements
8. Task 3.8 — Add CompanyRepository.findCompanyByAgreementId()
9. Task 3.9 — Add integration test for persistence

### Phase 2: Critical Fixes (Domain Layer)
10. Task 1.3 — Expand AgreementConditionType enum
11. Task 1.4 — Create Agreement factory method
12. Task 4.1 — Create AgreementAdded event
13. Task 4.2 — Create AgreementRemoved event
14. Task 1.1 — Fix Company.addAgreement
15. Task 1.2 — Fix Company.removeAgreement

### Phase 3: Database (Parallel with Phase 2)
16. Migration V10 — Add indexes
17. Migration V11 — Add FK constraints
18. Migration V12 — Add unique constraint

### Phase 4: Use Cases
18. Task 5.1 — CreateAgreementUseCase
19. Task 5.2 — GetAgreementsByCompanyUseCase
20. Task 5.3 — RemoveAgreementUseCase

### Phase 5: Infrastructure
21. Task 6.4 — Create DTOs
22. Task 6.1 — CreateAgreementController
23. Task 6.2 — GetAgreementsController
24. Task 6.3 — RemoveAgreementController

### Phase 6: Tests
25. Task 7.1 — Agreement domain tests
26. Task 7.2 — Company domain tests
27. Task 7.3 — Integration tests (create)
28. Task 7.4 — Integration tests (get/remove)

### Phase 7: Manual Verification
29. Create agreement-requests.http file
30. Execute HTTP scenarios
31. Verify events in outbox
32. Verify indexes via EXPLAIN ANALYZE
```

---

## ✅ Validation Checklist

Add to "Success Criteria" section:

```markdown
### Persistence Layer Validation
- [ ] CompanyEntity has @OneToMany agreements field
- [ ] AgreementEntity.of() converts domain → entity
- [ ] AgreementEntity.toAgreement() converts entity → domain
- [ ] AgreementConditionEntity mapping methods exist
- [ ] CompanyEntity.of() maps agreements
- [ ] CompanyEntity.toCompany() reconstructs agreements
- [ ] CompanyRepository.findCompanyByAgreementId() works
- [ ] Integration test: agreements persist when company saved
- [ ] Integration test: agreements load when company loaded
- [ ] Manual test: verify agreement rows in database
```

---

## 🎯 Summary of Changes

| Section | Action | Line Count |
|---------|--------|------------|
| Add "Persistence Layer" | Insert before "Domain Layer" | ~300 lines |
| Update "Implementation Order" | Replace existing section | ~50 lines |
| Update "Success Criteria" | Add persistence checklist | ~10 lines |
| Total addition | | ~360 lines |

**Estimated Effort Impact:** +1 day (from 2-3 days to 3-4 days total)

---

## 📝 Key Architecture Points

**Why Option A (Agreement Part of Company Aggregate):**
1. ✅ Matches current domain design (Company has Set<Agreement>)
2. ✅ Single transaction boundary (Company + agreements saved together)
3. ✅ Simpler implementation (one repository, cascade persistence)
4. ✅ Agreement lifecycle bound to Company (cannot exist independently)
5. ✅ Common query pattern optimized (get agreements for company X)

**Trade-offs Accepted:**
- ❌ Must load full Company to query agreements
- ❌ Cross-company queries require loading multiple companies
- ✅ These are acceptable because most queries are "get agreements for company X"

---

## 🚀 Next Steps

1. **Leonardo reviews** this document and 002-persistence-review.md
2. **If approved:**
   - Copy "Persistence Layer" section into 002-company-agreement-implementation.md
   - Replace "Implementation Order" section
   - Update "Success Criteria" section
3. **Start implementation** following updated plan
4. **Verify persistence** via integration test before proceeding to use cases

---

**Document Status:** READY FOR REVIEW  
**Approval Required:** Leonardo Moreira  
**Impact:** +9 tasks, +1 day effort, critical architecture fix
