# Agreement Persistence - Executive Summary

**For:** Leonardo Moreira  
**Date:** 2026-02-24  
**Issue:** "I did not find in the plan, where the agreements are saved"  
**Status:** ✅ RESOLVED - Architecture decision made, implementation plan ready

---

## 🎯 The Problem (What You Found)

You reviewed Plan 002 and identified **TWO CRITICAL MISSING PIECES:**

1. ✅ **UpdateAgreement functionality** (already identified in previous review)
2. ✅ **Agreement persistence layer** — WHERE and HOW are agreements saved?

**Your question was correct:** Plan 002 specifies domain behavior (`Company.addAgreement()`) and use cases (`CreateAgreementUseCase`) but **completely omits the persistence layer**. There's no explanation of how Agreement domain objects get saved to the database.

---

## 🔍 What I Found (Code Analysis)

### Current State

**Domain Layer (✅ Complete):**
- `Company` has `Set<Agreement> agreements`
- `Agreement` is an immutable record (value object pattern)
- `Company.addAgreement()` and `.removeAgreement()` methods exist (with mutability bugs Plan 002 fixes)

**Infrastructure Layer (⚠️ BROKEN):**
- `AgreementEntity` exists in database schema
- `CompanyEntity.of(company)` **IGNORES agreements** — never maps them
- `CompanyEntity.toCompany()` **ALWAYS returns** `Collections.emptySet()` for agreements
- `AgreementEntity` has **NO mapping methods** (`of()`, `toAgreement()`)

**Result:** Use cases will compile but **agreements will never be saved to database**.

---

## 🏗️ The Architecture Decision

**Question:** Should Agreement be:
- **Option A:** Part of Company aggregate (current design)
- **Option B:** Separate aggregate with its own AgreementRepository

### Recommendation: Option A (Agreement Part of Company Aggregate)

**Why Option A:**

| Factor | Justification |
|--------|---------------|
| **Domain Alignment** | Agreement has NO independent lifecycle — cannot exist without source Company |
| **Business Operations** | "Update discount" is a Company operation (Company negotiates terms) |
| **Transaction Boundary** | Clear: Company + all its agreements in single transaction |
| **Complexity** | Simpler: one repository, cascade persistence, no aggregate coordination |
| **Common Case** | Most frequent query: "get agreements for Company X" (happens during order placement) |
| **Aggregate Size** | Manageable: companies typically have 1-10 agreements, not hundreds |

**Trade-off Accepted:**
- ❌ Cross-company queries ("all agreements with Loggi") require loading multiple companies
- ✅ This is rare admin operation, NOT critical path
- ✅ Can be solved later with read-model if needed

**TMS DDD Principles:**
- ✅ Aggregate boundary = transaction boundary
- ✅ Aggregate root owns all contained entities
- ✅ Value objects (Agreement) have no repository

---

## 🔧 The Missing Implementation

**What needs to be added to Plan 002:**

### 9 New Tasks (Persistence Layer - Phase 1)

| Task | What | Why |
|------|------|-----|
| **3.1** | Add `@OneToMany` field to CompanyEntity | Enables cascade save/delete |
| **3.2** | Add Lombok to AgreementEntity | Required for mapping methods |
| **3.3** | Add `AgreementEntity.of(Agreement)` | Domain → Entity conversion |
| **3.4** | Add `AgreementEntity.toAgreement()` | Entity → Domain conversion |
| **3.5** | Fix `CompanyEntity.of()` | Map agreements (currently ignored) |
| **3.6** | Fix `CompanyEntity.toCompany()` | Reconstruct agreements (currently empty) |
| **3.7** | Add Lombok to AgreementConditionEntity | Required for nested mapping |
| **3.8** | Add condition mapping methods | Support nested conditions |
| **3.9** | Add `CompanyRepository.findByAgreementId()` | Query for RemoveAgreementUseCase |

### How Cascade Persistence Works

```java
// Use case code:
final Company company = companyRepository.getCompanyById(shoppeId);
final Agreement agreement = Agreement.createAgreement(...);
final Company updated = company.addAgreement(agreement);
companyRepository.update(updated);  // ← This triggers cascade

// What happens in repository:
CompanyEntity entity = CompanyEntity.of(updated);  
  ↓ (NEW) Maps company.getAgreements() → Set<AgreementEntity>
  ↓ Each Agreement converted via AgreementEntity.of(agreement, entity)
  ↓ Each AgreementCondition converted via AgreementConditionEntity.of(condition)

jpaRepository.save(entity);
  ↓ JPA detects @OneToMany with cascade = ALL
  ↓ Automatically inserts/updates/deletes related AgreementEntity rows
  ↓ FK constraints maintained by JPA
```

**Single transaction, single repository call.**

---

## 📊 Impact Analysis

### Effort Impact

| Original Estimate | New Estimate | Reason |
|------------------|--------------|--------|
| 2-3 days | 3-4 days | +9 persistence tasks (~1 day) |

### Implementation Order Change

**Current Plan 002:**
```
Phase 1: Critical Fixes (Domain)
Phase 2: Database
Phase 3: Use Cases
...
```

**Updated Order:**
```
Phase 1: Persistence Layer (NEW) ← MUST BE FIRST
Phase 2: Critical Fixes (Domain)
Phase 3: Database
Phase 4: Use Cases
...
```

**Why persistence first:** Domain fixes (immutability) and use cases depend on persistence working. If we implement use cases first and then discover persistence is broken, we're blocked.

---

## 📋 Three Documents Created

| Document | Purpose | Action |
|----------|---------|--------|
| **002-persistence-review.md** | Full analysis with code examples, architecture comparison, DDD justification | Read for deep understanding |
| **002-persistence-updates.md** | Exact sections to add to Plan 002, ready to copy-paste | Use for updating the plan |
| **002-executive-summary.md** | This document — high-level overview for approval | Read first |

---

## ✅ What You Need to Decide

**Question 1:** Do you approve **Option A** (Agreement part of Company aggregate)?

- ✅ **Yes** → Proceed with 9 persistence tasks added to Plan 002
- ❌ **No, prefer Option B** → Need to discuss separate aggregate approach (larger refactor)

**Question 2:** Should I update Plan 002 now or do you want to review the full analysis first?

- **Option A:** "Update the plan" → I'll edit 002-company-agreement-implementation.md with persistence section
- **Option B:** "Let me review first" → You read 002-persistence-review.md, then approve

---

## 🎯 Key Takeaways

1. ✅ **Your question was 100% correct** — the persistence layer WAS missing
2. ✅ **Option A (Agreement part of Company)** matches current design and DDD principles
3. ✅ **9 new tasks** needed to fix the gap (mostly mapping methods)
4. ✅ **+1 day effort** (acceptable for critical architecture fix)
5. ✅ **Persistence MUST be Phase 1** (before domain fixes and use cases)

---

## 🚀 Recommended Next Steps

1. **You decide:** Option A or Option B? (I recommend Option A)
2. **If Option A approved:**
   - I update Plan 002 with persistence section from 002-persistence-updates.md
   - You review updated plan
   - Implementation begins with Phase 1 (persistence layer)
3. **Validation strategy:**
   - After Phase 1 complete: run integration test
   - Verify agreements persist to database via SQL query
   - THEN proceed to Phase 2 (domain fixes)

---

**Recommendation:** ✅ Approve Option A, proceed with persistence layer addition

**Confidence:** 🟢 High — this matches TMS patterns, DDD principles, and current code structure

**Risk if skipped:** 🔴 Critical — use cases will compile but fail at runtime (agreements never saved)

---

**Awaiting your approval to proceed.**
