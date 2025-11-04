# Prompt: Add New Module

## Purpose
Template for creating a complete new bounded context module in TMS.

---

## Instructions for AI Assistant

Create a complete module following TMS modular monolith architecture.

### Required Information

**Module Details:**
- **Module Name:** `[module-name]` (e.g., quotation, delivery, invoice)
- **Description:** `[Purpose and responsibility of this module]`
- **Primary Aggregate:** `[MainAggregate]` (e.g., Quotation, Delivery)

**Aggregates:**
```
1. [AggregateName]
   - Description: [What it represents]
   - Key value objects: [List]
   - Key operations: [List]
```

**Module Dependencies:**
```
- Depends on: [Other modules this module needs events from]
- Publishes events to: [Modules that should listen to this module]
```

---

## Module Structure

```
{module-name}/
├── domain/                           # Pure Java - NO frameworks
│   ├── {Aggregate}.java             # Main aggregate root
│   ├── {Aggregate}Id.java           # Aggregate ID value object
│   ├── {ValueObject}.java           # Domain value objects
│   ├── {Entity}.java                # Entities within aggregate (if any)
│   └── events/                      # Domain events
│       ├── {Aggregate}Created.java
│       ├── {Aggregate}Updated.java
│       └── {Aggregate}Deleted.java
│
├── application/                      # Use cases and interfaces
│   ├── usecases/                    # Business operations
│   │   ├── Create{Aggregate}UseCase.java
│   │   ├── Get{Aggregate}ByIdUseCase.java
│   │   ├── Update{Aggregate}UseCase.java
│   │   └── Delete{Aggregate}ByIdUseCase.java
│   └── repositories/                # Repository interfaces
│       └── {Aggregate}Repository.java
│
└── infrastructure/                   # Spring, JPA, REST, messaging
    ├── config/                      # Configuration
    │   ├── {Module}UseCaseConfig.java
    │   └── {Module}RabbitMQConfig.java
    ├── rest/                        # REST controllers
    │   ├── CreateController.java
    │   ├── GetByIdController.java
    │   ├── UpdateController.java
    │   └── DeleteController.java
    ├── dto/                         # Data transfer objects
    │   ├── Create{Aggregate}DTO.java
    │   ├── {Aggregate}ResponseDTO.java
    │   └── Update{Aggregate}DTO.java
    ├── jpa/                         # JPA entities
    │   ├── {Aggregate}JpaEntity.java
    │   └── {Aggregate}JpaRepository.java
    ├── repositories/                # Repository implementations
    │   └── {Aggregate}RepositoryImpl.java
    └── listener/                    # Event listeners
        └── {EventName}Listener.java
```

---

## Implementation Checklist

### 1. Domain Layer (Pure Java)

#### Aggregate Root
- [ ] `{Aggregate}.java`
  - [ ] Extends `AbstractAggregateRoot`
  - [ ] All fields `final`
  - [ ] Private constructor
  - [ ] Static factory method `create{Aggregate}(...)`
  - [ ] Static `reconstruct(...)` method
  - [ ] Immutable update methods
  - [ ] Domain events placed in methods
  - [ ] Business logic methods
  - [ ] Getters only

#### Value Objects
- [ ] `{Aggregate}Id.java`
  - [ ] Record extending `Id`
  - [ ] Static `unique()` method
- [ ] `{ValueObject}.java`
  - [ ] Records with validation

#### Domain Events
- [ ] `{Aggregate}Created.java`
- [ ] `{Aggregate}Updated.java`
- [ ] Additional events as needed
  - [ ] Extend `AbstractDomainEvent`
  - [ ] Past tense naming
  - [ ] Include aggregate ID

### 2. Application Layer

#### Use Cases
- [ ] `Create{Aggregate}UseCase.java`
  - [ ] `@DomainService` + `@Cqrs(DatabaseRole.WRITE)`
  - [ ] Nested Input/Output records
  - [ ] Validation logic
  - [ ] Call aggregate factory method
  - [ ] Call repository
- [ ] `Get{Aggregate}ByIdUseCase.java`
  - [ ] `@DomainService` + `@Cqrs(DatabaseRole.READ)`
- [ ] `Update{Aggregate}UseCase.java`
  - [ ] `@DomainService` + `@Cqrs(DatabaseRole.WRITE)`
- [ ] `Delete{Aggregate}ByIdUseCase.java`
  - [ ] `@DomainService` + `@Cqrs(DatabaseRole.WRITE)`

#### Repository Interface
- [ ] `{Aggregate}Repository.java`
  - [ ] Methods return domain objects
  - [ ] Use domain value objects as parameters

### 3. Infrastructure Layer

#### Configuration
- [ ] `{Module}UseCaseConfig.java`
  - [ ] `@Configuration`
  - [ ] Bean definitions for use cases
- [ ] `{Module}RabbitMQConfig.java`
  - [ ] Queue declarations
  - [ ] Exchange declarations
  - [ ] Bindings

#### REST Controllers
- [ ] `CreateController.java`
  - [ ] `@RestController` + `@RequestMapping`
  - [ ] `@Cqrs(DatabaseRole.WRITE)`
  - [ ] Use `RestUseCaseExecutor`
- [ ] `GetByIdController.java`
  - [ ] `@Cqrs(DatabaseRole.READ)`
- [ ] Other controllers as needed

#### DTOs
- [ ] Request DTOs
  - [ ] Implement `UseCaseInputMapper<Input>`
  - [ ] `@JsonProperty` annotations
- [ ] Response DTOs
  - [ ] Implement `UseCaseOutputMapper<Output>`
  - [ ] Static `from()` method

#### JPA
- [ ] `{Aggregate}JpaEntity.java`
  - [ ] `@Entity` + `@Table`
  - [ ] `toDomain()` method
  - [ ] Static `from()` method
  - [ ] `updateFrom()` method
- [ ] `{Aggregate}JpaRepository.java`
  - [ ] Extends `JpaRepository`

#### Repository Implementation
- [ ] `{Aggregate}RepositoryImpl.java`
  - [ ] `@Repository`
  - [ ] Inject `JpaRepository` and `OutboxService`
  - [ ] `@Transactional` on write operations
  - [ ] Save events to outbox

### 4. Database Schema

#### Migration File
- [ ] Location: `src/main/resources/{module-name}/db/migration/V{version}__{description}.sql`
- [ ] Tables:
  - [ ] Main aggregate table
  - [ ] Child entity tables (if any)
  - [ ] Association tables (if any)
- [ ] Indexes
- [ ] Constraints

Example:
```sql
-- V001__create_quotation_tables.sql

CREATE TABLE quotations (
    quotation_id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    configuration JSONB,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_quotations_company_id ON quotations(company_id);
CREATE INDEX idx_quotations_status ON quotations(status);
CREATE INDEX idx_quotations_created_at ON quotations(created_at);
```

### 5. Module Configuration

#### Application Properties
- [ ] Location: `src/main/resources/{module-name}/application-{module}.yml`
- [ ] Module-specific configuration

Example:
```yaml
# application-quotation.yml
spring:
  liquibase:
    change-log: classpath:quotation/db/changelog/db.changelog-master.yaml

tms:
  quotation:
    validity-days: 30
    auto-approval-threshold: 10000
```

### 6. Tests

#### Domain Tests
- [ ] `{Aggregate}Test.java`
  - [ ] Test creation
  - [ ] Test updates (immutability)
  - [ ] Test business rules
  - [ ] Test validation
  - [ ] Test domain events

#### Use Case Tests
- [ ] `Create{Aggregate}UseCaseTest.java`
  - [ ] Mock repository
  - [ ] Test success case
  - [ ] Test validation errors
  - [ ] Test business rule violations

#### Integration Tests
- [ ] `{Module}IntegrationTest.java`
  - [ ] `@SpringBootTest` + `@Testcontainers`
  - [ ] Test complete flows
  - [ ] Test with real database

#### REST Tests
- [ ] `CreateControllerTest.java`
  - [ ] Test HTTP endpoints
  - [ ] Test request/response mapping

---

## package-info.java Files

Add to each package for documentation:

```java
// domain/package-info.java
/**
 * Domain layer for {Module} module.
 * Pure Java - NO framework dependencies.
 * Contains aggregates, entities, value objects, and domain events.
 */
package br.com.logistics.tms.{module}.domain;

// application/package-info.java
/**
 * Application layer for {Module} module.
 * Contains use cases and repository interfaces.
 * Depends only on domain layer.
 */
package br.com.logistics.tms.{module}.application;

// infrastructure/package-info.java
/**
 * Infrastructure layer for {Module} module.
 * Contains REST controllers, JPA entities, DTOs, and messaging.
 * Implements application interfaces.
 */
package br.com.logistics.tms.{module}.infrastructure;
```

---

## Integration Points

### Publishing Events

When this module needs to notify others:
```java
// In aggregate
public static {Aggregate} create{Aggregate}(...) {
    // ...
    aggregate.placeDomainEvent(new {Aggregate}Created(...));
    return aggregate;
}

// RabbitMQ Config
@Bean
public TopicExchange {module}Exchange() {
    return new TopicExchange("integration.{module}.events");
}
```

### Consuming Events

When this module needs to listen to other modules:
```java
// Create listener in infrastructure/listener/
@Component
@Lazy(false)
public class {EventName}Listener {
    @RabbitListener(queues = "integration.{this-module}.{other-module}.{event}")
    public void handle({EventName}DTO dto, ...) {
        // Process event
    }
}
```

---

## Documentation

Create module-specific docs:

- [ ] `doc/{module-name}/README.md`
  - [ ] Module purpose
  - [ ] Key concepts
  - [ ] API endpoints
  - [ ] Events published/consumed

- [ ] `doc/{module-name}/ARCHITECTURE.md`
  - [ ] Module structure
  - [ ] Key design decisions
  - [ ] Integration points

---

## Validation Checklist

Before completing:

✅ **Layer Boundaries:** No framework in domain layer
✅ **Immutability:** All aggregates immutable
✅ **Events:** Placed in aggregates, saved to outbox
✅ **CQRS:** All use cases and controllers annotated
✅ **REST:** All controllers use `RestUseCaseExecutor`
✅ **Tests:** Coverage for domain, use cases, and integration
✅ **Documentation:** package-info.java in all packages
✅ **Database:** Migration scripts created
✅ **Configuration:** Module config files added

---

## Example Module Creation Command

```
Please create a new module named "quotation" with:
- Main aggregate: Quotation
- Value objects: QuotationId, Money, QuotationStatus
- Operations: Create, GetById, UpdateStatus, Delete
- Publishes: QuotationCreated, QuotationApproved
- Consumes: CompanyCreated (to validate company exists)
```

---

## Common Patterns to Follow

✅ **Naming:** Consistent with existing modules
✅ **Structure:** Mirror company/shipmentorder structure
✅ **Configuration:** Follow existing config patterns
✅ **Testing:** Same test structure as other modules
✅ **Documentation:** Same documentation format
