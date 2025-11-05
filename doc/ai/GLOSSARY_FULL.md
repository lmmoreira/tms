# TMS Glossary

## Purpose
This glossary defines the **ubiquitous language** used throughout the TMS codebase. It ensures consistent terminology across code, documentation, and conversations between developers and domain experts.

---

## Domain Terms

### Company Module

#### Company
An organization that participates in logistics operations. Can be a **shipper** (sends goods), **carrier** (transports goods), or **logistics provider** (manages transportation).

**Synonyms:** Organization, Business Entity  
**Related:** CompanyType, Agreement

#### CNPJ
**Cadastro Nacional da Pessoa Jurídica** - Brazilian business tax identification number consisting of 14 digits. Unique identifier for legal entities in Brazil.

**Format:** `XX.XXX.XXX/XXXX-XX` (stored as 14 digits without formatting)  
**Example:** `12.345.678/0001-90`  
**Related:** Cnpj (value object)

#### Company Type
Classification of a company's role in logistics operations:
- **SHIPPER** - Sends goods (e.g., e-commerce marketplaces like Shein, Shopee)
- **CARRIER** - Transports goods (e.g., Correios, Loggi)
- **LOGISTICS_PROVIDER** - Manages transportation operations

**Related:** CompanyTypes (value object collection)

#### Agreement
A contract between companies defining service terms, pricing, and conditions for transportation services. Represents business relationships.

**Components:**
- Agreement ID
- Agreement Type
- Conditions
- Active status

**Related:** AgreementType, AgreementCondition

#### Agreement Type
Classification of business agreements:
- **SUPPLIER** - Company provides services
- **CLIENT** - Company receives services

#### Configuration
Key-value settings that customize company operations and behavior. Stored as flexible map structure.

**Examples:**
- Notification preferences
- API endpoints
- Business rules overrides

**Implementation:** `Map<String, Object>`

#### Shipment Order Counter
Number tracking how many shipment orders a company has processed. Incremented via domain events when orders are created.

**Purpose:** Business analytics, volume tracking

---

### ShipmentOrder Module

#### Shipment Order
A request to transport goods from origin to destination. Represents the entire lifecycle of a transportation operation.

**Synonyms:** Order, Transportation Request  
**Status:** Created, In Transit, Delivered, Cancelled  
**Related:** Company (shipper), Volume, Quotation

#### Volume
Physical characteristics of shipment cargo:
- Weight (kg)
- Dimensions (length × width × height in cm)
- Quantity (number of packages)

**Purpose:** Calculate shipping costs, plan vehicle capacity

#### Quotation
Price estimate for transportation services based on:
- Origin and destination
- Volume and weight
- Service type and urgency
- Route and carrier

**Status:** Pending, Approved, Rejected, Expired

---

## Technical Terms (DDD & Architecture)

### Aggregate
A cluster of domain objects treated as a single unit for data changes. Has a root entity (Aggregate Root) through which all operations must pass.

**Examples in TMS:**
- `Company` (Aggregate Root)
- `ShipmentOrder` (Aggregate Root)

**Characteristics:**
- Enforces business invariants
- Transactional boundary
- Raises domain events
- Reference other aggregates by ID only

**Related:** Aggregate Root, Entity, Value Object

### Aggregate Root
The main entity in an aggregate that serves as the entry point for all operations. Only the aggregate root can be referenced by other aggregates.

**Responsibilities:**
- Enforce consistency rules
- Control access to internal entities
- Raise domain events
- Provide public interface

**Examples:** `Company`, `ShipmentOrder`

### Value Object
An immutable object defined by its attributes rather than identity. Two value objects with same attributes are considered equal.

**Examples in TMS:**
- `CompanyId` - UUID wrapper
- `Cnpj` - Brazilian business ID
- `Agreement` - Contract details
- `Configurations` - Settings map

**Characteristics:**
- Immutable (no setters)
- Validated in constructor
- Implemented as Java records
- Compared by value, not reference

**Related:** Aggregate, Entity

### Entity
An object with a distinct identity that persists over time. Two entities with same attributes but different IDs are considered different.

**Distinguishing Feature:** Has an ID (CompanyId, AgreementId, etc.)

**Examples:**
- `Company` (also an Aggregate Root)
- `Agreement` (entity within Company aggregate)

**Related:** Aggregate Root, Value Object

### Domain Event
An immutable fact representing something meaningful that happened in the domain. Used to communicate between aggregates and modules.

**Naming Convention:** Past tense (e.g., `CompanyCreated`, `ShipmentOrderCreated`)

**Examples:**
- `CompanyCreated` - New company registered
- `CompanyUpdated` - Company information changed
- `ShipmentOrderCreated` - New order placed

**Characteristics:**
- Immutable
- Contains aggregate ID
- Timestamp of occurrence
- Persisted to outbox before publishing

**Related:** Event-Driven Architecture, Outbox Pattern

### Use Case
A single application operation representing one way to accomplish a task. Orchestrates domain logic without containing business rules.

**Naming Convention:** `{Verb}{Entity}UseCase`

**Examples:**
- `CreateCompanyUseCase`
- `GetCompanyByIdUseCase`
- `UpdateCompanyUseCase`

**Structure:**
- Nested `Input` record (parameters)
- Nested `Output` record (result)
- `execute(Input)` method (orchestration)

**Annotations:**
- `@DomainService`
- `@Cqrs(DatabaseRole.WRITE or READ)`

**Related:** Application Layer, CQRS

### Repository
An abstraction that provides collection-like access to aggregates. Hides persistence details from domain logic.

**Pattern:**
- Interface in `application/` layer
- Implementation in `infrastructure/` layer

**Examples:**
- `CompanyRepository` (interface)
- `CompanyRepositoryImpl` (JPA implementation)

**Methods:**
- `create()`, `update()`, `delete()`
- `getById()`, `getByCnpj()`

**Related:** Hexagonal Architecture, Ports & Adapters

---

## Architectural Patterns

### Domain-Driven Design (DDD)
Software design approach that focuses on modeling the business domain. Emphasizes collaboration between domain experts and developers using ubiquitous language.

**Key Concepts:** Aggregate, Entity, Value Object, Domain Event, Repository, Ubiquitous Language

### Hexagonal Architecture (Ports & Adapters)
Architectural pattern that separates business logic from external concerns.

**Layers:**
- **Domain** (Core) - Business logic, no frameworks
- **Application** (Ports) - Use cases, repository interfaces
- **Infrastructure** (Adapters) - REST, JPA, messaging implementations

**Benefits:** Testable, framework-independent domain, easy to swap implementations

### CQRS (Command Query Responsibility Segregation)
Pattern that separates read operations (queries) from write operations (commands).

**In TMS:**
- Write operations use `@Cqrs(DatabaseRole.WRITE)` → write database
- Read operations use `@Cqrs(DatabaseRole.READ)` → read replica

**Benefits:** Optimize each side independently, scale reads separately, eventual consistency support

**Related:** Event-Driven Architecture, Database Role

### Event-Driven Architecture
Architectural style where components communicate through events rather than direct calls.

**In TMS:**
- Modules communicate exclusively via domain events
- RabbitMQ for event streaming
- Outbox pattern for reliable delivery

**Benefits:** Loose coupling, asynchronous processing, audit trail

### Outbox Pattern
Pattern ensuring reliable event publishing by storing events in database before sending to message broker.

**Flow:**
1. Aggregate places event in memory
2. Repository saves aggregate + events to outbox table (same transaction)
3. Background publisher reads outbox and sends to RabbitMQ
4. Marks events as published

**Benefits:** Guaranteed delivery, transactional consistency, no event loss

**Related:** Domain Event, Event-Driven Architecture

### Modular Monolith
Architecture organizing a single application into well-defined modules with enforced boundaries.

**In TMS:**
- Modules: `commons`, `company`, `shipmentorder`
- Spring Modulith enforces boundaries
- Modules can be extracted to microservices later

**Benefits:** Simplicity of monolith, modularity of microservices, easy refactoring

---

## Technical Concepts

### Layer (in Hexagonal Architecture)

#### Domain Layer
The core business logic layer containing aggregates, value objects, and domain events. **Must have zero framework dependencies.**

**Location:** `{module}/domain/`  
**Contains:** Aggregates, Value Objects, Domain Events, Domain Exceptions  
**Rules:** Pure Java only, no Spring/JPA/Jackson

#### Application Layer
The orchestration layer containing use cases and repository interfaces.

**Location:** `{module}/application/`  
**Contains:** Use Cases, Repository Interfaces, Presenters (interfaces)  
**Dependencies:** Can only depend on domain layer

#### Infrastructure Layer
The technical implementation layer containing all framework code.

**Location:** `{module}/infrastructure/`  
**Contains:** REST Controllers, JPA Entities, Repository Implementations, DTOs, Message Listeners  
**Dependencies:** Can depend on domain and application layers

### Database Role
Enum specifying which database (read or write) an operation should use in CQRS pattern.

**Values:**
- `DatabaseRole.WRITE` - Write database (master)
- `DatabaseRole.READ` - Read database (replica)

**Usage:** `@Cqrs(DatabaseRole.WRITE)`

### UUID v7 (ULID)
Time-based UUID variant that is sequential and sortable. Used for all entity IDs in TMS.

**Format:** `018c7e7a-3d21-7a1f-8e3f-4d5a6b7c8d9e`

**Benefits:**
- Time-ordered (better database performance)
- Generated in application (no DB roundtrip)
- Sortable by creation time
- Compatible with standard UUID

**Generation:** `Id.unique()`

**Reference:** See `/doc/adr/ADR-001-ID-Format.md`

### DTO (Data Transfer Object)
Simple object used to transfer data between layers or systems. Contains no business logic.

**In TMS:**
- Request DTOs: `CreateCompanyDTO`
- Response DTOs: `CreateCompanyResponseDTO`
- Event DTOs: `ShipmentOrderCreatedDTO`

**Location:** `{module}/infrastructure/dto/`

**Characteristics:**
- Java records (immutable)
- Validation annotations (e.g., `@NotNull`)
- Serializable (Jackson)

### JPA Entity
Database mapping class used by Hibernate/JPA. Lives in infrastructure layer.

**Examples:** `CompanyEntity`, `AgreementEntity`

**Important:** JPA entities are NOT domain objects. Repository converts between them.

**Location:** `{module}/infrastructure/jpa/entities/`

---

## Process Terms

### Domain Service
A use case or service that operates on domain objects. Marked with `@DomainService` annotation.

**Not to confuse with:** Domain logic in aggregates (which should be preferred)

**Use when:** Operation involves multiple aggregates or doesn't naturally fit in one aggregate

### Factory Method
Static method on aggregate that creates new instances. Used instead of public constructors.

**Examples:**
- `Company.createCompany()` - Creates new company
- `Id.unique()` - Generates new UUID

**Benefits:** Clear intent, encapsulates creation logic, enforces invariants

### Presenter
Component that formats use case output for specific presentation (REST, GraphQL, etc.).

**In TMS:** `DefaultRestPresenter` formats output with HTTP status codes

**Pattern:** Presenter interfaces in application layer, implementations in infrastructure

---

## Infrastructure Terms

### Module (Spring Modulith)
Self-contained unit of functionality with explicit dependencies. Can be enabled/disabled via configuration.

**In TMS:**
- `commons` - Shared infrastructure
- `company` - Company management
- `shipmentorder` - Order management

**Configuration:** `application.yml` → `modules.{module}.enabled`

### Flyway Migration
Versioned SQL script that evolves database schema.

**Location:** `infra/database/migration/`  
**Naming:** `V{version}__{description}.sql`  
**Example:** `V001__create_company_table.sql`

### RabbitMQ Queue
Message queue for asynchronous event processing.

**Naming Convention:** `integration.{module}.{event-name}`  
**Examples:**
- `integration.company.created`
- `integration.company.shipmentorder.created`

### Outbox Table
Database table storing domain events before they're published to message broker.

**Tables:**
- `company_outbox` - Company module events
- `shipmentorder_outbox` - ShipmentOrder module events

**Columns:** event_id, aggregate_id, event_type, payload, created_at, published_at

---

## Testing Terms

### Testcontainers
Library providing lightweight, throwaway instances of databases, message brokers, etc., for testing.

**In TMS:** PostgreSQL and RabbitMQ containers for integration tests

**Benefits:** Real infrastructure, isolated tests, reproducible environments

### Integration Test
Test that verifies complete flow from REST API to database, including all layers.

**Example:** `TmsApplicationTests.java`

**Uses:** Spring context, Testcontainers, real HTTP requests

### Domain Test
Pure unit test focusing on domain logic without frameworks.

**Example:** `CompanyTest.java`

**Characteristics:** No Spring context, no mocks, fast execution

### Use Case Test
Test focusing on application logic with mocked repositories (TODO - not yet implemented).

**Purpose:** Verify orchestration and validation without persistence

---

## Abbreviations

- **TMS** - Transportation Management System
- **DDD** - Domain-Driven Design
- **CQRS** - Command Query Responsibility Segregation
- **DTO** - Data Transfer Object
- **JPA** - Java Persistence API
- **ORM** - Object-Relational Mapping
- **REST** - Representational State Transfer
- **UUID** - Universally Unique Identifier
- **CNPJ** - Cadastro Nacional da Pessoa Jurídica
- **ULID** - Universally Unique Lexicographically Sortable Identifier

---

## References

- **Ubiquitous Language:** Eric Evans, Domain-Driven Design
- **Architecture Patterns:** `/doc/ai/ARCHITECTURE.md`
- **Code Examples:** `/doc/ai/examples/`
- **ADRs:** `/doc/adr/`
