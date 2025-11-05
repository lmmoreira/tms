# TMS Codebase Context

**Purpose:** Essential project information for AI assistants. For complete details, see [CODEBASE_CONTEXT_FULL.md](CODEBASE_CONTEXT_FULL.md).

---

## Quick Facts

| Aspect | Details |
|--------|---------|
| **Project** | Transportation Management System (TMS) |
| **Purpose** | Manage logistics operations: quotations, orders, volume tracking, multi-actor integration |
| **Language** | Java 21 (virtual threads, records, pattern matching) |
| **Framework** | Spring Boot 3.x + Spring Modulith |
| **Architecture** | Modular Monolith with DDD, Hexagonal, CQRS, Event-Driven |
| **Database** | PostgreSQL (read/write separation) |
| **Messaging** | RabbitMQ (domain events, module communication) |
| **Build** | Maven |
| **Testing** | JUnit 5, Testcontainers (PostgreSQL + RabbitMQ) |

---

## Module Overview

### Commons Module
**Purpose:** Shared infrastructure and domain primitives

**Key Components:**
- `AbstractAggregateRoot` - Base for all aggregates
- `AbstractDomainEvent` - Base for events
- `Id` - UUID v7 generator
- `UseCase<Input, Output>` - Interface for all use cases
- `@DomainService`, `@Cqrs` - Annotations
- `RestUseCaseExecutor` - REST orchestration
- CQRS database routing infrastructure
- OpenTelemetry instrumentation

### Company Module
**Purpose:** Manage companies (shippers, carriers, logistics providers)

**Domain Concepts:**
- `Company` (Aggregate) - Organizations
- `Cnpj` (Value Object) - Brazilian tax ID (14 digits)
- `Agreement` (Entity) - Contracts between companies
- `Configuration` (Value Object) - Settings

**Operations:** Create, get by ID/CNPJ, update, delete, add configuration

**Events:** `CompanyCreated`, `CompanyUpdated`

### ShipmentOrder Module
**Purpose:** Manage transportation orders

**Domain Concepts:**
- `ShipmentOrder` (Aggregate) - Transportation request

**Operations:** Create, query

**Events:** `ShipmentOrderCreated`

---

## Technology Stack

### Core
- Java 21 (virtual threads)
- Spring Boot 3.x
- Spring Modulith (module boundaries)

### Persistence
- PostgreSQL (primary database)
- Flyway (migrations)
- Spring Data JPA
- Hibernate (ORM)

### Messaging
- RabbitMQ (event streaming)
- Spring AMQP

### Observability
- OpenTelemetry (traces, metrics, logs)
- Jaeger (tracing)
- Prometheus (metrics)
- Loki (logs)
- Grafana (visualization)

### Security
- Keycloak (authentication)
- OAuth2-Proxy (token validation)
- NGINX (edge gateway, API keys)

### Development
- Testcontainers (integration testing)
- Lombok (JPA entities only)
- Maven (build)

---

## Entry Points

### REST APIs

**Company Module:**
```
POST   /companies        - Create company
GET    /companies/{id}   - Get company by ID
PUT    /companies/{id}   - Update company
DELETE /companies/{id}   - Delete company
```

**ShipmentOrder Module:**
```
POST /shipment-orders       - Create order
GET  /shipment-orders/{id} - Get order
```

**Controllers:** `{module}/infrastructure/rest/*Controller.java`

### Database Migrations
**Location:** `infra/database/migration/`
- Flyway migrations (versioned SQL)
- Auto-run on startup via `tms-flyway` Docker service

### Message Queues
**Location:** `infra/rabbitmq/definitions.json`
- Pattern: `integration.{target-module}.{source-module}.{event}`
- Example: `integration.company.shipmentorder.created`

---

## Key Architecture Patterns

### Hexagonal Architecture (3 Layers)
```
domain/              → Pure Java, business logic, NO frameworks
application/         → Use cases, repository interfaces
infrastructure/      → REST, JPA, DTOs, listeners
```

### CQRS
- Write ops: `@Cqrs(DatabaseRole.WRITE)` → write database
- Read ops: `@Cqrs(DatabaseRole.READ)` → read replica
- Currently same PostgreSQL, can split later

### Event-Driven + Outbox
- Aggregates place events
- Repository saves aggregate + events (same transaction)
- Background publisher sends to RabbitMQ
- Other modules listen via `@RabbitListener`

### Immutable Domain
- Aggregates never mutate
- Update methods return NEW instances
- Value objects are Java records

---

## Important Constraints

1. **Domain purity** - No framework dependencies in domain layer
2. **Immutability** - Domain objects return new instances on updates
3. **Event-driven communication** - Modules interact only via events
4. **CQRS annotations** - Mandatory on all use cases and controllers
5. **UUID v7 for IDs** - Time-based, sequential identifiers (`Id.unique()`)
6. **Outbox pattern** - All events persisted before publishing

---

## Development Commands

```bash
# Infrastructure
make start-tms                      # Basic (DB + broker)
make start-tms-oauth                # + Auth (Keycloak)
make start-tms-with-observation     # + Observability (Grafana stack)
make stop-tms                       # Stop services
make down-tms-all                   # Stop and remove all

# Build & Test
mvn clean package                   # Build
mvn test                            # Run tests
mvn spring-boot:run                 # Run application

# Observability
http://localhost:16686              # Jaeger (traces)
http://localhost:9090               # Prometheus (metrics)
http://localhost:3000               # Grafana (dashboards)
```

---

## Testing Strategy

### Domain Tests
- Pure unit tests, no Spring context
- Test aggregates, value objects
- Fast, isolated
- **Example:** `CompanyTest.java`

### Integration Tests
- Full Spring context + Testcontainers
- PostgreSQL + RabbitMQ containers
- Test complete flows (REST → DB → events)
- **Example:** `TmsApplicationTests.java`

### Modularity Tests
- Verify module boundaries
- Spring Modulith verification
- **Example:** `ModularityTests.java`

---

## Configuration

### Module Activation
```yaml
modules:
  commons:
    enabled: ${MODULES_COMMONS_ENABLED:true}
  company:
    enabled: ${MODULES_COMPANY_ENABLED:true}
  order:
    enabled: ${MODULES_ORDER_ENABLED:true}
```

### CQRS Mode
```yaml
app:
  cqrs:
    mode: ${APP_CQRS_MODE:ENABLED}  # ENABLED, DISABLED, WRITE_ONLY, READ_ONLY
```

### Databases
```yaml
spring:
  datasource:
    write:
      url: jdbc:postgresql://${DB_WRITE_HOST}:${DB_WRITE_PORT}/${DB_WRITE_NAME}
    read:
      url: jdbc:postgresql://${DB_READ_HOST}:${DB_READ_PORT}/${DB_READ_NAME}
```

---

## For AI Assistants

When working with this codebase:

✅ **Do:**
- Respect layer boundaries strictly
- Follow existing patterns exactly
- Never mutate domain objects
- Place events in aggregates, not use cases
- Always use `@Cqrs` annotation
- Generate tests alongside code

❌ **Don't:**
- Add framework dependencies to domain
- Create mutable aggregates
- Call other module's repositories
- Skip CQRS annotations
- Throw events from use cases

**Primary References:**
- Patterns: [ARCHITECTURE.md](ARCHITECTURE.md)
- Quick lookup: [QUICK_REFERENCE.md](QUICK_REFERENCE.md)
- Examples: [examples/](examples/)
- Templates: [prompts/](prompts/)

---

## Additional Resources

### Documentation
- **Complete Context:** [CODEBASE_CONTEXT_FULL.md](CODEBASE_CONTEXT_FULL.md)
- **Architecture:** [ARCHITECTURE.md](ARCHITECTURE.md)
- **Quick Reference:** [QUICK_REFERENCE.md](QUICK_REFERENCE.md)
- **Glossary:** [GLOSSARY.md](GLOSSARY.md)

### Architecture Decisions
- **ADR-001:** ID Format (UUID v7) - [../adr/ADR-001-ID-Format.md](../adr/ADR-001-ID-Format.md)
- **ADR-002:** Log Pattern - [../adr/ADR-002-Log-Pattern.md](../adr/ADR-002-Log-Pattern.md)

### Technical Debt
- **Tracking:** [../debt/DEBT.md](../debt/DEBT.md)

---

**Last Updated:** 2025-11-05

**Note:** This is the condensed version for quick reference. For complete project structure, detailed workflows, and comprehensive guides, see [CODEBASE_CONTEXT_FULL.md](CODEBASE_CONTEXT_FULL.md).
