# Apoc — History

## Core Context
- **Project:** TMS (Transportation Management System)
- **Owner:** Leonardo Moreira
- **Tech Stack:** Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ
- **Architecture:** Modular Monolith, DDD, Hexagonal, CQRS, Event-Driven
- **Focus:** Agreement domain structure review

## Learnings

### 2026-02-24: Phase 3 Database Migrations Created
**Context:** Implemented Phase 3 of Plan 002 - Database Migrations for Company Agreement feature
**What I learned:**
- Created three sequential migrations (V10, V11, V12) building on existing V5 agreement tables
- V10: Added comprehensive indexes for query optimization (source, destination, relation_type, active agreements, condition lookups)
- V11: Enhanced data integrity by changing foreign key constraints from default CASCADE to RESTRICT, preventing accidental company deletion when agreements exist
- V12: Implemented unique partial index to enforce business rule preventing duplicate active agreements for same source-destination-relation_type
- Followed Flyway naming convention: V{number}__{description}.sql
- Used partial indexes with WHERE clauses to enforce business constraints at database level
- Ensured all SQL includes comments explaining business purpose, not just technical implementation
**Files created:**
- infra/database/migration/V10__add_agreement_indexes.sql
- infra/database/migration/V11__fix_agreement_foreign_keys.sql
- infra/database/migration/V12__add_agreement_unique_constraint.sql

### 2026-02-24: Phase 8 HTTP Request Scenarios Created
**Context:** Implemented Phase 8.1 of Plan 002 - Created comprehensive HTTP request scenarios for Company Agreement feature
**What I learned:**
- HTTP request files tell a business story, not just test CRUD operations
- Structured narrative flow: setup companies → create agreements → query → update → delete → negative tests
- Real-world scenario demonstrates value: Shoppe (marketplace), Loggi (logistics provider), Biquelo (seller) forming commercial relationships
- Variable capture pattern enables sequential requests: `> {% client.global.set("varName", response.body.field) %}`
- Negative test cases document business rules clearly: duplicates, self-reference, expired dates, invalid values, missing targets
- Agreement updates show two patterns: condition changes (discount increase) and validity extension (renewal)
- Query patterns show different perspectives: source company's agreements vs target company receiving multiple agreements
- Comments and section headers transform raw HTTP into executable documentation
**Business rules validated:**
- Cannot create duplicate agreements (same source + target + relationship type)
- Cannot create self-referencing agreements (company with itself)
- Date ranges must be logical (validFrom before validTo)
- Discount percentages must be 0-100%
- Target company must exist
**Files created:**
- src/main/resources/company/agreement-requests.http (11 positive scenarios, 5 negative test cases)
