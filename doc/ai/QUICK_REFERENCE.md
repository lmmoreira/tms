# Quick Reference Guide

**Quick lookup for common TMS patterns and commands.**

---

## Essential Annotations

```java
// Use Cases
@DomainService                          // Mark as domain service
@Cqrs(DatabaseRole.WRITE)              // Write operation
@Cqrs(DatabaseRole.READ)               // Read operation

// Controllers
@RestController                         // REST controller
@RequestMapping("path")                 // Base path
@Cqrs(DatabaseRole.WRITE or READ)      // Must match use case

// Event Listeners
@Component                              // Spring component
@Cqrs(DatabaseRole.WRITE)              // Always WRITE
@Lazy(false)                           // Register at startup
@RabbitListener(queues = "queue-name") // Listen to queue

// JPA
@Entity                                 // JPA entity
@Table(name = "table_name")            // Table name
@Id                                     // Primary key
@Column(name = "column_name")          // Column name
```

---

## ⚠️ Common Pitfalls

### JPA Entity Dangers

```java
// ❌ DANGER - Lombok @Data on entities with relationships
@Entity
@Data  // Generates equals/hashCode using ALL fields → circular references
public class CompanyEntity {
    @OneToMany(mappedBy = "company")
    private Set<ShipmentOrderEntity> orders;  // Will cause StackOverflowError
}

// ✅ CORRECT - ID-only equals/hashCode
@Entity
@Getter
@Setter
public class CompanyEntity {
    @Id
    private UUID id;
    
    @OneToMany(mappedBy = "company")
    private Set<ShipmentOrderEntity> orders;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompanyEntity that)) return false;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

// ❌ DANGER - Transient entity as FK reference
public static ShipmentOrderJpaEntity from(ShipmentOrder aggregate) {
    final CompanyEntity company = new CompanyEntity();  // NOT persisted!
    company.setId(aggregate.getCompanyId().value());
    entity.setCompany(company);  // TransientObjectException
}

// ✅ CORRECT - Use resolver function
public static ShipmentOrderJpaEntity from(
    ShipmentOrder aggregate,
    Function<UUID, CompanyEntity> companyResolver  // Fetches persisted entity
) {
    entity.setCompany(companyResolver.apply(aggregate.getCompanyId().value()));
}
```

### REST API Path Variables

```java
// ❌ WRONG - Path variable not in method signature
@RestController
@RequestMapping("companies/{companyId}/agreements")
public class CreateAgreementController {
    @PostMapping
    public Object create(@RequestBody CreateAgreementDTO dto) {
        // companyId is in URL but NOT ACCESSIBLE!
    }
}

// ✅ CORRECT - All path variables in method signature
@RestController
@RequestMapping("companies/{companyId}/agreements")
public class CreateAgreementController {
    @PostMapping
    public Object create(@PathVariable UUID companyId,  // Must be here
                        @RequestBody CreateAgreementDTO dto) {
        // companyId now accessible
    }
    
    @GetMapping("/{agreementId}")
    public Object getById(@PathVariable UUID companyId,    // Both
                         @PathVariable UUID agreementId) {  // required
        // ...
    }
}
```

---

## Code Snippets

### Value Objects (Records)

```java
// ID Value Object
public record {Entity}Id(UUID value) {
    public {Entity}Id {
        if (value == null) {
            throw new ValidationException("Invalid value for {Entity}Id");
        }
    }

    public static {Entity}Id unique() {
        return new {Entity}Id(Id.unique());
    }

    public static {Entity}Id with(final UUID value) {
        return new {Entity}Id(value);
    }
}

// Validated String
public record {ValueObject}(String value) {
    public {ValueObject} {
        if (value == null || !isValid(value)) {
            throw new ValidationException("Invalid {ValueObject}");
        }
    }
    
    private static boolean isValid(String value) {
        // validation logic
    }
}

// Map Value Object
public record {ValueObject}(Map<String, Object> value) {
    public {ValueObject} {
        if (value == null || value.isEmpty()) {
            throw new ValidationException("{ValueObject} cannot be null or empty");
        }
        value = Collections.unmodifiableMap(value);
    }

    public static {ValueObject} with(final Map<String, Object> value) {
        return new {ValueObject}(value);
    }
}
```

### Immutable Aggregate Pattern

```java
public class {Aggregate} extends AbstractAggregateRoot {
    private final {Aggregate}Id id;
    // ... (other final fields)
    
    private {Aggregate}(/* all params */) {
        super(new HashSet<>(events), new HashMap<>(metadata));
        this.id = id;
        // ... (assignments)
    }
    
    public static {Aggregate} create{Aggregate}(params) {
        {Aggregate} aggregate = new {Aggregate}({Aggregate}Id.unique(), ...);
        aggregate.placeDomainEvent(new {Aggregate}Created(...));
        return aggregate;
    }
    
    public {Aggregate} updateField(NewValue newValue) {
        if (this.field.equals(newValue)) return this;
        {Aggregate} updated = new {Aggregate}(this.id, newValue, ...);
        updated.placeDomainEvent(new {Aggregate}Updated(...));
        return updated;
    }
    
    public {Aggregate}Id getId() { return id; }
}

// Full implementation: doc/ai/examples/complete-aggregate.md
// Pattern guide: .squad/skills/immutable-aggregate-update/SKILL.md
```

### Use Case Pattern

```java
@DomainService
@Cqrs(DatabaseRole.WRITE)
public class {Operation}UseCase implements UseCase<Input, Output> {
    
    private final {Aggregate}Repository repository;

    public {Operation}UseCase(final {Aggregate}Repository repository) {
        this.repository = repository;
    }

    @Override
    public Output execute(final Input input) {
        // 1. Validate
        // 2. Load/Create aggregate
        // 3. Execute business logic
        // 4. Persist
        // 5. Return output
    }

    public record Input(...) {}
    public record Output(...) {}
}
```

### Controller Pattern

```java
@RestController
@RequestMapping("path")
@Cqrs(DatabaseRole.WRITE)
public class {Operation}Controller {
    
    private final {Operation}UseCase useCase;
    private final DefaultRestPresenter presenter;
    private final RestUseCaseExecutor executor;

    @PostMapping
    public Object operation(@RequestBody RequestDTO dto) {
        return executor
            .from(useCase)
            .withInput(dto)
            .mapOutputTo(ResponseDTO.class)
            .presentWith(output -> presenter.present(output, HttpStatus.CREATED.value()))
            .execute();
    }
}
```

### Repository Implementation

```java
@Repository
public class {Aggregate}RepositoryImpl implements {Aggregate}Repository {
    
    private final {Aggregate}JpaRepository jpaRepository;
    private final OutboxService outboxService;

    @Override
    @Transactional
    public {Aggregate} create({Aggregate} aggregate) {
        {Aggregate}JpaEntity entity = {Aggregate}JpaEntity.from(aggregate);
        entity = jpaRepository.save(entity);
        outboxService.saveEvents(aggregate.getDomainEvents());
        return entity.toDomain();
    }
}
```

### Event Listener

```java
@Component
@Cqrs(DatabaseRole.WRITE)
@Lazy(false)
public class {Event}Listener {
    
    private final VoidUseCaseExecutor voidUseCaseExecutor;
    private final {Operation}UseCase useCase;

    public {Event}Listener(final VoidUseCaseExecutor voidUseCaseExecutor,
                           final {Operation}UseCase useCase) {
        this.voidUseCaseExecutor = voidUseCaseExecutor;
        this.useCase = useCase;
    }

    @RabbitListener(queues = "integration.{module}.{event}")
    public void handle(final {Event}DTO dto, final Channel channel, 
                      @Header(AmqpHeaders.DELIVERY_TAG) final long deliveryTag) {
        try {
            voidUseCaseExecutor
                .from(useCase)
                .withInput(new Input(...))
                .execute();
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            channel.basicNack(deliveryTag, false, true);
        }
    }
}
```

### Value Object (Record)

```java
public record {ValueObject}(Type value) {
    public {ValueObject} {
        if (value == null || !isValid(value)) {
            throw new ValidationException("Invalid {ValueObject}");
        }
    }
    
    private static boolean isValid(Type value) {
        // validation logic
    }
}
```

### Domain Event

```java
public class {Aggregate}{Action} extends AbstractDomainEvent {
    
    private final UUID aggregateId;
    private final Type payload;

    public {Aggregate}{Action}(UUID aggregateId, Type payload) {
        super(Id.unique(), aggregateId, Instant.now());
        this.aggregateId = aggregateId;
        this.payload = payload;
    }

    // Getters only
}
```

---

## Common Commands

### Build & Run

```bash
# Build
mvn clean install -DskipTests

# Run tests
mvn test

# Run specific test
mvn test -Dtest=ClassName

# Run application
mvn spring-boot:run

# Run with profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Database Migrations

**Location:** `/infra/database/migration/`

**Naming Convention:** `V{number}__{objective_description}.sql`
- `{number}`: Last migration number + 1 (e.g., V7, V8, V9)
- `{objective_description}`: Brief, clear purpose (e.g., `add_shipper_to_shipment_order`)

**Migration Examples:**

```sql
-- Create table
CREATE TABLE {schema}.{table_name} (
    id UUID NOT NULL,
    field_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (id)
);

-- Add column
ALTER TABLE {schema}.{table_name} ADD COLUMN {column_name} {type} NOT NULL;

-- Create index
CREATE INDEX idx_{table}_{column} ON {schema}.{table}({column});
```

**Applying Migrations:**
```bash
# Via Docker Compose (automatically applies on startup)
docker compose up

# Migrations are applied by Flyway container during compose up
# Check migration status in logs: docker compose logs tms-flyway
```

**Creating New Migration:**
1. Check last migration number: `ls infra/database/migration/`
2. Create new file: `V{N+1}__{description}.sql`
3. Write SQL (CREATE, ALTER, DROP, INDEX, etc.)
4. Run `docker compose up` to apply

### Git Workflow

```bash
# Conventional commits
git commit -m "feat(company): add get company by CNPJ use case"
git commit -m "fix(shipmentorder): correct order validation"
git commit -m "refactor(commons): improve error handling"
git commit -m "docs(ai): update architecture guide"

# Types: feat, fix, refactor, docs, test, chore
# Scopes: company, shipmentorder, commons, infra, docs
```

---

## File Templates

### New Use Case File

```
{module}/application/usecases/{Operation}{Aggregate}UseCase.java
```

### New Controller File

```
{module}/infrastructure/rest/{Operation}Controller.java
```

### New DTO Files

```
{module}/infrastructure/dto/{Operation}{Aggregate}DTO.java
{module}/infrastructure/dto/{Aggregate}ResponseDTO.java
```

### New Domain Event File

```
{module}/domain/events/{Aggregate}{Action}.java
```

### New Listener File

```
{module}/infrastructure/listener/{Event}Listener.java
```

---

## Validation Rules

### Domain Layer
❌ NO Spring annotations
❌ NO JPA annotations
❌ NO Jackson annotations
❌ NO HTTP/REST classes
✅ Pure Java only
✅ Business logic only

### Application Layer
❌ NO framework implementations
❌ NO JPA entities
❌ NO REST controllers
✅ Use case interfaces
✅ Repository interfaces
✅ Domain types only

### Infrastructure Layer
✅ All frameworks allowed
✅ Spring, JPA, Jackson
✅ REST, messaging
✅ Implements application interfaces

---

## Queue Naming

**Pattern:** `integration.{target-module}.{source-module}.{event}`

**Examples:**
- `integration.company.shipmentorder.created`
- `integration.shipmentorder.company.updated`

---

## REST Endpoint Conventions

```
POST   /{resource}              Create
GET    /{resource}/{id}         Get by ID
GET    /{resource}              List/Search
PUT    /{resource}/{id}         Update
PATCH  /{resource}/{id}         Partial update
DELETE /{resource}/{id}         Delete
```

---

## HTTP Request Files (Testing)

**Location:** `src/main/resources/{module}/request.http`

IntelliJ HTTP Client files for manual API testing with variable chaining.

**Basic Pattern:**
```http
@server = http://localhost:8080

### Create
POST {{server}}/resources
Content-Type: application/json

{ "name": "value" }

> {% client.global.set("resourceId", response.body.id) %}

### Get
GET {{server}}/resources/{{resourceId}}
```

**Dynamic Variables:**
- `{{$uuid}}` - Generate UUID
- `{{$timestamp}}` - Current timestamp
- `{{variableName}}` - Use stored variable

**Chaining Example:**
1. Create parent resource → save ID to variable
2. Create child resource using parent ID variable
3. Query using both variables

**Files:**
- `src/main/resources/company/request.http`
- `src/main/resources/shipmentorder/request.http`

---

## Common Errors & Solutions

### Error: "No qualifying bean of type..."
**Solution:** Check `@DomainService` annotation and configuration

### Error: "LazyInitializationException"
**Solution:** Use `@Transactional` or fetch eagerly

### Error: "ConstraintViolationException"
**Solution:** Check database constraints and entity validations

### Error: "Events not being published"
**Solution:** Check `@Lazy(false)` on listener, queue configuration

### Error: "Immutability violation"
**Solution:** Ensure update methods return new instances

---

## Testing Shortcuts

```java
// Mock repository
@Mock
private {Aggregate}Repository repository;

// Testcontainers
@Container
static PostgreSQLContainer<?> postgres = 
    new PostgreSQLContainer<>("postgres:16-alpine");

// MockMvc
mockMvc.perform(post("/path")
    .contentType(MediaType.APPLICATION_JSON)
    .content(json))
    .andExpect(status().isCreated());
```

---

## Performance Tips

✅ Use `@Transactional(readOnly = true)` for queries
✅ Use database indexes on frequently queried columns
✅ Use `@Cqrs(DatabaseRole.READ)` to route to read replica
✅ Batch operations when possible
✅ Use pagination for large result sets
✅ Cache frequently accessed data (carefully)

---

## Security Checklist

✅ Validate all inputs
✅ Use parameterized queries (JPA does this)
✅ Don't expose internal IDs unnecessarily
✅ Log security-relevant events
✅ Use HTTPS in production
✅ Implement authentication/authorization

---

## Documentation Standards

```java
/**
 * Brief description of class.
 * Additional details if needed.
 */
public class ClassName {
    
    /**
     * Description of method.
     * 
     * @param param Description of parameter
     * @return Description of return value
     * @throws ExceptionType When exception is thrown
     */
    public ReturnType methodName(ParamType param) {
        // implementation
    }
}
```

---

## Key Principles

1. **Immutability:** Domain objects never mutate
2. **Events in Aggregates:** Domain events placed in aggregate methods
3. **One Transaction:** Entity + Events saved together
4. **CQRS Everywhere:** All use cases and controllers annotated
5. **No Cross-Module Calls:** Only events for module communication
6. **Repository Abstraction:** Interface in application, impl in infrastructure
7. **Pure Domain:** Domain layer has zero framework dependencies

---

## When in Doubt

1. Check existing code in `company` or `shipmentorder` modules
2. Refer to `/doc/ai/examples/` for complete examples
3. Follow the patterns EXACTLY - consistency is key
4. Ask for clarification if unsure
5. Test thoroughly with unit and integration tests
