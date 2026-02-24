# Plan 002: Company Agreement Implementation

**Status:** `READY FOR IMPLEMENTATION`  
**Owner:** Leonardo Moreira  
**Created:** 2026-02-24  
**Updated:** 2026-02-24 (Consolidated from 6 review documents)  
**Target:** Enable company-to-company agreements with configurable conditions (discounts, SLA, updates)

---

## 🎯 Overview

### Business Goal
Enable companies in the TMS to establish formal agreements with each other, defining relationship types (DELIVERS_WITH, USES_PROVIDER) and configurable conditions such as discount percentages and delivery SLA requirements. Support full CRUD operations including updates to existing agreements.

### Success Criteria
- ✅ REST API endpoints functional (POST, GET, PUT, DELETE)
- ✅ Persistence layer working (agreements saved to database via cascade)
- ✅ Update operations working (extend validity, change conditions)
- ✅ Domain events generated (AgreementAdded, AgreementRemoved, AgreementUpdated)
- ✅ Immutability pattern enforced (all update methods return new instances)
- ✅ Business validations working (self-reference, duplicates, overlaps)
- ✅ Database optimizations in place (indexes, FK constraints)
- ✅ All tests passing (domain, integration, persistence)
- ✅ HTTP request scenarios executable end-to-end

### Scope

**✅ IN SCOPE:**
- Persistence layer implementation (Agreement ↔ AgreementEntity mapping)
- Company aggregate immutability fixes
- Agreement domain events (Added, Removed, Updated)
- AgreementConditionType expansion (DISCOUNT_PERCENTAGE, DELIVERY_SLA_DAYS)
- Full CRUD REST endpoints (create, read, update, delete)
- Agreement lifecycle methods (updateValidTo, updateConditions)
- Overlapping agreement detection
- Database indexes and constraints
- Comprehensive test coverage
- Complete HTTP request scenarios

**❌ OUT OF SCOPE (Future Iterations):**
- Agreement approval workflows
- Agreement templates
- Cross-module synchronization
- Agreement condition enforcement in pricing
- Audit trail beyond domain events
- Agreement versioning/history

---

## 🗄️ Phase 1: Persistence Layer (CRITICAL — MUST BE FIRST)

**Context:** Agreement is part of the Company aggregate. Current code has JPA entities (AgreementEntity, AgreementConditionEntity) but NO mapping logic between domain and persistence layers. This phase implements cascade persistence so agreements are saved when Company is saved.

**Architecture Decision:** Agreement remains within Company aggregate boundary (single repository, single transaction).

---

### Task 1.1: Add Lombok Annotations to AgreementEntity

**File:** `src/main/java/br/com/logistics/tms/company/infrastructure/jpa/entities/AgreementEntity.java`

**Add to class:**
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
```

**Remove:** Manual getters/setters if any exist.

---

### Task 1.2: Add Lombok Annotations to AgreementConditionEntity

**File:** `src/main/java/br/com/logistics/tms/company/infrastructure/jpa/entities/AgreementConditionEntity.java`

**Add to class:**
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
```

---

### Task 1.3: Add Mapping Methods to AgreementConditionEntity

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

### Task 1.4: Add Mapping Methods to AgreementEntity

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

---

### Task 1.5: Add @OneToMany Relationship to CompanyEntity

**File:** `src/main/java/br/com/logistics/tms/company/infrastructure/jpa/entities/CompanyEntity.java`

**Add field (after existing fields):**
```java
@OneToMany(mappedBy = "from", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
private Set<AgreementEntity> agreements = new HashSet<>();
```

**Why:** Enables cascade persistence. When Company is saved, agreements are automatically saved/updated/deleted.

---

### Task 1.6: Update CompanyEntity.of() to Map Agreements

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

---

### Task 1.7: Update CompanyEntity.toCompany() to Reconstruct Agreements

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

---

### Task 1.8: Add Repository Query Method

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

---

### Task 1.9: Integration Test for Agreement Persistence

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

---

## 🚨 Phase 2: Pre-Implementation Fixes (Domain Layer)

These fixes MUST be applied before implementing new features. They address architectural violations discovered during analysis.

### Task 1.1: Fix Company.addAgreement Immutability Violation

**Problem:** Current implementation mutates the `agreements` Set directly, violating TMS immutability principle.

**Current Code (WRONG):**
```java
public void addAgreement(final Agreement agreement) {
    if (agreements.contains(agreement)) {
        throw new ValidationException("Agreement already exists for this company");
    }
    agreements.add(agreement);  // ❌ MUTATION
}
```

**Required Fix:**
```java
public Company addAgreement(final Agreement agreement) {
    if (agreements.contains(agreement)) {
        throw new ValidationException("Agreement already exists for this company");
    }
    
    if (agreement.from().equals(this.companyId)) {
        throw new ValidationException("Company cannot create agreement with itself");
    }
    
    // Check for duplicate active agreements
    final boolean duplicateExists = agreements.stream()
        .anyMatch(a -> a.to().equals(agreement.to()) && 
                      a.type().equals(agreement.type()) && 
                      a.isActive());
    if (duplicateExists) {
        throw new ValidationException("Active agreement already exists");
    }
    
    final Set<Agreement> updatedAgreements = new HashSet<>(this.agreements);
    updatedAgreements.add(agreement);
    
    final Company updated = new Company(
        this.companyId,
        this.name,
        this.cnpj,
        this.companyTypes,
        this.configurations,
        updatedAgreements,
        this.status,
        this.getDomainEvents(),
        this.getPersistentMetadata()
    );
    
    updated.placeDomainEvent(new AgreementAdded(
        this.companyId.value(),
        agreement.agreementId().value(),
        agreement.to().value(),
        agreement.type().name()
    ));
    
    return updated;
}
```

**Impact:** All use cases calling `Company.addAgreement` must be updated to capture returned instance.

---

### Task 1.2: Fix Company.removeAgreement Immutability Violation

**Problem:** Current implementation mutates the `agreements` Set directly.

**Required Fix:**
```java
public Company removeAgreement(final AgreementId agreementId) {
    final Agreement agreementToRemove = agreements.stream()
        .filter(a -> a.agreementId().equals(agreementId))
        .findFirst()
        .orElseThrow(() -> new ValidationException("Agreement not found"));
    
    final Set<Agreement> updatedAgreements = new HashSet<>(this.agreements);
    updatedAgreements.remove(agreementToRemove);
    
    final Company updated = new Company(
        this.companyId,
        this.name,
        this.cnpj,
        this.companyTypes,
        this.configurations,
        updatedAgreements,
        this.status,
        this.getDomainEvents(),
        this.getPersistentMetadata()
    );
    
    updated.placeDomainEvent(new AgreementRemoved(
        this.companyId.value(),
        agreementToRemove.agreementId().value(),
        agreementToRemove.to().value()
    ));
    
    return updated;
}
```

---

### Task 1.3: Expand AgreementConditionType Enum

**Location:** `src/main/java/br/com/logistics/tms/company/domain/AgreementConditionType.java`

**Required Addition:**
```java
public enum AgreementConditionType {
    USES_PROVIDER,
    DISCOUNT_PERCENTAGE,    // NEW: Percentage discount
    DELIVERY_SLA_DAYS       // NEW: Max delivery time in days
}
```

---

### Task 1.4: Create Agreement Factory Method

**Location:** `src/main/java/br/com/logistics/tms/company/domain/Agreement.java`

```java
public static Agreement createAgreement(
    final CompanyId from,
    final CompanyId to,
    final AgreementType type,
    final Map<String, Object> configuration,
    final Set<AgreementCondition> conditions,
    final Instant validFrom,
    final Instant validTo
) {
    if (from.equals(to)) {
        throw new ValidationException("Agreement source and destination must be different");
    }
    
    return new Agreement(
        AgreementId.unique(),
        from,
        to,
        type,
        Configurations.with(configuration),
        conditions,
        validFrom,
        validTo
    );
}
```

## 🚨 Phase 2: Pre-Implementation Fixes (Domain Layer)

These fixes MUST be applied before implementing use cases. They address immutability violations discovered during analysis.

---

### Task 2.1: Expand AgreementConditionType Enum

**Location:** `src/main/java/br/com/logistics/tms/company/domain/AgreementConditionType.java`

**Required Addition:**
```java
public enum AgreementConditionType {
    USES_PROVIDER,
    DISCOUNT_PERCENTAGE,    // NEW: Percentage discount
    DELIVERY_SLA_DAYS       // NEW: Max delivery time in days
}
```

---

### Task 2.2: Create Agreement Factory Method

**Location:** `src/main/java/br/com/logistics/tms/company/domain/Agreement.java`

```java
public static Agreement createAgreement(
    final CompanyId from,
    final CompanyId to,
    final AgreementType type,
    final Map<String, Object> configuration,
    final Set<AgreementCondition> conditions,
    final Instant validFrom,
    final Instant validTo
) {
    if (from.equals(to)) {
        throw new ValidationException("Agreement source and destination must be different");
    }
    
    return new Agreement(
        AgreementId.unique(),
        from,
        to,
        type,
        Configurations.with(configuration),
        conditions,
        validFrom,
        validTo
    );
}
```

---

### Task 2.3: Add Agreement Lifecycle Methods

**Location:** `src/main/java/br/com/logistics/tms/company/domain/Agreement.java`

```java
public Agreement updateValidTo(final Instant newValidTo) {
    if (newValidTo != null && newValidTo.isBefore(this.validFrom)) {
        throw new ValidationException("Valid to must be after valid from");
    }
    
    return new Agreement(
        this.agreementId,
        this.from,
        this.to,
        this.type,
        this.configurations,
        this.conditions,
        this.validFrom,
        newValidTo
    );
}

public Agreement updateConditions(final Set<AgreementCondition> newConditions) {
    if (newConditions == null || newConditions.isEmpty()) {
        throw new ValidationException("Agreement must have at least one condition");
    }
    
    return new Agreement(
        this.agreementId,
        this.from,
        this.to,
        this.type,
        this.configurations,
        newConditions,
        this.validFrom,
        this.validTo
    );
}

public boolean overlapsWith(final Agreement other) {
    // Same destination and type?
    if (!this.to.equals(other.to) || !this.type.equals(other.type)) {
        return false;
    }
    
    // Check date range overlap
    final Instant thisStart = this.validFrom;
    final Instant thisEnd = this.validTo != null ? this.validTo : Instant.MAX;
    final Instant otherStart = other.validFrom;
    final Instant otherEnd = other.validTo != null ? other.validTo : Instant.MAX;
    
    return thisStart.isBefore(otherEnd) && otherStart.isBefore(thisEnd);
}
```

---

### Task 2.4: Fix Company.addAgreement Immutability Violation

**Problem:** Current implementation mutates the `agreements` Set directly, violating TMS immutability principle.

**Required Fix:**
```java
public Company addAgreement(final Agreement agreement) {
    if (agreements.contains(agreement)) {
        throw new ValidationException("Agreement already exists for this company");
    }
    
    if (agreement.from().equals(this.companyId)) {
        throw new ValidationException("Company cannot create agreement with itself");
    }
    
    // Check for overlapping active agreements
    final boolean overlappingExists = agreements.stream()
        .filter(Agreement::isActive)
        .anyMatch(a -> a.overlapsWith(agreement));
    if (overlappingExists) {
        throw new ValidationException("Overlapping active agreement exists");
    }
    
    final Set<Agreement> updatedAgreements = new HashSet<>(this.agreements);
    updatedAgreements.add(agreement);
    
    final Company updated = new Company(
        this.companyId,
        this.name,
        this.cnpj,
        this.companyTypes,
        this.configurations,
        updatedAgreements,
        this.status,
        this.getDomainEvents(),
        this.getPersistentMetadata()
    );
    
    updated.placeDomainEvent(new AgreementAdded(
        this.companyId.value(),
        agreement.agreementId().value(),
        agreement.to().value(),
        agreement.type().name()
    ));
    
    return updated;
}
```

---

### Task 2.5: Fix Company.removeAgreement Immutability Violation

**Required Fix:**
```java
public Company removeAgreement(final AgreementId agreementId) {
    final Agreement agreementToRemove = agreements.stream()
        .filter(a -> a.agreementId().equals(agreementId))
        .findFirst()
        .orElseThrow(() -> new ValidationException("Agreement not found"));
    
    final Set<Agreement> updatedAgreements = new HashSet<>(this.agreements);
    updatedAgreements.remove(agreementToRemove);
    
    final Company updated = new Company(
        this.companyId,
        this.name,
        this.cnpj,
        this.companyTypes,
        this.configurations,
        updatedAgreements,
        this.status,
        this.getDomainEvents(),
        this.getPersistentMetadata()
    );
    
    updated.placeDomainEvent(new AgreementRemoved(
        this.companyId.value(),
        agreementToRemove.agreementId().value(),
        agreementToRemove.to().value()
    ));
    
    return updated;
}
```

---

### Task 2.6: Add Company.updateAgreement Method

**Location:** `src/main/java/br/com/logistics/tms/company/domain/Company.java`

```java
public Company updateAgreement(final AgreementId agreementId, final Agreement updatedAgreement) {
    final Agreement existingAgreement = agreements.stream()
        .filter(a -> a.agreementId().equals(agreementId))
        .findFirst()
        .orElseThrow(() -> new ValidationException("Agreement not found"));
    
    // Check for overlaps with OTHER agreements
    final boolean overlappingExists = agreements.stream()
        .filter(a -> !a.agreementId().equals(agreementId))
        .filter(Agreement::isActive)
        .anyMatch(a -> a.overlapsWith(updatedAgreement));
    if (overlappingExists) {
        throw new ValidationException("Update would create overlapping agreement");
    }
    
    final Set<Agreement> updatedAgreements = new HashSet<>(this.agreements);
    updatedAgreements.remove(existingAgreement);
    updatedAgreements.add(updatedAgreement);
    
    final Company updated = new Company(
        this.companyId,
        this.name,
        this.cnpj,
        this.companyTypes,
        this.configurations,
        updatedAgreements,
        this.status,
        this.getDomainEvents(),
        this.getPersistentMetadata()
    );
    
    // Detect what changed
    String fieldChanged = "unknown";
    String oldValue = "";
    String newValue = "";
    
    if (!existingAgreement.validTo().equals(updatedAgreement.validTo())) {
        fieldChanged = "validTo";
        oldValue = existingAgreement.validTo() != null ? existingAgreement.validTo().toString() : "null";
        newValue = updatedAgreement.validTo() != null ? updatedAgreement.validTo().toString() : "null";
    } else if (!existingAgreement.conditions().equals(updatedAgreement.conditions())) {
        fieldChanged = "conditions";
        oldValue = String.valueOf(existingAgreement.conditions().size());
        newValue = String.valueOf(updatedAgreement.conditions().size());
    }
    
    updated.placeDomainEvent(new AgreementUpdated(
        this.companyId.value(),
        agreementId.value(),
        fieldChanged,
        oldValue,
        newValue
    ));
    
    return updated;
}
```

---

## 📦 Phase 3: Database Migrations

### Migration V10: Add Indexes

**File:** `infra/database/migration/V10__add_indexes_to_agreement.sql`

```sql
CREATE INDEX idx_agreement_source_company 
ON company.agreement(source_company_id) 
WHERE deleted_at IS NULL;

CREATE INDEX idx_agreement_destination_company 
ON company.agreement(destination_company_id) 
WHERE deleted_at IS NULL;

CREATE INDEX idx_agreement_relation_type 
ON company.agreement(relation_type) 
WHERE deleted_at IS NULL;

CREATE INDEX idx_agreement_active 
ON company.agreement(source_company_id, destination_company_id, relation_type, valid_from, valid_to) 
WHERE deleted_at IS NULL;

CREATE INDEX idx_agreement_condition_agreement 
ON company.agreement_condition(agreement_id) 
WHERE deleted_at IS NULL;
```

---

### Migration V11: Add FK Constraints

**File:** `infra/database/migration/V11__add_fk_constraints_to_agreement.sql`

```sql
ALTER TABLE company.agreement 
ADD CONSTRAINT fk_agreement_source_company 
FOREIGN KEY (source_company_id) 
REFERENCES company.company(id) 
ON DELETE RESTRICT;

ALTER TABLE company.agreement 
ADD CONSTRAINT fk_agreement_destination_company 
FOREIGN KEY (destination_company_id) 
REFERENCES company.company(id) 
ON DELETE RESTRICT;

ALTER TABLE company.agreement_condition 
ADD CONSTRAINT fk_condition_agreement 
FOREIGN KEY (agreement_id) 
REFERENCES company.agreement(id) 
ON DELETE CASCADE;
```

---

### Migration V12: Add Unique Constraint

**File:** `infra/database/migration/V12__add_unique_constraint_active_agreements.sql`

```sql
CREATE UNIQUE INDEX idx_agreement_unique_active 
ON company.agreement(source_company_id, destination_company_id, relation_type) 
WHERE deleted_at IS NULL 
  AND valid_to IS NULL;
```

---

## 🏗️ Phase 4: Domain Layer (Events)

### Task 4.1: Create AgreementAdded Domain Event

**Location:** `src/main/java/br/com/logistics/tms/company/domain/AgreementAdded.java`

```java
package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.AbstractDomainEvent;
import br.com.logistics.tms.commons.domain.Id;

import java.time.Instant;
import java.util.UUID;

public class AgreementAdded extends AbstractDomainEvent {
    private final UUID sourceCompanyId;
    private final UUID agreementId;
    private final UUID destinationCompanyId;
    private final String agreementType;

    public AgreementAdded(final UUID sourceCompanyId, 
                         final UUID agreementId,
                         final UUID destinationCompanyId,
                         final String agreementType) {
        super(Id.unique(), sourceCompanyId, Instant.now());
        this.sourceCompanyId = sourceCompanyId;
        this.agreementId = agreementId;
        this.destinationCompanyId = destinationCompanyId;
        this.agreementType = agreementType;
    }

    public UUID getSourceCompanyId() { return sourceCompanyId; }
    public UUID getAgreementId() { return agreementId; }
    public UUID getDestinationCompanyId() { return destinationCompanyId; }
    public String getAgreementType() { return agreementType; }
}
```

---

### Task 4.2: Create AgreementRemoved Domain Event

**Location:** `src/main/java/br/com/logistics/tms/company/domain/AgreementRemoved.java`

```java
package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.AbstractDomainEvent;
import br.com.logistics.tms.commons.domain.Id;

import java.time.Instant;
import java.util.UUID;

public class AgreementRemoved extends AbstractDomainEvent {
    private final UUID sourceCompanyId;
    private final UUID agreementId;
    private final UUID destinationCompanyId;

    public AgreementRemoved(final UUID sourceCompanyId, 
                           final UUID agreementId,
                           final UUID destinationCompanyId) {
        super(Id.unique(), sourceCompanyId, Instant.now());
        this.sourceCompanyId = sourceCompanyId;
        this.agreementId = agreementId;
        this.destinationCompanyId = destinationCompanyId;
    }

    public UUID getSourceCompanyId() { return sourceCompanyId; }
    public UUID getAgreementId() { return agreementId; }
    public UUID getDestinationCompanyId() { return destinationCompanyId; }
}
```

---

### Task 4.3: Create AgreementUpdated Domain Event

**Location:** `src/main/java/br/com/logistics/tms/company/domain/AgreementUpdated.java`

```java
package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.AbstractDomainEvent;
import br.com.logistics.tms.commons.domain.Id;

import java.time.Instant;
import java.util.UUID;

public class AgreementUpdated extends AbstractDomainEvent {
    private final UUID sourceCompanyId;
    private final UUID agreementId;
    private final String fieldChanged;
    private final String oldValue;
    private final String newValue;

    public AgreementUpdated(final UUID sourceCompanyId, 
                           final UUID agreementId,
                           final String fieldChanged,
                           final String oldValue,
                           final String newValue) {
        super(Id.unique(), sourceCompanyId, Instant.now());
        this.sourceCompanyId = sourceCompanyId;
        this.agreementId = agreementId;
        this.fieldChanged = fieldChanged;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public UUID getSourceCompanyId() { return sourceCompanyId; }
    public UUID getAgreementId() { return agreementId; }
    public String getFieldChanged() { return fieldChanged; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }
}
```

---

## 💼 Phase 5: Application Layer (Use Cases)

### Task 5.1: CreateAgreementUseCase

**Location:** `src/main/java/br/com/logistics/tms/company/application/usecases/CreateAgreementUseCase.java`

```java
@DomainService
@Cqrs(DatabaseRole.WRITE)
public class CreateAgreementUseCase implements UseCase<Input, Output> {

    private final CompanyRepository companyRepository;

    public CreateAgreementUseCase(final CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public Output execute(final Input input) {
        final Company sourceCompany = companyRepository.getCompanyById(new CompanyId(input.sourceCompanyId()))
            .orElseThrow(() -> new NotFoundException("Source company not found"));
        
        final Company destinationCompany = companyRepository.getCompanyById(new CompanyId(input.destinationCompanyId()))
            .orElseThrow(() -> new NotFoundException("Destination company not found"));
        
        final Agreement agreement = Agreement.createAgreement(
            sourceCompany.getCompanyId(),
            destinationCompany.getCompanyId(),
            input.type(),
            input.configuration(),
            input.conditions(),
            input.validFrom(),
            input.validTo()
        );
        
        final Company updatedCompany = sourceCompany.addAgreement(agreement);
        companyRepository.update(updatedCompany);
        
        return new Output(
            agreement.agreementId().value(),
            sourceCompany.getCompanyId().value(),
            destinationCompany.getCompanyId().value(),
            agreement.type().name()
        );
    }

    public record Input(
        UUID sourceCompanyId,
        UUID destinationCompanyId,
        AgreementType type,
        Map<String, Object> configuration,
        Set<AgreementCondition> conditions,
        Instant validFrom,
        Instant validTo
    ) {}

    public record Output(
        UUID agreementId,
        UUID sourceCompanyId,
        UUID destinationCompanyId,
        String agreementType
    ) {}
}
```

---

### Task 5.2: GetAgreementsByCompanyUseCase

**Location:** `src/main/java/br/com/logistics/tms/company/application/usecases/GetAgreementsByCompanyUseCase.java`

```java
@DomainService
@Cqrs(DatabaseRole.READ)
public class GetAgreementsByCompanyUseCase implements UseCase<Input, Output> {

    private final CompanyRepository companyRepository;

    public GetAgreementsByCompanyUseCase(final CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public Output execute(final Input input) {
        final Company company = companyRepository.getCompanyById(new CompanyId(input.companyId()))
            .orElseThrow(() -> new NotFoundException("Company not found"));
        
        final List<AgreementView> views = company.getAgreements().stream()
            .map(a -> new AgreementView(
                a.agreementId().value(),
                a.from().value(),
                a.to().value(),
                a.type().name(),
                a.conditions().size(),
                a.validFrom(),
                a.validTo(),
                a.isActive()
            ))
            .toList();
        
        return new Output(company.getCompanyId().value(), views);
    }

    public record Input(UUID companyId) {}
    public record Output(UUID companyId, List<AgreementView> agreements) {}
    public record AgreementView(
        UUID agreementId, UUID from, UUID to, String type,
        int conditionCount, Instant validFrom, Instant validTo, boolean isActive
    ) {}
}
```

---

### Task 5.3: RemoveAgreementUseCase

**Location:** `src/main/java/br/com/logistics/tms/company/application/usecases/RemoveAgreementUseCase.java`

```java
@DomainService
@Cqrs(DatabaseRole.WRITE)
public class RemoveAgreementUseCase implements UseCase<Input, Output> {

    private final CompanyRepository companyRepository;

    public RemoveAgreementUseCase(final CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public Output execute(final Input input) {
        final AgreementId agreementId = new AgreementId(input.agreementId());
        
        final Company company = companyRepository.findCompanyByAgreementId(agreementId)
            .orElseThrow(() -> new NotFoundException("Agreement not found"));
        
        final Company updatedCompany = company.removeAgreement(agreementId);
        companyRepository.update(updatedCompany);
        
        return new Output(agreementId.value(), company.getCompanyId().value());
    }

    public record Input(UUID agreementId) {}
    public record Output(UUID agreementId, UUID companyId) {}
}
```

**Note:** Requires new repository method `findCompanyByAgreementId(AgreementId)`.

---

### Task 5.4: UpdateAgreementUseCase

**Location:** `src/main/java/br/com/logistics/tms/company/application/usecases/UpdateAgreementUseCase.java`

```java
@DomainService
@Cqrs(DatabaseRole.WRITE)
public class UpdateAgreementUseCase implements UseCase<Input, Output> {

    private final CompanyRepository companyRepository;

    public UpdateAgreementUseCase(final CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public Output execute(final Input input) {
        final AgreementId agreementId = new AgreementId(input.agreementId());
        
        final Company company = companyRepository.findCompanyByAgreementId(agreementId)
            .orElseThrow(() -> new NotFoundException("Agreement not found"));
        
        final Agreement existingAgreement = company.getAgreements().stream()
            .filter(a -> a.agreementId().equals(agreementId))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Agreement not found"));
        
        // Apply updates
        Agreement updatedAgreement = existingAgreement;
        
        if (input.validTo() != null) {
            updatedAgreement = updatedAgreement.updateValidTo(input.validTo());
        }
        
        if (input.conditions() != null && !input.conditions().isEmpty()) {
            updatedAgreement = updatedAgreement.updateConditions(input.conditions());
        }
        
        final Company updatedCompany = company.updateAgreement(agreementId, updatedAgreement);
        companyRepository.update(updatedCompany);
        
        return new Output(agreementId.value(), "Agreement updated successfully");
    }

    public record Input(
        UUID agreementId,
        Instant validTo,
        Set<AgreementCondition> conditions
    ) {}

    public record Output(UUID agreementId, String message) {}
}
```

---

### Task 5.5: GetAgreementByIdUseCase

**Location:** `src/main/java/br/com/logistics/tms/company/application/usecases/GetAgreementByIdUseCase.java`

```java
@DomainService
@Cqrs(DatabaseRole.READ)
public class GetAgreementByIdUseCase implements UseCase<Input, Output> {

    private final CompanyRepository companyRepository;

    public GetAgreementByIdUseCase(final CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public Output execute(final Input input) {
        final AgreementId agreementId = new AgreementId(input.agreementId());
        
        final Company company = companyRepository.findCompanyByAgreementId(agreementId)
            .orElseThrow(() -> new NotFoundException("Agreement not found"));
        
        final Agreement agreement = company.getAgreements().stream()
            .filter(a -> a.agreementId().equals(agreementId))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Agreement not found"));
        
        return new Output(
            agreement.agreementId().value(),
            agreement.from().value(),
            agreement.to().value(),
            agreement.type().name(),
            agreement.conditions(),
            agreement.validFrom(),
            agreement.validTo(),
            agreement.isActive()
        );
    }

    public record Input(UUID agreementId) {}
    public record Output(
        UUID agreementId,
        UUID sourceCompanyId,
        UUID destinationCompanyId,
        String type,
        Set<AgreementCondition> conditions,
        Instant validFrom,
        Instant validTo,
        boolean isActive
    ) {}
}
```

---

## 🔌 Phase 6: Infrastructure Layer (Controllers & DTOs)

### Task 6.1-6.5: Controllers

Create five controllers following TMS pattern (RestUseCaseExecutor + DefaultRestPresenter):
- **CreateAgreementController** — POST /companies/{id}/agreements
- **GetAgreementsController** — GET /companies/{id}/agreements
- **GetAgreementController** — GET /agreements/{id}
- **UpdateAgreementController** — PUT /agreements/{id}
- **RemoveAgreementController** — DELETE /agreements/{id}

**Pattern (example for CreateAgreementController):**
```java
@RestController
@RequestMapping("companies/{companyId}/agreements")
@Cqrs(DatabaseRole.WRITE)
public class CreateAgreementController {
    private final CreateAgreementUseCase useCase;
    private final DefaultRestPresenter presenter;
    private final RestUseCaseExecutor executor;
    
    @PostMapping
    public Object create(@PathVariable UUID companyId, @RequestBody CreateAgreementDTO dto) {
        return executor
            .from(useCase)
            .withInput(dto.toInput(companyId))
            .mapOutputTo(AgreementResponseDTO.class)
            .presentWith(output -> presenter.present(output, HttpStatus.CREATED.value()))
            .execute();
    }
}
```

**UpdateAgreementController pattern:**
```java
@RestController
@RequestMapping("agreements")
@Cqrs(DatabaseRole.WRITE)
public class UpdateAgreementController {
    private final UpdateAgreementUseCase useCase;
    private final DefaultRestPresenter presenter;
    private final RestUseCaseExecutor executor;
    
    @PutMapping("/{agreementId}")
    public Object update(@PathVariable UUID agreementId, @RequestBody UpdateAgreementDTO dto) {
        return executor
            .from(useCase)
            .withInput(dto.toInput(agreementId))
            .mapOutputTo(AgreementUpdateResponseDTO.class)
            .presentWith(output -> presenter.present(output, HttpStatus.OK.value()))
            .execute();
    }
}
```

---

### Task 6.6: Create DTOs

- **CreateAgreementDTO** — Request body with destinationCompanyId, type, configuration, conditions, validFrom, validTo
- **UpdateAgreementDTO** — Request body with validTo, conditions
- **AgreementConditionDTO** — Condition type + conditions map
- **AgreementResponseDTO** — Response with agreementId, sourceCompanyId, destinationCompanyId, agreementType
- **AgreementUpdateResponseDTO** — Response with agreementId, message
- **AgreementDetailResponseDTO** — Full agreement details for GET by ID
- **AgreementsListResponseDTO** — Response with companyId + list of agreement views

---

## 🧪 Phase 7: Tests

### Task 7.1: Domain Tests - Agreement Creation

Test Agreement factory method, self-reference validation, date range validation.

### Task 7.2: Domain Tests - Company Agreement Methods

Test addAgreement/removeAgreement immutability, event generation, duplicate prevention.

### Task 7.3: Domain Tests - Agreement Lifecycle Methods

Test `Agreement.updateValidTo()`, `Agreement.updateConditions()`, `Agreement.overlapsWith()` edge cases.

---

### Task 7.4: Domain Tests - Company Update Methods

Test `Company.updateAgreement()` immutability, event generation, overlap detection.

---

### Task 7.5-7.7: Integration Tests

Full REST → Use Case → Repository flow for create, get, update, and remove operations.

**Key Assertions:**
- Status codes (201 Created, 200 OK, 204 No Content)
- Response body structure
- Event generation (verify outbox table)
- Business validations (self-reference, duplicates, overlaps)
- Persistence verification (agreements saved and loaded correctly)

---

## 📄 Phase 8: HTTP Request Scenarios

**File:** `src/main/resources/company/agreement-requests.http`

### Scenario 1: Shoppe → Loggi (10% Discount)
```http
@server = http://localhost:8080

### 1. Create Shoppe Company
POST {{server}}/companies
Content-Type: application/json

{
  "name": "Shoppe Marketplace",
  "cnpj": "12345678901234",
  "types": ["MARKETPLACE"],
  "configuration": {"website": "https://shoppe.com.br"}
}

> {% client.global.set("shoppeId", response.body.companyId); %}

### 2. Create Loggi Company
POST {{server}}/companies
Content-Type: application/json

{
  "name": "Loggi Logistics",
  "cnpj": "98765432109876",
  "types": ["LOGISTICS_PROVIDER"],
  "configuration": {"coverage": "National"}
}

> {% client.global.set("loggiId", response.body.companyId); %}

### 3. Create Agreement
POST {{server}}/companies/{{shoppeId}}/agreements
Content-Type: application/json

{
  "destinationCompanyId": "{{loggiId}}",
  "type": "DELIVERS_WITH",
  "configuration": {"priority": "standard"},
  "conditions": [
    {
      "type": "DISCOUNT_PERCENTAGE",
      "conditions": {"percentage": 10.0}
    }
  ],
  "validFrom": "2026-02-24T00:00:00Z",
  "validTo": null
}

> {% client.global.set("shoppeLoggiAgreementId", response.body.agreementId); %}
```

### Scenario 2: Biquelo → Loggi (30% + SLA)
```http
### 4. Create Biquelo Company
POST {{server}}/companies
Content-Type: application/json

{
  "name": "Biquelo Electronics",
  "cnpj": "11122233344455",
  "types": ["SELLER"],
  "configuration": {"category": "Electronics"}
}

> {% client.global.set("biqueloId", response.body.companyId); %}

### 5. Create Agreement with Better Terms
POST {{server}}/companies/{{biqueloId}}/agreements
Content-Type: application/json

{
  "destinationCompanyId": "{{loggiId}}",
  "type": "DELIVERS_WITH",
  "configuration": {"priority": "express"},
  "conditions": [
    {
      "type": "DISCOUNT_PERCENTAGE",
      "conditions": {"percentage": 30.0}
    },
    {
      "type": "DELIVERY_SLA_DAYS",
      "conditions": {"maxDays": 1}
    }
  ],
  "validFrom": "2026-02-24T00:00:00Z",
  "validTo": null
}

> {% client.global.set("biqueloLoggiAgreementId", response.body.agreementId); %}
```

### Scenario 3: Update Operations
```http
### 6. Get Agreement by ID
GET {{server}}/agreements/{{shoppeLoggiAgreementId}}

### 7. Extend Validity to 2027
PUT {{server}}/agreements/{{shoppeLoggiAgreementId}}
Content-Type: application/json

{
  "validTo": "2027-12-31T23:59:59Z",
  "conditions": [
    {
      "type": "DISCOUNT_PERCENTAGE",
      "conditions": {"percentage": 10.0}
    }
  ]
}

### 8. Increase Discount to 15%
PUT {{server}}/agreements/{{shoppeLoggiAgreementId}}
Content-Type: application/json

{
  "validTo": "2027-12-31T23:59:59Z",
  "conditions": [
    {
      "type": "DISCOUNT_PERCENTAGE",
      "conditions": {"percentage": 15.0}
    }
  ]
}

### 9. Terminate Biquelo Agreement Early
PUT {{server}}/agreements/{{biqueloLoggiAgreementId}}
Content-Type: application/json

{
  "validTo": "2026-02-24T23:59:59Z",
  "conditions": [
    {
      "type": "DISCOUNT_PERCENTAGE",
      "conditions": {"percentage": 30.0}
    },
    {
      "type": "DELIVERY_SLA_DAYS",
      "conditions": {"maxDays": 1}
    }
  ]
}
```

### Scenario 4: Query and Verify
```http
### 10. Get Shoppe Agreements
GET {{server}}/companies/{{shoppeId}}/agreements

### 11. Get Biquelo Agreements
GET {{server}}/companies/{{biqueloId}}/agreements

### 12. Get Updated Agreement Details
GET {{server}}/agreements/{{shoppeLoggiAgreementId}}
```

### Scenario 5: Delete Operations
```http
### 13. Remove Shoppe Agreement
DELETE {{server}}/agreements/{{shoppeLoggiAgreementId}}

### 14. Verify Removal
GET {{server}}/companies/{{shoppeId}}/agreements
```

### Scenario 6: Negative Tests
```http
### 15. Duplicate Agreement (should fail 400)
POST {{server}}/companies/{{shoppeId}}/agreements
Content-Type: application/json

{
  "destinationCompanyId": "{{loggiId}}",
  "type": "DELIVERS_WITH",
  "configuration": {"priority": "standard"},
  "conditions": [
    {
      "type": "DISCOUNT_PERCENTAGE",
      "conditions": {"percentage": 10.0}
    }
  ],
  "validFrom": "2026-02-24T00:00:00Z",
  "validTo": null
}

### 16. Self-Agreement (should fail 400)
POST {{server}}/companies/{{shoppeId}}/agreements
Content-Type: application/json

{
  "destinationCompanyId": "{{shoppeId}}",
  "type": "DELIVERS_WITH",
  "configuration": {},
  "conditions": [],
  "validFrom": "2026-02-24T00:00:00Z",
  "validTo": null
}
```

---

## 📋 Implementation Order

Execute in this sequence to maintain clean build state:

### Phase 1: Persistence Layer (MUST BE FIRST)
1. Task 1.1 — Add Lombok to AgreementEntity
2. Task 1.2 — Add Lombok to AgreementConditionEntity
3. Task 1.3 — Add AgreementConditionEntity mapping methods
4. Task 1.4 — Add AgreementEntity mapping methods
5. Task 1.5 — Add @OneToMany to CompanyEntity
6. Task 1.6 — Update CompanyEntity.of() to map agreements
7. Task 1.7 — Update CompanyEntity.toCompany() to reconstruct agreements
8. Task 1.8 — Add CompanyRepository.findCompanyByAgreementId()
9. Task 1.9 — Add integration test for persistence

### Phase 2: Pre-Implementation Fixes (Domain Layer)
10. Task 2.1 — Expand AgreementConditionType enum
11. Task 2.2 — Create Agreement factory method
12. Task 2.3 — Add Agreement lifecycle methods (updateValidTo, updateConditions, overlapsWith)
13. Task 2.4 — Fix Company.addAgreement (immutability + overlap validation)
14. Task 2.5 — Fix Company.removeAgreement (immutability)
15. Task 2.6 — Add Company.updateAgreement method

### Phase 3: Database Migrations
16. Migration V10 — Add indexes
17. Migration V11 — Add FK constraints
18. Migration V12 — Add unique constraint

### Phase 4: Domain Layer (Events)
19. Task 4.1 — Create AgreementAdded event
20. Task 4.2 — Create AgreementRemoved event
21. Task 4.3 — Create AgreementUpdated event

### Phase 5: Application Layer (Use Cases)
22. Task 5.1 — CreateAgreementUseCase
23. Task 5.2 — GetAgreementsByCompanyUseCase
24. Task 5.3 — RemoveAgreementUseCase
25. Task 5.4 — UpdateAgreementUseCase
26. Task 5.5 — GetAgreementByIdUseCase

### Phase 6: Infrastructure Layer (Controllers & DTOs)
27. Task 6.1 — CreateAgreementController
28. Task 6.2 — GetAgreementsController
29. Task 6.3 — GetAgreementController
30. Task 6.4 — UpdateAgreementController
31. Task 6.5 — RemoveAgreementController
32. Task 6.6 — Create all DTOs

### Phase 7: Tests
33. Task 7.1 — Agreement domain tests (creation)
34. Task 7.2 — Company domain tests (add/remove)
35. Task 7.3 — Agreement lifecycle tests (update methods)
36. Task 7.4 — Company update tests (updateAgreement)
37. Task 7.5 — Integration tests (create)
38. Task 7.6 — Integration tests (get/query)
39. Task 7.7 — Integration tests (update/delete)

### Phase 8: Manual Verification
40. Create agreement-requests.http file with all scenarios
41. Execute HTTP scenarios (create, read, update, delete, negatives)
42. Verify events in outbox table
43. Verify indexes via EXPLAIN ANALYZE

---

## ✅ Success Criteria

### Technical Validation
- [ ] All domain unit tests pass
- [ ] All integration tests pass
- [ ] Persistence layer working (agreements saved to DB)
- [ ] Migrations apply cleanly
- [ ] Indexes improve query performance
- [ ] FK constraints prevent orphans
- [ ] Unique constraint prevents duplicates

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

### Business Validation
- [ ] Can create agreement via REST
- [ ] Can query agreements (by company and by ID)
- [ ] Can update agreement (validTo, conditions)
- [ ] Can remove agreement
- [ ] Self-reference validation works
- [ ] Duplicate validation works
- [ ] Overlapping agreement detection works
- [ ] HTTP scenarios execute successfully (including updates)

### Event Verification
- [ ] AgreementAdded event generated
- [ ] AgreementRemoved event generated
- [ ] AgreementUpdated event generated
- [ ] Events stored in outbox
- [ ] Event payload correct

### Code Quality
- [ ] Immutability pattern followed (all updates return new instances)
- [ ] All variables declared final
- [ ] Events in aggregates, not use cases
- [ ] Use cases annotated correctly (@DomainService, @Cqrs)
- [ ] Controllers use RestUseCaseExecutor
- [ ] No business logic in controllers

---

## 📝 Notes

### Critical Reminders
1. **Persistence First:** Phase 1 MUST be completed before domain fixes — without it, agreements won't save
2. **Immutability:** MUST capture returned Company instance from all update methods
3. **Event Placement:** Events MUST be in aggregate methods, NOT use cases
4. **Database Constraints:** ON DELETE RESTRICT means companies with agreements cannot be deleted
5. **Architecture Decision:** Agreement is part of Company aggregate (single repository, cascade persistence)

### Future Extensions (Out of Scope)
- Agreement approval workflows
- Agreement templates
- Cross-module synchronization
- Condition enforcement in pricing/routing
- Agreement versioning/history

---

**Plan Status:** `READY FOR IMPLEMENTATION`  
**Ready for Implementation:** YES  
**Estimated Effort:** 3-4 days  
**Dependencies:** None  
**Next Steps:** Begin Phase 1 (Persistence Layer)

---

## Consolidation Log

**From:** 6 documents (2,756 lines total)
- 002-company-agreement-implementation.md (794 lines) — original draft
- 002-review-assessment-summary.md (323 lines) — UpdateAgreement gap
- 002-persistence-updates.md (421 lines) — Persistence layer tasks
- 002-executive-summary.md (201 lines) — Architecture decision
- 002-persistence-review.md (726 lines) — Technical analysis
- 002-persistence-flow.md (291 lines) — Visual diagrams

**To:** 1 consolidated plan (1,632 lines)

**Added:**
- 9 persistence layer tasks (Phase 1)
- 6 domain layer enhancements (Phase 2: lifecycle methods, updateAgreement)
- 3 domain events (AgreementAdded, Removed, Updated)
- 2 additional use cases (UpdateAgreement, GetAgreementById)
- 2 additional controllers (UpdateAgreement, GetAgreement)
- Complete HTTP scenarios including updates and negative tests

**Total tasks:** 43 (was 25, added 18)
**Phases:** 8 (was 6, added Persistence + split Domain)
**Effort:** 3-4 days (was 2-3 days, +1 day for persistence)
**Status:** READY FOR IMPLEMENTATION
