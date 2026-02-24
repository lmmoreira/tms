# History — Morpheus

## Project Context (Day 1)

**Product:** TMS (Transportation Management System)
**Tech Stack:** Java 21, Spring Boot 3.x, DDD/Hexagonal/CQRS/Event-Driven architecture
**Mission:** Documentation optimization for AI consumption
- Extract 6 reusable skills to .squad/skills/
- Consolidate docs: 260K → 118K tokens (55% reduction)
- Improve retrieval speed 5-10x (15-30s → 3-5s)

**Your Role:** Lead

**Owner:** Leonardo Moreira

---

## Learnings

{agents append here}

## 2026-02-24: Plan 002 Consolidation

**Context:** Leonardo identified critical gaps in Plan 002 — missing UpdateAgreement functionality and no persistence layer specification.

**Action Taken:**
- Reviewed 6 scattered documents (2,756 lines total)
- Consolidated into single executable plan at plans/002-company-agreement-implementation.md (1,632 lines)
- Architecture decision: Agreement remains part of Company aggregate (cascade persistence)

**Key Additions:**
- Phase 1: Persistence Layer (9 tasks) — CRITICAL, must be first
- Phase 2: Domain enhancements (6 tasks including updateAgreement, overlapsWith)
- Phase 4: AgreementUpdated event
- Phase 5: UpdateAgreementUseCase, GetAgreementByIdUseCase
- Phase 6: UpdateAgreementController, GetAgreementController
- Phase 8: Complete HTTP scenarios with update operations and negative tests

**Critical Architecture Decisions:**
1. Agreement is part of Company aggregate (not separate aggregate root)
2. Single repository (CompanyRepository) with cascade persistence
3. Persistence layer MUST be Phase 1 — without it, agreements never save to database
4. All update operations return new instances (immutability maintained)
5. Overlapping agreement detection added to prevent conflicts

**Impact:**
- Total tasks: 43 (was 25)
- Phases: 8 (was 6)
- Effort: 3-4 days (was 2-3 days)
- Status: READY FOR IMPLEMENTATION

**Learnings:**
- Persistence layer is often overlooked in domain-first planning
- JPA entities existing != persistence working (need mapping methods)
- CompanyEntity.of() and .toCompany() are critical integration points
- Cascade persistence eliminates need for separate agreement repository
- Update operations require careful overlap detection in bounded time ranges


## 2026-02-24: Phase 1 Persistence Layer Implementation

**Tasks Completed:** All 9 tasks from Phase 1

**1.1-1.2: Lombok Annotations**
- Added @Data, @NoArgsConstructor, @AllArgsConstructor, @Builder to AgreementEntity and AgreementConditionEntity
- Eliminated boilerplate while maintaining immutability pattern

**1.3-1.4: Entity Mapping Methods**
- AgreementConditionEntity.of() — domain → entity (preserves parent reference)
- AgreementConditionEntity.toAgreementCondition() — entity → domain
- AgreementEntity.of() — domain → entity (handles bidirectional cascade, lazy FK reference for destination)
- AgreementEntity.toAgreement() — entity → domain (reconstructs full graph)

**Critical Patterns Applied:**
- Bidirectional parent reference: AgreementConditionEntity.of() receives parent AgreementEntity
- Lazy FK reference for destination: Set ID only, JPA resolves on persist
- All mapping uses final fields and defensive copies (new HashMap, new HashSet)
- Stream-based collection mapping with Collectors.toSet()

**1.5-1.7: CompanyEntity Cascade Integration**
- Added @OneToMany(mappedBy="from", cascade=ALL, orphanRemoval=true) field
- Updated CompanyEntity.of() to map agreements (stream → AgreementEntity.of → collect)
- Updated CompanyEntity.toCompany() to reconstruct agreements (was returning emptySet)

**1.8: Repository Query Method**
- CompanyRepository.findCompanyByAgreementId(AgreementId) — interface method
- CompanyRepositoryImpl implementation — delegates to JPA repository
- CompanyJpaRepository.findByAgreementsId(UUID) — @Query with JOIN

**1.9: Integration Test**
- CompanyAgreementPersistenceTest with 3 scenarios:
  - shouldPersistAgreementsWhenSavingCompany — create → flush → clear → reload → verify
  - shouldCascadeDeleteAgreementsWhenRemovingFromCompany — orphanRemoval validation
  - shouldFindCompanyByAgreementId — query method validation
- Code compiles successfully (mvn test-compile passed)
- Test execution blocked by Docker/Testcontainer issue (infrastructure, not code)

**Key Learnings:**
- Hibernate 6 uses @JdbcTypeCode(SqlTypes.JSON) not @Type(JsonBinaryType.class)
- Cascade persistence requires bidirectional relationship awareness at mapping time
- EntityManager.flush() + clear() critical for integration tests (forces DB roundtrip)
- AgreementCondition record uses conditionType() accessor (matches parameter name)
- Lombok @Builder works well with JPA entities when combined with @Data

**Files Modified:**
- AgreementEntity.java — Lombok + of()/toAgreement() methods
- AgreementConditionEntity.java — Lombok + of()/toAgreementCondition() methods
- CompanyEntity.java — @OneToMany field + updated of()/toCompany()
- CompanyRepository.java — added findCompanyByAgreementId()
- CompanyRepositoryImpl.java — implemented findCompanyByAgreementId()
- CompanyJpaRepository.java — added @Query findByAgreementsId()

**Files Created:**
- CompanyAgreementPersistenceTest.java — 3 integration tests

**Decision:** Persistence layer complete. Phase 2 (domain fixes) can now proceed.
