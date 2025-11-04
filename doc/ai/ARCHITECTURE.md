# Architecture Documentation

## Purpose
This document provides comprehensive architectural guidance for AI assistants working on the TMS codebase. It describes the patterns, principles, and structural decisions that govern the system.

---

## System Overview

**TMS (Transportation Management System)** is a modular monolith built using:
- **Domain-Driven Design (DDD)** principles
- **Hexagonal Architecture** (Ports & Adapters)
- **CQRS** (Command Query Responsibility Segregation)
- **Event-Driven Architecture** with domain events
- **Spring Modulith** for module boundaries

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        External Clients                      │
│              (Marketplaces, Logistics Providers)             │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                      NGINX Edge Gateway                      │
│                  (API Key Validation)                        │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│             OAuth2-Proxy + Keycloak (Authentication)         │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                     TMS Application                          │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   Commons    │  │   Company    │  │ ShipmentOrder│     │
│  │   Module     │  │   Module     │  │   Module     │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
│                                                              │
└─────────┬──────────────────────────────────────┬───────────┘
          │                                       │
          ▼                                       ▼
┌──────────────────┐                    ┌─────────────────┐
│   PostgreSQL     │                    │    RabbitMQ     │
│  (Write / Read)  │                    │ (Domain Events) │
└──────────────────┘                    └─────────────────┘
```

---

## Architectural Patterns

### 1. Modular Monolith with Spring Modulith

**Concept**: The application is organized as a monolith with well-defined module boundaries enforced at runtime.

**Module Structure**:
```
src/main/java/br/com/logistics/tms/
├── commons/           # Shared domain primitives & infrastructure
├── company/           # Company bounded context
└── shipmentorder/     # ShipmentOrder bounded context
```

**Module Activation**: Modules can be enabled/disabled via environment variables:
```yaml
modules:
  commons:
    enabled: ${MODULES_COMMONS_ENABLED}
  company:
    enabled: ${MODULES_COMPANY_ENABLED}
  order:
    enabled: ${MODULES_ORDER_ENABLED}
```

**Why**: Allows for monorepo development with the flexibility to extract modules into microservices later without code changes.

---

### 2. Hexagonal Architecture (Ports & Adapters)

Each module follows a **3-layer structure**:

```
module/
├── domain/              # CORE - Business logic, entities, value objects
├── application/         # PORTS - Use cases, repository interfaces
└── infrastructure/      # ADAPTERS - REST controllers, JPA, messaging
```

#### Layer Responsibilities

##### **Domain Layer** (Inner Circle)
- **Pure business logic** - No framework dependencies
- Contains:
  - **Aggregates**: Entities that are consistency boundaries (e.g., `Company`, `ShipmentOrder`)
  - **Value Objects**: Immutable objects (e.g., `CompanyId`, `Cnpj`, `Agreement`)
  - **Domain Events**: Events raised by aggregates (e.g., `CompanyCreated`, `CompanyUpdated`)
  - **Domain Exceptions**: Business validation failures

**Rules**:
- No references to `application` or `infrastructure` layers
- No Spring annotations
- No database or external I/O
- Immutable aggregates (updates return new instances)

##### **Application Layer** (Middle Circle)
- **Orchestration** of business use cases
- Contains:
  - **Use Cases**: Single-responsibility operations (e.g., `CreateCompanyUseCase`)
  - **Repository Interfaces**: Domain-defined contracts (e.g., `CompanyRepository`)
  - **Presenters**: Output formatting contracts

**Rules**:
- Depends only on `domain` layer
- No knowledge of infrastructure details (HTTP, database, etc.)
- Uses repository interfaces, not implementations

##### **Infrastructure Layer** (Outer Circle)
- **Technical implementation** of ports
- Contains:
  - **REST Controllers**: HTTP endpoints
  - **Repository Implementations**: JPA-based persistence
  - **JPA Entities**: Database mapping
  - **DTOs**: Data transfer objects for API contracts
  - **Message Listeners**: RabbitMQ consumers
  - **Outbox Pattern**: Event publishing

**Rules**:
- Implements interfaces defined in `application` layer
- Contains all Spring annotations
- Handles infrastructure concerns (transactions, serialization, etc.)

---

### 3. Domain-Driven Design (DDD)

#### Aggregates

**Definition**: Cluster of domain objects treated as a single unit for data changes.

**Example - Company Aggregate**:
```java
public class Company extends AbstractAggregateRoot {
    private final CompanyId companyId;      // Aggregate Root ID
    private final String name;
    private final Cnpj cnpj;                // Value Object
    private final CompanyTypes companyTypes;
    private final Configurations configurations;
    private final Set<Agreement> agreements; // Entities within aggregate
    
    // Business methods that maintain invariants
    public Company updateName(String name) { ... }
    public void addAgreement(Agreement agreement) { ... }
}
```

**Key Characteristics**:
1. **Aggregate Root**: `Company` is the entry point - all operations go through it
2. **Invariants**: Business rules are enforced within aggregate methods
3. **Transactional Boundary**: Changes to aggregate are atomic
4. **Domain Events**: Aggregates raise events when state changes

#### Value Objects

**Immutable objects** defined by their attributes, not identity.

Examples:
- `CompanyId` - Wraps UUID v7
- `Cnpj` - Brazilian business identifier with validation
- `Agreement` - Contract between companies
- `Configurations` - Key-value settings

**Pattern**:
```java
public record Cnpj(String value) {
    public Cnpj {
        // Validation in compact constructor
        if (value == null || !isValid(value)) {
            throw new ValidationException("Invalid CNPJ");
        }
    }
}
```

#### Domain Events

**Purpose**: Communicate that something meaningful happened in the domain.

**Pattern**:
```java
public class CompanyCreated extends AbstractDomainEvent {
    private final UUID aggregateId;
    private final String payload;
    
    public CompanyCreated(UUID aggregateId, String payload) {
        super(Id.unique(), aggregateId, Instant.now());
        this.aggregateId = aggregateId;
        this.payload = payload;
    }
}
```

**Event Flow**:
1. Aggregate method places event: `placeDomainEvent(new CompanyCreated(...))`
2. Repository persists aggregate + events to outbox table
3. Outbox publisher sends events to RabbitMQ
4. Other modules/services consume events

---

### 4. CQRS (Command Query Responsibility Segregation)

**Concept**: Separate read operations from write operations.

**Implementation**:
- **Write Operations**: Use write database (`@Cqrs(DatabaseRole.WRITE)`)
- **Read Operations**: Use read replica (`@Cqrs(DatabaseRole.READ)`)

**Configuration**:
```yaml
datasource:
  write:
    url: jdbc:postgresql://${DB_WRITE_HOST}:${DB_WRITE_PORT}/${DB_WRITE_NAME}
  read:
    url: jdbc:postgresql://${DB_READ_HOST}:${DB_READ_PORT}/${DB_READ_NAME}
```

**Usage**:
```java
@DomainService
@Cqrs(DatabaseRole.WRITE)
public class CreateCompanyUseCase implements UseCase<Input, Output> {
    // Writes to write database
}

@DomainService
@Cqrs(DatabaseRole.READ)
public class GetCompanyByIdUseCase implements UseCase<Input, Output> {
    // Reads from read replica
}
```

**Mode Control**:
```yaml
app:
  cqrs:
    mode: ${APP_CQRS_MODE}  # Can be: ENABLED, DISABLED, WRITE_ONLY, READ_ONLY
```

---

### 5. Event-Driven Architecture

#### Domain Events Pattern

**Purpose**: Decouple modules and enable eventual consistency.

**Outbox Pattern Implementation**:
1. **Aggregate** places events in memory collection
2. **Repository** saves aggregate + events to outbox table **in same transaction**
3. **Outbox Publisher** polls outbox table and publishes to RabbitMQ
4. **Event Listeners** in other modules consume events

**Example - Company Module**:
```java
// 1. Domain raises event
company.placeDomainEvent(new CompanyCreated(company.getId(), company.toString()));

// 2. Repository saves both
@Override
public Company create(Company company) {
    companyJpaRepository.save(CompanyEntity.of(company));
    outboxGateway.save(COMPANY_SCHEMA, company.getDomainEvents(), CompanyOutboxEntity.class);
    return company;
}

// 3. Outbox publisher sends to RabbitMQ (automatically)

// 4. Other modules listen - Company module listening to ShipmentOrderCreated event
@Component
@Cqrs(DatabaseRole.WRITE)
@Lazy(false)
public class IncrementShipmentOrderListener {

    private final VoidUseCaseExecutor voidUseCaseExecutor;
    private final IncrementShipmentOrderUseCase incrementShipmentOrderUseCase;

    public IncrementShipmentOrderListener(VoidUseCaseExecutor voidUseCaseExecutor,
                                          IncrementShipmentOrderUseCase incrementShipmentOrderUseCase) {
        this.voidUseCaseExecutor = voidUseCaseExecutor;
        this.incrementShipmentOrderUseCase = incrementShipmentOrderUseCase;
    }

    @RabbitListener(queues = "integration.company.shipmentorder.created")
    public void handle(ShipmentOrderCreatedDTO shipmentOrderCreated, Message message, Channel channel) {
        voidUseCaseExecutor
                .from(incrementShipmentOrderUseCase)
                .withInput(new IncrementShipmentOrderUseCase.Input(shipmentOrderCreated.companyId()))
                .execute();
    }

}
```

**Benefits**:
- **Guaranteed delivery**: Events stored in database before publishing
- **Decoupling**: Modules don't directly depend on each other
- **Asynchronous**: Non-blocking communication
- **Audit trail**: All domain events are stored

---

## Immutability & Functional Patterns

### Immutable Aggregates

**Principle**: Domain objects never mutate - updates return new instances.

**Pattern**:
```java
public Company updateName(String name) {
    if (this.name.equals(name)) 
        return this; // No change
    
    // Create new instance with updated value
    Company updated = new Company(
        this.companyId,
        name,  // New value
        this.cnpj,
        this.companyTypes,
        this.configurations,
        this.agreements,
        this.getDomainEvents(),
        this.getPersistentMetadata()
    );
    
    updated.placeDomainEvent(new CompanyUpdated(...));
    return updated;
}
```

**Why**:
- Thread-safe by default
- Clear audit trail (events capture all changes)
- Easier to reason about state changes
- Fits functional programming paradigms

---

## Technical Decisions

### UUID v7 (ULID) for IDs

**Decision**: Use time-based sequential UUIDs instead of auto-increment or random UUIDs.

**Implementation**:
```java
public record Id() {
    public static UUID unique() {
        return Generators.timeBasedEpochGenerator().generate();
    }
}
```

**Benefits**:
- Time-ordered (better database index performance)
- No database roundtrip needed
- Domain remains independent of persistence
- Sortable by creation time

**Reference**: See [ADR-001](../adr/ADR-001-ID-Format.md)

---

### Virtual Threads (Java 21)

**Configuration**:
```yaml
spring:
  threads:
    virtual:
      enabled: true
```

**Impact**:
- High concurrency with low resource usage
- Blocking I/O doesn't starve thread pool
- Requires async logging (configured in Logback)

---

### Read/Write Database Separation

**Current Setup**: Both point to same PostgreSQL instance (or read replica).

**Annotation-Based Routing**:
```java
@Cqrs(DatabaseRole.WRITE) // Routes to write datasource
@Cqrs(DatabaseRole.READ)  // Routes to read datasource
```

**Future-Proof**: Can switch read operations to different database type (e.g., Elasticsearch) without code changes.

**Technical Debt**: See [DEBT-010](../debt/DEBT.md) - Need to support mixed read/write transactions.

---

## Observability

### OpenTelemetry Integration

**Enabled by default** with traces, metrics, and logs sent to OTEL Collector.

**Configuration**:
```yaml
otel:
  exporter:
    otlp:
      protocol: grpc
      endpoint: http://0.0.0.0:4317
  instrumentation:
    jdbc:
      enabled: true  # Auto-instrument database calls
```

**Stack**:
- **Traces**: Jaeger
- **Metrics**: Prometheus
- **Logs**: Loki
- **Visualization**: Grafana

---

## Request Flow Example

**Creating a Company**:

```
1. Client → NGINX (API key validation)
2. NGINX → OAuth2-Proxy (Bearer token validation)
3. OAuth2-Proxy → Keycloak (Token verification)
4. Validated request → TMS Application

5. CreateController receives HTTP POST
   ↓
6. RestUseCaseExecutor orchestrates:
   - Maps DTO → UseCase.Input
   - Executes CreateCompanyUseCase
   - Maps UseCase.Output → ResponseDTO
   - Applies presenter (status 201)
   ↓
7. CreateCompanyUseCase:
   - Validates CNPJ doesn't exist
   - Calls Company.createCompany() (domain method)
   - Company places CompanyCreated event
   - Saves via CompanyRepository
   ↓
8. CompanyRepositoryImpl:
   - Persists CompanyEntity (JPA)
   - Saves domain events to outbox table
   - Returns domain object
   ↓
9. Background: Outbox publisher
   - Polls outbox table
   - Publishes CompanyCreated to RabbitMQ
   ↓
10. Other modules consume event (if listening)
```

---

## Module Communication Rules

### Internal Module Communication

**Rule**: Modules communicate **only through domain events**.

**❌ Don't**:
```java
// Direct repository call to another module
private final CompanyRepository companyRepository; // Wrong in ShipmentOrder module
```

**✅ Do**:
```java
// Listen to events from other modules - example Company module listening ShipmentOrderCreated event
public class IncrementShipmentOrderListener {

    private final VoidUseCaseExecutor voidUseCaseExecutor;
    private final IncrementShipmentOrderUseCase incrementShipmentOrderUseCase;

    public IncrementShipmentOrderListener(VoidUseCaseExecutor voidUseCaseExecutor,
                                          IncrementShipmentOrderUseCase incrementShipmentOrderUseCase) {
        this.voidUseCaseExecutor = voidUseCaseExecutor;
        this.incrementShipmentOrderUseCase = incrementShipmentOrderUseCase;
    }

    @RabbitListener(queues = "integration.company.shipmentorder.created")
    public void handle(ShipmentOrderCreatedDTO shipmentOrderCreated, Message message, Channel channel) {
        voidUseCaseExecutor
                .from(incrementShipmentOrderUseCase)
                .withInput(new IncrementShipmentOrderUseCase.Input(shipmentOrderCreated.companyId()))
                .execute();
    }

}
```

### External API Communication

**Rule**: Each module exposes its own REST API under its namespace.

**Structure**:
- `/companies/**` - Company module endpoints
- `/shipment-orders/**` - ShipmentOrder module endpoints

---

## Testing Strategy

### Test Levels

1. **Domain Tests**: Pure unit tests for aggregates and value objects
   - No Spring context
   - Fast, isolated
   - Example: `CompanyTest.java`

2. **Use Case Tests**: Test application logic with mocked repositories
   - No Spring context (TODO: currently incomplete)
   - Example: `CreateCompanyUseCaseTest.java`

3. **Integration Tests**: Full module testing with Testcontainers
   - PostgreSQL + RabbitMQ containers
   - Tests full flow including persistence
   - Example: `TmsApplicationTests.java`

4. **Modularity Tests**: Verify module boundaries
   - Spring Modulith verification
   - Example: `ModularityTests.java`

---

## Key Architectural Constraints

1. **Domain Layer Purity**: No framework dependencies in domain
2. **Immutability**: Domain objects return new instances, never mutate
3. **Event-Driven**: Inter-module communication via events only
4. **Aggregate Boundaries**: All changes go through aggregate root
5. **Repository Pattern**: Infrastructure hidden behind interfaces
6. **CQRS Annotations**: All use cases and controllers must specify database role
7. **Outbox Pattern**: All domain events persisted before publishing

---

## For AI Assistants

When generating code:

1. **Always respect layer boundaries**:
   - Domain → No external dependencies
   - Application → Depends only on domain
   - Infrastructure → Implements application interfaces

2. **Follow the use case pattern**:
   - One use case per operation
   - Input/Output records nested inside use case
   - Annotated with `@DomainService` and `@Cqrs`

3. **Domain events**:
   - Always place events in aggregate methods
   - Never throw events directly from use cases

4. **Immutability**:
   - Update methods return new instances
   - Use records for value objects

5. **Testing**:
   - Domain tests with no frameworks
   - Integration tests with Testcontainers

6. **IDs**:
   - Always use `Id.unique()` for new entities
   - Never use auto-increment

7. **Transactions**:
   - Repository methods handle transactions
   - One transaction per use case execution

---

## References

- [ADR-001: ID Format](../adr/ADR-001-ID-Format.md)
- [ADR-002: Log Pattern](../adr/ADR-002-Log-Pattern.md)
- [Technical Debt](../debt/DEBT.md)
- [Spring Modulith Documentation](https://docs.spring.io/spring-modulith/reference/)
- [DDD Reference](https://www.domainlanguage.com/ddd/reference/)
