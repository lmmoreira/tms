# TMS Codebase Context

## Quick Facts

- **Project:** Transportation Management System (TMS)
- **Purpose:** Manage logistics operations including quotations, orders, volume tracking, and multi-actor integration
- **Language:** Java 21
- **Framework:** Spring Boot 3.x with Spring Modulith
- **Architecture:** Modular Monolith using DDD, Hexagonal Architecture, CQRS, and Event-Driven patterns
- **Database:** PostgreSQL (with read/write separation via CQRS)
- **Messaging:** RabbitMQ (for domain events and module communication)
- **Build Tool:** Maven
- **Testing:** JUnit 5, Testcontainers (PostgreSQL + RabbitMQ)

---

## Project Structure

```
tms/
├── src/main/java/br/com/logistics/tms/
│   ├── TmsApplication.java              # Main entry point
│   │
│   ├── commons/                         # Shared infrastructure & domain primitives
│   │   ├── domain/                      # Base classes (AbstractAggregateRoot, Id, etc.)
│   │   ├── application/                 # UseCase interface, annotations, presenters
│   │   └── infrastructure/              # CQRS, database routing, telemetry, REST support
│   │
│   ├── company/                         # Company bounded context
│   │   ├── domain/                      # Company aggregate, Cnpj, Agreement, events
│   │   ├── application/                 # Use cases, CompanyRepository interface
│   │   └── infrastructure/              # REST controllers, JPA entities, DTOs, listeners
│   │
│   └── shipmentorder/                   # ShipmentOrder bounded context
│       ├── domain/                      # ShipmentOrder aggregate, events
│       ├── application/                 # Use cases, repository interface
│       └── infrastructure/              # REST controllers, JPA entities, DTOs
│
├── src/main/resources/
│   ├── application.yml                  # Spring Boot configuration
│   ├── company/                         # Company module resources
│   ├── shipmentorder/                   # ShipmentOrder module resources
│   └── logback-spring.xml               # Logging configuration
│
├── src/test/java/                       # Tests mirror main structure
│
├── doc/
│   ├── ai/                              # AI assistant documentation
│   │   ├── ARCHITECTURE.md              # Comprehensive architecture guide
│   │   ├── examples/                    # Code examples
│   │   └── prompts/                     # Reusable templates
│   ├── adr/                             # Architecture Decision Records
│   │   ├── ADR-001-ID-Format.md
│   │   └── ADR-002-Log-Pattern.md
│   └── debt/                            # Technical debt tracking
│       └── DEBT.md
│
├── infra/                               # Infrastructure configuration
│   ├── database/migration/              # Flyway SQL migrations
│   ├── nginx/                           # NGINX edge gateway config
│   ├── keycloak/                        # Authentication setup
│   ├── rabbitmq/                        # Message broker definitions
│   ├── postgresql/                      # Database config
│   ├── grafana/                         # Observability dashboards
│   └── otel/                            # OpenTelemetry collector config
│
├── docker-compose.yaml                  # Local development infrastructure
├── Makefile                             # Convenience commands
├── pom.xml                              # Maven configuration
├── README.md                            # Project overview
└── HELP.md                              # Developer setup guide
```

---

## Module Overview

### Commons Module
**Purpose:** Shared infrastructure and domain primitives used by all modules

**Key Components:**
- `AbstractAggregateRoot` - Base class for all aggregates
- `AbstractDomainEvent` - Base class for domain events
- `Id` - UUID v7 generator for all entities
- `UseCase<Input, Output>` - Interface for all use cases
- `@DomainService` - Marks use cases
- `@Cqrs(DatabaseRole)` - Routes operations to read/write databases
- `RestUseCaseExecutor` - Orchestrates REST request → use case → response flow
- CQRS database routing infrastructure
- OpenTelemetry instrumentation

### Company Module
**Purpose:** Manage companies (shippers, carriers, logistics providers)

**Domain Concepts:**
- `Company` (Aggregate Root) - Organizations that ship or receive goods
- `Cnpj` (Value Object) - Brazilian business tax ID (14 digits)
- `CompanyId` (Value Object) - UUID v7 identifier
- `Agreement` (Entity) - Contracts between companies
- `Configuration` (Value Object) - Key-value settings

**Operations:**
- Create company
- Get company by ID
- Get company by CNPJ
- Update company
- Delete company
- Add configuration
- Increment shipment order counter (via event listener)

**Events:**
- `CompanyCreated`
- `CompanyUpdated`

### ShipmentOrder Module
**Purpose:** Manage transportation orders from creation to completion

**Domain Concepts:**
- `ShipmentOrder` (Aggregate Root) - Transportation request

**Operations:**
- Create shipment order
- Query shipment orders

**Events:**
- `ShipmentOrderCreated`

---

## Architecture Patterns

### 1. Hexagonal Architecture (Ports & Adapters)
Each module has three layers:
- **Domain** (Core) - Pure business logic, no frameworks
- **Application** (Ports) - Use cases and repository interfaces
- **Infrastructure** (Adapters) - REST, JPA, messaging implementations

### 2. CQRS (Command Query Responsibility Segregation)
- Write operations use `@Cqrs(DatabaseRole.WRITE)` → write database
- Read operations use `@Cqrs(DatabaseRole.READ)` → read replica
- Currently both point to same PostgreSQL, but can be split later

### 3. Event-Driven with Outbox Pattern
- Aggregates place domain events in memory
- Repository persists aggregate + events to outbox table (same transaction)
- Background publisher sends events to RabbitMQ
- Other modules listen via `@RabbitListener`

### 4. Immutable Domain Objects
- Aggregates never mutate - updates return new instances
- Value objects are Java records (immutable by nature)
- Ensures thread safety and clear audit trail

---

## Key Concepts

### Aggregates
- Consistency boundaries (e.g., `Company`, `ShipmentOrder`)
- All changes go through aggregate root
- Enforce business invariants
- Raise domain events

### Value Objects
- Defined by attributes, not identity
- Examples: `CompanyId`, `Cnpj`, `Agreement`
- Immutable (Java records)
- Contain validation logic

### Domain Events
- Past tense naming (e.g., `CompanyCreated`)
- Raised by aggregates when state changes
- Persisted to outbox table before publishing
- Enable module decoupling

### Use Cases
- Single operation per use case
- One way to perform an operation
- Annotated with `@DomainService` and `@Cqrs`
- Input/Output as nested records

---

## Entry Points

### Main Application
**File:** `src/main/java/br/com/logistics/tms/TmsApplication.java`
- Spring Boot main class
- `@Modulithic` enables Spring Modulith
- Imports module configurations

### REST APIs
**Company Module:**
- `POST /companies` - Create company
- `GET /companies/{id}` - Get company by ID
- `PUT /companies/{id}` - Update company
- `DELETE /companies/{id}` - Delete company

**ShipmentOrder Module:**
- `POST /shipment-orders` - Create order
- `GET /shipment-orders/{id}` - Get order

**Controllers:** `{module}/infrastructure/rest/*Controller.java`

### Database Migrations
**Location:** `infra/database/migration/`
- Flyway migrations (versioned SQL scripts)
- Run automatically on startup via `tms-flyway` Docker service

### Message Queues
**Location:** `infra/rabbitmq/definitions.json`
- Queue definitions: `integration.{module}.{event-name}`
- Example: `integration.company.shipmentorder.created`

---

## Development Workflow

### Starting Infrastructure
```bash
# Basic (database + broker)
make start-tms

# With authentication (+ Keycloak + OAuth2 + NGINX)
make start-tms-oauth

# Full observability stack (+ Grafana + Loki + Prometheus + Jaeger)
make start-tms-with-observation
```

### Running the Application
```bash
# Using Maven
mvn spring-boot:run

# Or with specific profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### Running Tests
```bash
# All tests
mvn test

# Specific test
mvn test -Dtest=CompanyTest

# Integration tests only
mvn verify
```

### Building
```bash
mvn clean package
```

---

## Common Development Tasks

### Creating a New Use Case
1. Create use case class in `{module}/application/usecases/`
2. Add `@DomainService` and `@Cqrs(DatabaseRole.WRITE or READ)`
3. Define nested `Input` and `Output` records
4. Implement `execute(Input)` method
5. Create controller in `{module}/infrastructure/rest/`
6. Create request/response DTOs in `{module}/infrastructure/dto/`
7. Add tests

**Reference:** See `/doc/ai/prompts/new-use-case.md`

### Creating a New Module
1. Create package structure: `domain/`, `application/`, `infrastructure/`
2. Create module configuration class
3. Add to `TmsApplication` imports
4. Add module enable flag to `application.yml`
5. Create Flyway migrations
6. Create RabbitMQ queue bindings
7. Add integration tests

**Reference:** See `/doc/ai/prompts/new-module.md`

### Adding a Domain Event
1. Create event class extending `AbstractDomainEvent`
2. Add to aggregate method: `placeDomainEvent(new EventCreated(...))`
3. Create outbox entity in `infrastructure/outbox/`
4. Create DTO for external consumption in `infrastructure/dto/`
5. Add RabbitMQ queue binding
6. Create listener in consuming module if needed

**Reference:** See `/doc/ai/prompts/add-domain-event.md`

---

## Testing Strategy

### Domain Tests
- **No Spring context** - pure Java unit tests
- Test aggregates, value objects, domain logic
- Fast, isolated, no external dependencies
- **Example:** `CompanyTest.java`

### Use Case Tests (TODO - Not yet implemented)
- Test application logic with mocked repositories
- Verify orchestration and validation
- **Example:** `CreateCompanyUseCaseTest.java`

### Integration Tests
- Full Spring context with Testcontainers
- PostgreSQL + RabbitMQ containers
- Test complete flows (REST → use case → database → events)
- **Example:** `TmsApplicationTests.java`

### Modularity Tests
- Verify module boundaries with Spring Modulith
- Ensure modules don't violate dependencies
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

## Observability

### Traces
- **Jaeger:** http://localhost:16686
- Automatic instrumentation via OpenTelemetry

### Metrics
- **Prometheus:** http://localhost:9090
- Custom metrics for use cases, controllers, repositories

### Logs
- **Loki:** http://localhost:3100
- Structured JSON logs with correlation IDs
- Centralized via OpenTelemetry Collector

### Dashboards
- **Grafana:** http://localhost:3000 (admin/admin)
- Pre-configured dashboards for TMS metrics

---

## Technology Stack Details

### Core
- **Java 21** - Virtual threads, records, pattern matching
- **Spring Boot 3.x** - Latest stable
- **Spring Modulith** - Module boundary enforcement

### Persistence
- **PostgreSQL** - Primary database
- **Flyway** - Database migrations
- **Spring Data JPA** - Persistence layer
- **Hibernate** - ORM (but domain is independent)

### Messaging
- **RabbitMQ** - Event streaming
- **Spring AMQP** - RabbitMQ integration

### Observability
- **OpenTelemetry** - Traces, metrics, logs
- **Jaeger** - Distributed tracing
- **Prometheus** - Metrics collection
- **Loki** - Log aggregation
- **Grafana** - Visualization

### Security
- **Keycloak** - Authentication & authorization
- **OAuth2-Proxy** - Reverse proxy for token validation
- **NGINX** - Edge gateway with API key validation

### Development
- **Testcontainers** - Integration testing
- **Lombok** - Boilerplate reduction (JPA entities only)
- **Maven** - Build & dependency management

---

## Important Constraints

1. **Domain purity:** No framework dependencies in domain layer
2. **Immutability:** Domain objects return new instances on updates
3. **Event-driven communication:** Modules interact only via events
4. **CQRS annotations:** Mandatory on all use cases and controllers
5. **UUID v7 for IDs:** Time-based, sequential identifiers
6. **Outbox pattern:** All events persisted before publishing

---

## Helpful Resources

### Documentation
- **Architecture Guide:** `/doc/ai/ARCHITECTURE.md` (comprehensive)
- **Code Examples:** `/doc/ai/examples/` (working patterns)
- **Testing Guide:** `/doc/TESTING_GUIDE.md`
- **Glossary:** `/doc/GLOSSARY.md` (ubiquitous language)
- **Contributing:** `/doc/CONTRIBUTING.md`

### Architecture Decisions
- **ADR-001:** ID Format (UUID v7)
- **ADR-002:** Log Pattern
- **See:** `/doc/adr/`

### Technical Debt
- **Tracking:** `/doc/debt/DEBT.md`

---

## Quick Commands Reference

```bash
# Infrastructure
make start-tms              # Start basic infrastructure
make start-tms-oauth        # Start with auth
make start-tms-with-observation  # Start with full observability
make stop-tms               # Stop services
make down-tms-all           # Stop and remove all

# Build & Test
mvn clean package           # Build application
mvn test                    # Run tests
mvn spring-boot:run         # Run application

# Database
# Migrations run automatically via Docker service 'tms-flyway'

# View Logs
docker logs tms-database
docker logs tms-broker
docker logs tms-nginx
```

---

## For New Developers

1. Read `README.md` for project overview
2. Follow `HELP.md` for environment setup
3. Study `/doc/ai/ARCHITECTURE.md` for patterns
4. Review code examples in `/doc/ai/examples/`
5. Check ADRs in `/doc/adr/` for key decisions
6. Start with a simple task: Add a getter to existing aggregate

---

## For AI Assistants

When working with this codebase:
- Respect layer boundaries strictly
- Follow existing patterns exactly
- Never mutate domain objects
- Place events in aggregates, not use cases
- Always use `@Cqrs` annotation
- Generate tests alongside code
- Ask if uncertain about module boundaries

**Primary reference:** `/doc/ai/ARCHITECTURE.md`
