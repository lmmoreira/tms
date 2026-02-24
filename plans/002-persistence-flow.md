# Agreement Persistence Flow - Visual Guide

**For:** Leonardo Moreira  
**Context:** Understanding WHERE and HOW agreements are saved

---

## 🔄 Current Broken Flow (What Happens NOW)

```
Use Case Layer:
┌─────────────────────────────────────┐
│ CreateAgreementUseCase              │
│                                     │
│ 1. agreement = Agreement.create()   │
│ 2. updated = company.add(agreement) │
│ 3. companyRepository.update(updated)│ ← Calls repository
└──────────────────┬──────────────────┘
                   ↓
Repository Layer:
┌─────────────────────────────────────┐
│ CompanyRepositoryImpl.update()      │
│                                     │
│ 1. entity = CompanyEntity.of(company)│ ← Mapping happens here
└──────────────────┬──────────────────┘
                   ↓
Mapping Layer (BROKEN):
┌─────────────────────────────────────┐
│ CompanyEntity.of(company)           │
│                                     │
│ ✅ Maps: id, name, cnpj, types      │
│ ✅ Maps: configuration, status      │
│ ❌ IGNORES: agreements              │ ← THE PROBLEM
│                                     │
│ Returns CompanyEntity WITHOUT       │
│ any agreement data                  │
└──────────────────┬──────────────────┘
                   ↓
Database:
┌─────────────────────────────────────┐
│ JPA saves CompanyEntity             │
│ ❌ NO agreements saved              │
│ ❌ agreement table empty            │
└─────────────────────────────────────┘
```

**Result:** Use case executes successfully but agreements never reach database.

---

## ✅ Fixed Flow (What SHOULD Happen)

```
Use Case Layer:
┌─────────────────────────────────────┐
│ CreateAgreementUseCase              │
│                                     │
│ 1. agreement = Agreement.create()   │
│ 2. updated = company.add(agreement) │
│ 3. companyRepository.update(updated)│ ← Calls repository
└──────────────────┬──────────────────┘
                   ↓
Repository Layer:
┌─────────────────────────────────────┐
│ CompanyRepositoryImpl.update()      │
│                                     │
│ 1. entity = CompanyEntity.of(company)│ ← Mapping happens here
└──────────────────┬──────────────────┘
                   ↓
Mapping Layer (FIXED):
┌─────────────────────────────────────────────────────────┐
│ CompanyEntity.of(company)                               │
│                                                         │
│ ✅ Maps: id, name, cnpj, types, configuration, status   │
│ ✅ NEW: Maps agreements:                                │
│                                                         │
│   company.getAgreements() ──────┐                       │
│                                 ↓                       │
│   for each Agreement:                                   │
│     agreementEntity = AgreementEntity.of(agreement)     │
│                                                         │
│   Set<AgreementEntity> ──────┐                          │
│                              ↓                          │
│   entity.setAgreements(agreementEntities)               │
│                                                         │
│ Returns CompanyEntity WITH agreement data               │
└──────────────────┬──────────────────────────────────────┘
                   ↓
Cascade Persistence:
┌─────────────────────────────────────────────────────────┐
│ JPA detects @OneToMany(cascade = ALL)                   │
│                                                         │
│ CompanyEntity ──────┐                                   │
│                     ↓                                   │
│         INSERT/UPDATE company table                     │
│                                                         │
│ AgreementEntity ────┐                                   │
│                     ↓                                   │
│         INSERT/UPDATE agreement table                   │
│                                                         │
│ AgreementConditionEntity ──┐                            │
│                            ↓                            │
│         INSERT/UPDATE agreement_condition table         │
└─────────────────────────────────────────────────────────┘
                   ↓
Database (Single Transaction):
┌─────────────────────────────────────┐
│ company table:                      │
│   ✅ 1 row inserted/updated         │
│                                     │
│ agreement table:                    │
│   ✅ N rows inserted/updated        │
│   ✅ source_company_id = company.id │
│                                     │
│ agreement_condition table:          │
│   ✅ M rows inserted/updated        │
│   ✅ agreement_id = agreement.id    │
└─────────────────────────────────────┘
```

**Result:** All agreement data persisted in single transaction via cascade.

---

## 🔀 Load Flow (Reading from Database)

### Current Broken Flow:

```
companyRepository.getCompanyById(shoppeId)
    ↓
CompanyJpaRepository.findById(id)
    ↓
SELECT * FROM company WHERE id = ?
    ↓
CompanyEntity loaded from DB
    ↓
CompanyEntity.toCompany()
    ↓
❌ agreements = Collections.emptySet()  ← HARDCODED EMPTY
    ↓
Company returned with NO agreements
```

### Fixed Flow:

```
companyRepository.getCompanyById(shoppeId)
    ↓
CompanyJpaRepository.findById(id)
    ↓
SELECT c.*, a.*, ac.*
FROM company c
LEFT JOIN agreement a ON c.id = a.source_company_id
LEFT JOIN agreement_condition ac ON a.id = ac.agreement_id
WHERE c.id = ?
    ↓
CompanyEntity loaded WITH AgreementEntity collection
    ↓
CompanyEntity.toCompany()
    ↓
✅ agreements = this.agreements.stream()
                  .map(AgreementEntity::toAgreement)
                  .collect(toSet())
    ↓
Company returned WITH reconstructed agreements
```

---

## 📊 Database Schema Relationship

```
┌─────────────────────────────────────┐
│ company.company                     │
├─────────────────────────────────────┤
│ id (PK)                             │
│ name                                │
│ cnpj                                │
│ status                              │
│ ...                                 │
└────────────┬────────────────────────┘
             │ 1
             │
             │ N
┌────────────▼────────────────────────┐
│ company.agreement                   │
├─────────────────────────────────────┤
│ id (PK)                             │
│ source_company_id (FK) ←───────────┘
│ destination_company_id (FK)         │
│ relation_type                       │
│ configuration (JSON)                │
│ valid_from                          │
│ valid_to                            │
└────────────┬────────────────────────┘
             │ 1
             │
             │ N
┌────────────▼────────────────────────┐
│ company.agreement_condition         │
├─────────────────────────────────────┤
│ id (PK)                             │
│ agreement_id (FK) ←─────────────────┘
│ condition_type                      │
│ conditions (JSON)                   │
└─────────────────────────────────────┘
```

**Cascade Behavior:**
- `DELETE company` → cascades to `agreement` (orphanRemoval = true)
- `DELETE agreement` → cascades to `agreement_condition`
- `UPDATE company` → JPA detects changes in `agreements` collection, updates DB

---

## 🔧 The 9 Tasks Explained Visually

```
Task 3.1: Add @OneToMany to CompanyEntity
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
CompanyEntity {
    ...existing fields...
    + @OneToMany Set<AgreementEntity> agreements  ← NEW
}

Task 3.2-3.3: Add AgreementEntity mapping
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
AgreementEntity {
    + static of(Agreement) → AgreementEntity     ← NEW
    + toAgreement() → Agreement                  ← NEW
}

Task 3.4: Fix CompanyEntity.of()
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
of(company) {
    ...existing mapping...
    + entity.setAgreements(                      ← NEW
        company.getAgreements().stream()
            .map(AgreementEntity::of)
            .collect(toSet())
    )
}

Task 3.5: Fix CompanyEntity.toCompany()
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
toCompany() {
    - agreements: Collections.emptySet()          ← OLD
    + agreements: this.agreements.stream()        ← NEW
                    .map(AgreementEntity::toAgreement)
                    .collect(toSet())
}

Task 3.6-3.7: Add AgreementConditionEntity mapping
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
AgreementConditionEntity {
    + static of(AgreementCondition) → Entity     ← NEW
    + toAgreementCondition() → AgreementCondition ← NEW
}

Task 3.8: Add repository query
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
CompanyRepository {
    + findCompanyByAgreementId(id)               ← NEW
      (for RemoveAgreementUseCase)
}
```

---

## 🎯 Summary

**The Gap:** Mapping layer exists but is incomplete
**The Fix:** Add bidirectional mapping (domain ↔ entity)
**The Benefit:** Agreements persist automatically via cascade
**The Cost:** 9 tasks, ~1 day effort

**Before Fix:**
- Use cases compile ✅
- Agreements saved ❌

**After Fix:**
- Use cases compile ✅
- Agreements saved ✅
- Single transaction ✅
- Cascade delete ✅
- Load with company ✅

---

**Next:** Review 002-executive-summary.md for approval decision
