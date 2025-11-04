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

## Code Snippets

### Create New Aggregate

```java
public static {Aggregate} create{Aggregate}(params) {
    {Aggregate} aggregate = new {Aggregate}(
        {Aggregate}Id.unique(),
        params,
        new HashSet<>(),
        new HashMap<>()
    );
    aggregate.placeDomainEvent(new {Aggregate}Created(...));
    return aggregate;
}
```

### Update Aggregate (Immutable)

```java
public {Aggregate} updateField(NewValue newValue) {
    if (this.field.equals(newValue)) return this;
    
    {Aggregate} updated = new {Aggregate}(
        this.id,
        newValue,
        this.getDomainEvents(),
        this.getPersistentMetadata()
    );
    updated.placeDomainEvent(new {Aggregate}Updated(...));
    return updated;
}
```

### Use Case Pattern

```java
@DomainService
@Cqrs(DatabaseRole.WRITE)
public class {Operation}UseCase implements UseCase<Input, Output> {
    
    private final {Aggregate}Repository repository;

    @Override
    public Output execute(Input input) {
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
    
    @RabbitListener(queues = "integration.{module}.{event}")
    public void handle({Event}DTO dto, Channel channel, 
                      @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
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

### Database

```bash
# Create migration
# Create file: src/main/resources/{module}/db/migration/V{version}__{description}.sql

# Apply migrations
mvn liquibase:update

# Rollback
mvn liquibase:rollback -Dliquibase.rollbackCount=1
```

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
