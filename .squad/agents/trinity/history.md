# History — Trinity

## Project Context (Day 1)

**Product:** TMS (Transportation Management System)
**Tech Stack:** Java 21, Spring Boot 3.x, DDD/Hexagonal/CQRS/Event-Driven architecture
**Mission:** Documentation optimization for AI consumption
- Extract 6 reusable skills to .squad/skills/
- Consolidate docs: 260K → 118K tokens (55% reduction)
- Improve retrieval speed 5-10x (15-30s → 3-5s)

**Your Role:** Doc Engineer

**Owner:** Leonardo Moreira

---

## Learnings

### 2026-02-26 — JPA and REST API Patterns from E2E Testing

**Context:** Leonardo and Switch completed E2E testing for Company-Agreement agreement flow. Documented critical JPA and REST API patterns learned during implementation.

**JPA Bidirectional Relationships:**
- **ID-only equals/hashCode:** Essential for entities with bidirectional relationships (Company ↔ ShipmentOrder, Company ↔ Agreement). Prevents circular references and StackOverflowError.
- **Lombok @Data danger:** Never use @Data on entities with relationships — it generates equals/hashCode using ALL fields, causing circular traversal.
- **Resolver functions for FK references:** Always use `Function<UUID, Entity>` resolvers to fetch existing entities. Creating transient entities and assigning as FK causes `TransientObjectException`.

**REST API Patterns:**
- **Nested resource routing:** Use `/companies/{companyId}/agreements` for parent-child relationships.
- **Path variable requirement:** ALL path variables in `@RequestMapping` MUST appear in method signature as `@PathVariable`. Spring won't bind them otherwise.
- **DTO field validation:** Configuration fields validated in value objects (cannot be null/empty). Formatted value objects (CNPJ) receive raw input in DTOs, domain applies formatting.

**Environment Setup:**
- **Minimal Docker:** Use `make start-tms` for tests — starts only PostgreSQL + RabbitMQ (no OAuth2, no observability). Faster startup, lower resource usage.

**Documentation Updates:**
- Added "JPA Bidirectional Relationships" section to ARCHITECTURE.md (equals/hashCode, resolver functions, @Data dangers)
- Added "Nested Resource Routing Pattern" and "DTO Field Validation" sections to ARCHITECTURE.md (pattern 2)
- Added "DTO Field Validation" and "Environment Setup" to INTEGRATION_TESTS.md (best practices)
- Added "Common Pitfalls" section to QUICK_REFERENCE.md (JPA dangers, path variable requirements)
- All sections reference `.squad/skills/e2e-testing-tms/SKILL.md` (Switch is creating)

**Files Modified:**
- `/doc/ai/ARCHITECTURE.md` — Added JPA patterns and REST API patterns
- `/doc/ai/INTEGRATION_TESTS.md` — Added DTO validation and environment notes
- `/doc/ai/QUICK_REFERENCE.md` — Added quick lookups for common pitfalls

{agents append here}
