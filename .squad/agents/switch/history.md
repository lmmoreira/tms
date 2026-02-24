# Switch — History

## Core Context
- **Project:** TMS (Transportation Management System)
- **Owner:** Leonardo Moreira
- **Tech Stack:** Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ
- **Architecture:** Modular Monolith, DDD, Hexagonal, CQRS, Event-Driven
- **Focus:** Agreement domain structure review

## Learnings

### 2026-02-24: Phase 2 Domain Layer Implementation Complete

**Implemented Tasks:**
- 2.1: Fixed Company.addAgreement() — Now returns new Company instance, places AgreementAdded event
- 2.2: Fixed Company.removeAgreement() — Now returns new Company instance, places AgreementRemoved event
- 2.3: Expanded AgreementConditionType enum — Added DISCOUNT_PERCENTAGE and DELIVERY_SLA_DAYS
- 2.4: Added Agreement.createAgreement() factory method — Validates no self-reference
- 2.5: Added Agreement.updateValidTo() method — Returns new instance, validates date range
- 2.6: Added Agreement.updateConditions() method — Returns new instance, validates non-empty
- Added Company.updateAgreement() method — Returns new instance, validates no overlaps, places AgreementUpdated event
- Created three domain events: AgreementAdded, AgreementRemoved, AgreementUpdated

**Key Patterns Applied:**
- All update methods return NEW Company/Agreement instances (immutability)
- Domain events placed in aggregate methods (Company.addAgreement, removeAgreement, updateAgreement)
- Added overlap detection via Agreement.overlapsWith() method
- Self-reference validation in Agreement.createAgreement()
- All variables declared final
- Followed existing Company.updateName() pattern for consistency

**Architecture Decisions:**
- Agreement lifecycle managed entirely within Company aggregate
- Overlap detection compares destination, type, and date ranges
- Agreement.updateValidTo() accepts null (no expiry date)
- Agreement.updateConditions() requires at least one condition
- Company.updateAgreement() detects field changes for event payload

**Files Modified:**
- AgreementConditionType.java — Added two new enum values
- Agreement.java — Added factory method and three lifecycle methods
- Company.java — Fixed addAgreement/removeAgreement to return new instances, added updateAgreement

**Files Created:**
- AgreementAdded.java — Domain event for agreement creation
- AgreementRemoved.java — Domain event for agreement removal
- AgreementUpdated.java — Domain event for agreement updates

**Compilation Status:** ✅ SUCCESS (mvn clean compile passed)


### 2026-02-24: Phase 5-6 Application & Infrastructure Layer Implementation Complete

**Implemented Tasks (Plan 002 - Company Agreement):**

**Phase 5: Application Layer (Use Cases) - 5 Use Cases Created:**
- 5.1: CreateAgreementUseCase — POST operation, validates companies exist, calls Agreement.createAgreement, adds to Company
- 5.2: GetAgreementsByCompanyUseCase — GET list, returns agreement views with metadata
- 5.3: GetAgreementByIdUseCase — GET single, uses findCompanyByAgreementId repository method
- 5.4: UpdateAgreementUseCase — PUT operation, supports validTo and conditions updates
- 5.5: RemoveAgreementUseCase — DELETE operation, removes agreement from Company

**Phase 6: Infrastructure Layer (Controllers + DTOs) - 5 Controllers + 6 DTOs Created:**

**Controllers:**
- CreateAgreementController — POST /companies/{id}/agreements
- GetAgreementsByCompanyController — GET /companies/{id}/agreements
- GetAgreementByIdController — GET /agreements/{id}
- UpdateAgreementController — PUT /agreements/{id}
- RemoveAgreementController — DELETE /agreements/{id}

**DTOs:**
- CreateAgreementDTO — Request with destinationCompanyId, type, configuration, conditions, validFrom/To
- UpdateAgreementDTO — Request with validTo, conditions
- AgreementResponseDTO — Response with agreementId, sourceCompanyId, destinationCompanyId, agreementType
- AgreementUpdateResponseDTO — Response with agreementId, message
- AgreementDetailResponseDTO — Full agreement details for GET by ID
- AgreementsListResponseDTO — Response with companyId + list of agreement views

**Exception Created:**
- AgreementNotFoundException — Domain exception for agreement not found scenarios

**Key Patterns Applied:**
- All use cases: @DomainService + @Cqrs(DatabaseRole.WRITE/READ)
- Input/Output as nested records in use cases
- All controllers: RestUseCaseExecutor pattern + DefaultRestPresenter
- Zero business logic in controllers — pure delegation
- All variables declared final
- DTOs provide toInput() helper methods for mapping
- Proper HTTP status codes: 201 Created, 200 OK, 204 No Content

**Architecture Decisions:**
- CreateAgreementUseCase validates both source and destination companies exist
- UpdateAgreementUseCase applies updates conditionally (only non-null fields)
- RemoveAgreementController uses NO_CONTENT status (standard REST pattern)
- GetAgreementsByCompanyUseCase returns lightweight views, GetAgreementByIdUseCase returns full details
- All agreement lookups use findCompanyByAgreementId repository method (requires Phase 1 implementation)

**Files Created (16 total):**

**Use Cases (5):**
- CreateAgreementUseCase.java
- GetAgreementsByCompanyUseCase.java
- GetAgreementByIdUseCase.java
- UpdateAgreementUseCase.java
- RemoveAgreementUseCase.java

**Controllers (5):**
- CreateAgreementController.java
- GetAgreementsByCompanyController.java
- GetAgreementByIdController.java
- UpdateAgreementController.java
- RemoveAgreementController.java

**DTOs (6):**
- CreateAgreementDTO.java
- UpdateAgreementDTO.java
- AgreementResponseDTO.java
- AgreementUpdateResponseDTO.java
- AgreementDetailResponseDTO.java
- AgreementsListResponseDTO.java

**Exceptions (1):**
- AgreementNotFoundException.java

**Compilation Status:** ✅ SUCCESS (mvn clean compile passed)

**Remaining Work (Plan 002):**
- Phase 1: Persistence Layer (CRITICAL — agreements won't save without this)
- Phase 3: Database Migrations (indexes, constraints)
- Phase 7: Tests (domain, integration)
- Phase 8: HTTP Request Scenarios

**Notes:**
- Phase 5-6 implementation is complete and follows all TMS patterns
- Use cases depend on findCompanyByAgreementId() repository method from Phase 1
- Controllers are ready to receive HTTP requests once persistence layer is implemented
- All code compiles successfully and follows immutability patterns
