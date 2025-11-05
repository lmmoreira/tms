# GitHub Copilot Instructions for TMS

## Quick Context

**Project:** Transportation Management System (TMS)  
**Architecture:** Modular Monolith with DDD, Hexagonal Architecture, CQRS, Event-Driven  
**Tech Stack:** Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Maven  
**Team:** Includes junior developers - prioritize clarity and consistency

---

## 🎯 Quick Decision Tree

**What do you need to do?**

```
CREATE something new?
├─ Aggregate? → Use "Essential Patterns #1" below + /doc/ai/prompts/new-aggregate.md
├─ Use Case? → Use "Essential Patterns #2" below + /doc/ai/prompts/new-use-case.md
├─ Controller? → Use "Essential Patterns #3" below + /doc/ai/prompts/new-controller.md
├─ Event Listener? → Use "Essential Patterns #6" below + /doc/ai/prompts/new-event-listener.md
├─ Database Migration? → See "Database Migrations" section + /doc/ai/prompts/new-migration.md
├─ HTTP Request File? → See /doc/ai/prompts/http-requests.md
└─ Module? → See /doc/ai/prompts/new-module.md

UNDERSTAND a pattern?
├─ Quick lookup? → See sections below or /doc/ai/QUICK_REFERENCE.md
├─ Full architecture? → See /doc/ai/ARCHITECTURE.md
├─ HTTP testing? → See /doc/ai/prompts/http-requests.md
└─ Project overview? → See /doc/ai/CODEBASE_CONTEXT.md

REVIEW code?
├─ Check patterns? → Compare against sections below
├─ Validation rules? → See "Critical Rules" and "Anti-Patterns" sections
└─ Examples? → See /doc/ai/examples/
```

---

## Essential Patterns

### 1. Use Case Pattern

**WRITE Operation:**
```java
@DomainService
@Cqrs(DatabaseRole.WRITE)
public class CreateCompanyUseCase implements UseCase<CreateCompanyUseCase.Input, CreateCompanyUseCase.Output> {
    
    private final CompanyRepository companyRepository;

    public CreateCompanyUseCase(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public Output execute(Input input) {
        // 1. Validation
        if (companyRepository.getCompanyByCnpj(new Cnpj(input.cnpj())).isPresent()) {
            throw new ValidationException("Company already exists");
        }
        
        // 2. Create via aggregate factory method
        Company company = Company.createCompany(input.name(), input.cnpj(), input.types(), input.configuration());
        
        // 3. Persist (repository handles event outbox)
        company = companyRepository.create(company);
        
        // 4. Return output
        return new Output(company.getCompanyId().value(), company.getName());
    }

    public record Input(String name, String cnpj, Set<CompanyType> types, Map<String, Object> configuration) {}
    public record Output(UUID companyId, String name) {}
}
```

**READ Operation:**
```java
@DomainService
@Cqrs(DatabaseRole.READ)
public class GetCompanyByIdUseCase implements UseCase<GetCompanyByIdUseCase.Input, GetCompanyByIdUseCase.Output> {
    
    private final CompanyRepository companyRepository;

    public GetCompanyByIdUseCase(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public Output execute(Input input) {
        Company company = companyRepository.getCompanyById(new CompanyId(input.companyId()))
                .orElseThrow(() -> new NotFoundException("Company not found"));

        return new Output(company.getCompanyId().value(), company.getName(), company.getCnpj().value());
    }

    public record Input(UUID companyId) {}
    public record Output(UUID companyId, String name, String cnpj) {}
}
```

**Key Points:**
- ✅ Annotate with `@DomainService` + `@Cqrs(DatabaseRole.WRITE or READ)`
- ✅ Input/Output as nested records
- ✅ Constructor injection only
- ✅ One operation per use case

---

### 2. REST Controller Pattern

```java
@RestController
@RequestMapping("companies")
@Cqrs(DatabaseRole.WRITE)
public class CreateController {

    private final CreateCompanyUseCase createCompanyUseCase;
    private final DefaultRestPresenter defaultRestPresenter;
    private final RestUseCaseExecutor restUseCaseExecutor;

    public CreateController(CreateCompanyUseCase createCompanyUseCase,
                           DefaultRestPresenter defaultRestPresenter,
                           RestUseCaseExecutor restUseCaseExecutor) {
        this.createCompanyUseCase = createCompanyUseCase;
        this.defaultRestPresenter = defaultRestPresenter;
        this.restUseCaseExecutor = restUseCaseExecutor;
    }

    @PostMapping
    public Object create(@RequestBody CreateCompanyDTO dto) {
        return restUseCaseExecutor
                .from(createCompanyUseCase)
                .withInput(dto)
                .mapOutputTo(CreateCompanyResponseDTO.class)
                .presentWith(output -> defaultRestPresenter.present(output, HttpStatus.CREATED.value()))
                .execute();
    }
}
```

**Key Points:**
- ✅ Zero business logic - only delegation
- ✅ Use `RestUseCaseExecutor` for orchestration
- ✅ DTOs for request/response
- ✅ Must have `@Cqrs` annotation

---

### 3. Immutable Aggregate Pattern

```java
public class Company extends AbstractAggregateRoot {

    private final CompanyId companyId;
    private final String name;
    private final Cnpj cnpj;

    // Private constructor
    private Company(CompanyId companyId, String name, Cnpj cnpj,
                   Set<AbstractDomainEvent> domainEvents, 
                   Map<String, Object> persistentMetadata) {
        super(new HashSet<>(domainEvents), new HashMap<>(persistentMetadata));
        
        if (companyId == null) throw new ValidationException("Invalid companyId");
        if (name == null || name.isBlank()) throw new ValidationException("Invalid name");
        
        this.companyId = companyId;
        this.name = name;
        this.cnpj = cnpj;
    }

    // Factory method
    public static Company createCompany(String name, String cnpj, Set<CompanyType> types, Map<String, Object> config) {
        Company company = new Company(CompanyId.unique(), name, new Cnpj(cnpj), new HashSet<>(), new HashMap<>());
        company.placeDomainEvent(new CompanyCreated(company.getCompanyId().value(), company.toString()));
        return company;
    }

    // Update returns NEW instance
    public Company updateName(String name) {
        if (this.name.equals(name)) return this;
        
        Company updated = new Company(this.companyId, name, this.cnpj, this.getDomainEvents(), this.getPersistentMetadata());
        updated.placeDomainEvent(new CompanyUpdated(updated.getCompanyId().value(), "name", this.name, name));
        return updated;
    }

    // Getters only
    public CompanyId getCompanyId() { return companyId; }
    public String getName() { return name; }
    public Cnpj getCnpj() { return cnpj; }
}
```

**Key Points:**
- ✅ ALWAYS immutable - updates return new instances
- ✅ Private constructor + public factory methods
- ✅ Domain events placed HERE, not in use cases
- ✅ Getters only, NO setters

---

### 4. Value Object Pattern

```java
public record Cnpj(String value) {
    public Cnpj {
        if (value == null || !isValid(value)) {
            throw new ValidationException("Invalid CNPJ format");
        }
    }
    
    private static boolean isValid(String cnpj) {
        return cnpj != null && cnpj.matches("\\d{14}");
    }
}
```

**Key Points:**
- ✅ Use Java `record`
- ✅ Validation in compact constructor
- ✅ Immutable by nature

---

### 5. Domain Event Pattern

```java
public class CompanyCreated extends AbstractDomainEvent {
    private final UUID aggregateId;
    private final String payload;

    public CompanyCreated(UUID aggregateId, String payload) {
        super(Id.unique(), aggregateId, Instant.now());
        this.aggregateId = aggregateId;
        this.payload = payload;
    }

    public UUID getAggregateId() { return aggregateId; }
    public String getPayload() { return payload; }
}
```

**Key Points:**
- ✅ Past tense naming (Created, Updated, Deleted)
- ✅ Always include aggregateId
- ✅ Place in aggregate methods via `placeDomainEvent()`

---

### 6. Event Listener Pattern (Module Communication)

```java
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
    public void handle(ShipmentOrderCreatedDTO dto, Message message, Channel channel) {
        voidUseCaseExecutor
                .from(incrementShipmentOrderUseCase)
                .withInput(new IncrementShipmentOrderUseCase.Input(dto.companyId()))
                .execute();
    }
}
```

**Key Points:**
- ✅ Modules communicate ONLY via events
- ✅ Use `@RabbitListener` for inter-module events
- ✅ Never call other module's repositories directly

---

## Database Migrations

**Location:** `/infra/database/migration/`

**Process:**
1. Migrations are versioned SQL files: `V{number}__{description}.sql`
2. Applied automatically by Flyway container during `docker compose up`
3. Run in sequential order (V1, V2, V3, ...)
4. Never modify existing migrations - create new ones

**Creating a Migration:**
```bash
# 1. Check last version
ls infra/database/migration/
# Example output: V1...V6__create_shipment_order_outbox.sql

# 2. Create V{N+1} file
touch infra/database/migration/V7__add_field_to_table.sql

# 3. Write SQL
echo "ALTER TABLE schema.table ADD COLUMN field UUID NOT NULL;" > infra/database/migration/V7__add_field_to_table.sql

# 4. Apply (automatic on compose up)
docker compose up
```

**Common Operations:**
```sql
-- Add column
ALTER TABLE {schema}.{table} ADD COLUMN {field} {type} {constraints};

-- Create table
CREATE TABLE {schema}.{table} (
    id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (id)
);

-- Create index
CREATE INDEX idx_{table}_{field} ON {schema}.{table}({field});
```

**Key Points:**
- ✅ Use sequential numbers (V7, V8, V9...)
- ✅ Clear, objective descriptions
- ✅ Check existing migrations to understand schemas
- ✅ Include schema name in SQL
- ❌ Never modify existing migrations

**See Also:** `/doc/ai/prompts/new-migration.md` for complete guide

---

## Critical Rules

### Layer Architecture (STRICT)

**Domain Layer** (`domain/`)
- ❌ NO Spring, JPA, Jackson, or ANY framework
- ✅ Pure Java only
- ✅ Business logic lives here

**Application Layer** (`application/`)
- ❌ NO Spring, JPA, Jackson, or ANY framework
- ✅ Use cases, repository interfaces
- ✅ Depends ONLY on domain
- ❌ NO HTTP, database, or messaging

**Infrastructure Layer** (`infrastructure/`)
- ✅ Controllers, JPA entities, DTOs, messaging
- ✅ All Spring annotations here
- ✅ Implements application interfaces

### Module Communication
- ✅ Event-driven: Modules communicate via RabbitMQ events
- ❌ NO direct repository calls between modules

### Immutability
- ✅ Domain objects NEVER mutate
- ✅ Update methods return NEW instances

### IDs
- ✅ Use `Id.unique()` for UUID v7 (time-based, sequential)
- ❌ Never use auto-increment

### CQRS
- ✅ EVERY use case and controller must have `@Cqrs(DatabaseRole.WRITE or READ)`

---

## Naming Conventions

- **Use Cases:** `{Verb}{Entity}UseCase` (e.g., `CreateCompanyUseCase`)
- **Controllers:** `{Verb}Controller` (e.g., `CreateController`)
- **Events:** `{Entity}{PastTense}` (e.g., `CompanyCreated`)
- **Repositories:** `{Entity}Repository` (interface), `{Entity}RepositoryImpl` (implementation)

---

## File Structure

```
{module}/
├── domain/              # Pure Java, no frameworks
│   ├── {Aggregate}.java
│   ├── {ValueObject}.java
│   └── {DomainEvent}.java
├── application/         # Use cases, repository interfaces
│   ├── usecases/
│   └── repositories/
└── infrastructure/      # Spring, JPA, REST, messaging
    ├── rest/
    ├── dto/
    ├── jpa/
    ├── repositories/
    └── listener/
```

---

## Commit Message Format (Conventional Commits)

```
<type>(<scope>): <description>

Types: feat, fix, refactor, docs, test, chore
Scopes: company, shipmentorder, commons, infra, docs

Examples:
feat(company): add get company by CNPJ use case
fix(shipmentorder): correct order validation
docs(ai): update architecture guide
```

---

## Testing Approach

**Domain Tests:** Pure unit tests, no Spring context
```java
@Test
void shouldCreateCompany() {
    Company company = Company.createCompany("Test", "12345678901234", types, config);
    assertNotNull(company.getCompanyId());
}
```

**Integration Tests:** Use Testcontainers for PostgreSQL + RabbitMQ
```java
@SpringBootTest
@Testcontainers
class CreateCompanyIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");
}
```

**Strategy:**
- ✅ Many unit tests (domain logic)
- ✅ Broad integration tests (full flows)

---

## Anti-Patterns (DO NOT DO)

❌ Mutable aggregates with setters
❌ Framework dependencies in domain layer
❌ Business logic in controllers
❌ Cross-module repository calls
❌ Domain events thrown from use cases
❌ Omitting `@Cqrs` annotation

---

## Quick Reference Links

- **Comprehensive Guide:** `/doc/ai/ARCHITECTURE.md`
- **Architecture Decisions:** `/doc/adr/`
- **Code Examples:** `/doc/ai/examples/`
- **Glossary:** `/doc/ai/GLOSSARY.md`
- **Codebase Context:** `/doc/ai/CODEBASE_CONTEXT.md`
- **Quick Reference:** `/doc/ai/QUICK_REFERENCE.md`
- **Readme:** `/doc/ai/README.md`

---

## For GitHub Copilot

When suggesting code:
1. Follow the patterns shown above EXACTLY
2. Respect layer boundaries strictly
3. Always return new instances for aggregate updates
4. Place events in aggregates, not use cases
5. Use proper annotations (`@DomainService`, `@Cqrs`)
6. Generate tests alongside code

**Remember:** Junior developers will read this code. Prioritize clarity and consistency over cleverness.
