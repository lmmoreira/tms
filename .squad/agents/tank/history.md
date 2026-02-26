# History — Tank

## Project Context (Day 1)

**Product:** TMS (Transportation Management System)
**Tech Stack:** Java 21, Spring Boot 3.x, DDD/Hexagonal/CQRS/Event-Driven architecture
**Mission:** Documentation optimization for AI consumption
- Extract 6 reusable skills to .squad/skills/
- Consolidate docs: 260K → 118K tokens (55% reduction)
- Improve retrieval speed 5-10x (15-30s → 3-5s)

**Your Role:** Automation

**Owner:** Leonardo Moreira

---

## Learnings

{agents append here}

## 2026-02-26: Agreement Integration Test Creation

**Task:** Created story-driven integration test for Agreement CRUD operations.

**Created:**
- `AgreementIntegrationFixture` at `/src/test/java/br/com/logistics/tms/integration/fixtures/AgreementIntegrationFixture.java`
  - Methods: `createAgreement()`, `updateAgreement()`, `removeAgreement()`, `getAgreement()`
  - Clean API for agreement operations via REST endpoints
  - Response format: agreement data is at root level (not wrapped in "value" field)

- `CompanyAgreementIT` at `/src/test/java/br/com/logistics/tms/integration/CompanyAgreementIT.java`
  - Single story-driven test: `shouldCreateUpdateAndRemoveAgreementBetweenCompaniesInCompleteBusinessFlow()`
  - Flow: Create Shoppe (marketplace) → Create Loggi (logistics provider) → Create agreement → Verify in DB → Update validity → Verify update → Remove agreement → Verify deletion
  - Uses `EntityManager` with JOIN FETCH to handle lazy-loaded agreement collections
  - All variables declared as `final`

**Deleted:**
- `CreateAgreementIntegrationTest.java`
- `GetAgreementsIntegrationTest.java`
- `RemoveAgreementIntegrationTest.java`
- `UpdateAgreementIntegrationTest.java`

**Updated:**
- `AbstractIntegrationTest.java`: Added `agreementFixture` setup

**Key Learnings:**
- Agreement response DTOs are NOT wrapped in "value" field (different from initial assumption)
- Lazy-loaded `CompanyEntity.agreements` collection requires explicit JOIN FETCH via EntityManager to avoid LazyInitializationException
- UpdateAgreement requires initial validTo to be non-null (domain validation compares existing with new, null comparison fails)
- Test follows existing pattern from `CompanyShipmentOrderIT` - story-driven flow with database-level verification using JPA repositories and assertions

**Test Result:** ✅ PASSED (1 test, 0 failures, 0 errors)
