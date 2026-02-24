# JsonSingleton Usage Pattern

## ⚡ TL;DR

- **When:** Parsing JSON in event listeners, serializing domain events, handling Map<String, Object> conversions, or configuring Spring components with ObjectMapper
- **Why:** Avoid injecting ObjectMapper (breaks layer boundaries and adds unnecessary coupling)
- **Pattern:** `JsonSingleton.getInstance().fromJson(jsonString, TargetClass.class)` / `toJson(object)`
- **Confidence:** Low (pattern exists and is widely used, but no dedicated doc until now)

---

## Pattern

### Parsing JSON (Event Listeners)

Event listeners consume DTO payloads from RabbitMQ. Use JsonSingleton to deserialize without injecting ObjectMapper:

```java
@Component
@Cqrs(DatabaseRole.WRITE)
@Lazy(false)
public class CompanyCreatedListener {

    private final VoidUseCaseExecutor voidUseCaseExecutor;
    private final SynchronizeCompanyUseCase synchronizeCompanyUseCase;
    private final Mapper mapper;

    public CompanyCreatedListener(final VoidUseCaseExecutor voidUseCaseExecutor,
                                  final SynchronizeCompanyUseCase synchronizeCompanyUseCase,
                                  final Mapper mapper) {
        this.voidUseCaseExecutor = voidUseCaseExecutor;
        this.synchronizeCompanyUseCase = synchronizeCompanyUseCase;
        this.mapper = mapper;
    }

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = "integration.shipmentorder.company.created")
    public void handle(final CompanyCreatedDTO event, final Message message, final Channel channel) {
        // Mapper internally uses JsonSingleton for DTO → Map conversion
        voidUseCaseExecutor
                .from(synchronizeCompanyUseCase)
                .withInput(new SynchronizeCompanyUseCase.Input(
                    event.companyId(), 
                    mapper.map(event, Map.class)
                ))
                .execute();
    }
}
```

### Serializing Domain Events (Outbox Pattern)

When persisting domain events to the outbox table, serialize them using JsonSingleton:

```java
@MappedSuperclass
@Data
public abstract class AbstractOutboxEntity {

    @Id
    private UUID id;

    @JdbcTypeCode(SqlTypes.JSON)
    private String content;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    public static <T extends AbstractOutboxEntity> T of(AbstractDomainEvent event, Class<T> entityClass) {
        try {
            T entity = entityClass.getDeclaredConstructor().newInstance();
            entity.setId(event.getDomainEventId());
            // ✅ Serialize domain event to JSON string for storage
            entity.setContent(JsonSingleton.getInstance().toJson(event));
            entity.setAggregateId(event.getAggregateId());
            entity.setStatus(OutboxStatus.NEW);
            entity.setCreatedAt(event.getOccurredOn().atOffset(ZoneOffset.UTC));
            entity.setType(event.getType());
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create outbox entity", e);
        }
    }
}
```

### Deserializing from Outbox (Publishing Events)

When processing outbox entries, deserialize the stored JSON back to domain events:

```java
@Component
@Cqrs(DatabaseRole.WRITE)
public class OutboxGatewayImpl implements OutboxGateway {

    @Async
    public void process(String schemaName, int batchSize, Class<? extends AbstractOutboxEntity> entityClass) {
        // ... fetch outbox entries ...

        result.parallelStream().forEach(outbox -> {
            try {
                final Class<?> eventClass = DomainEventRegistry.getClass(schemaName, outbox.getType());

                // ✅ Deserialize JSON string back to domain event
                final AbstractDomainEvent event = (AbstractDomainEvent) JsonSingleton.getInstance()
                        .fromJson(outbox.getContent(), eventClass);
                
                UUID correlationId = outbox.getId();
                domainEventQueueGateway.publish(event, correlationId, this::onSuccess, this::onFailure);
            } catch (Exception e) {
                logable.error(getClass(), "Failed to process outbox message with ID {}: {}", 
                    outbox.getId(), e.getMessage());
            }
        });
    }
}
```

### Type Safety with Generics

When working with generic types like `Map<String, Object>`, use `@SuppressWarnings("unchecked")`:

```java
@SuppressWarnings("unchecked")
final Map<String, Object> data = JsonSingleton.getInstance()
        .fromJson(jsonString, Map.class);
```

**Why suppress warnings?** Java's type erasure means `Map.class` loses the generic type parameters at runtime. The warning is unavoidable, but the pattern is safe when you control the JSON structure.

### Configuring Spring Beans

Spring components that need ObjectMapper (like Jackson2JsonMessageConverter) get it via `JsonSingleton.registeredMapper()`:

```java
@Configuration
public class RabbitMQConfig {

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        // ✅ Use registered mapper for Spring beans
        return new Jackson2JsonMessageConverter(JsonSingleton.registeredMapper());
    }
}
```

**Why `.registeredMapper()` instead of `.getInstance()`?**  
- `.registeredMapper()` returns a **copy** of the configured ObjectMapper
- Safe for Spring to modify without affecting the singleton instance
- `.getInstance()` returns the JsonAdapter interface for application code

---

## When to Use

- ✅ Event listeners parsing DTO JSON payloads from RabbitMQ
- ✅ Outbox pattern serializing/deserializing domain events
- ✅ Converting between `Map<String, Object>` and JSON strings
- ✅ Infrastructure layer needing JSON operations (gateways, publishers)
- ✅ Spring configuration beans requiring a configured ObjectMapper
- ✅ Any layer that needs JSON but cannot inject ObjectMapper

---

## Anti-Pattern: Never Inject ObjectMapper

❌ **WRONG:**
```java
@Component
public class CompanyCreatedListener {
    private final ObjectMapper objectMapper;  // DON'T DO THIS
    
    public CompanyCreatedListener(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "integration.shipmentorder.company.created")
    public void handle(CompanyCreatedDTO event) {
        // Using injected mapper violates single source of truth
        Map<String, Object> data = objectMapper.convertValue(event, Map.class);
    }
}
```

✅ **CORRECT:**
```java
@Component
public class CompanyCreatedListener {
    // No ObjectMapper injection
    private final Mapper mapper;  // Mapper internally uses JsonSingleton
    
    public CompanyCreatedListener(Mapper mapper) {
        this.mapper = mapper;
    }

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = "integration.shipmentorder.company.created")
    public void handle(CompanyCreatedDTO event) {
        // Use framework Mapper or JsonSingleton directly
        Map<String, Object> data = mapper.map(event, Map.class);
    }
}
```

**Why?**
1. **Single source of truth:** JsonSingleton centralizes ObjectMapper configuration (date handling, null inclusion, module registration)
2. **Layer independence:** Infrastructure layer can use JSON without Spring injection
3. **Consistency:** All JSON operations use the same configuration
4. **Testability:** No need to mock ObjectMapper in tests

---

## Implementation Details

### JsonSingleton Class Location
`src/main/java/br/com/logistics/tms/commons/infrastructure/json/JsonSingleton.java`

### Singleton Initialization
JsonSingleton lazily initializes on first use with these configurations:
- **BlackbirdModule:** High-performance Jackson extension
- **JavaTimeModule:** Java 8+ date/time support
- **Date serialization:** ISO-8601 strings (not timestamps)
- **Unknown properties:** Ignored (fail-safe deserialization)
- **Null values:** Excluded from serialization
- **BigDecimal:** Plain notation (no scientific notation)

### API Methods

**JsonAdapter Interface:**
```java
public interface JsonAdapter {
    String toJson(Object object);
    <T> T fromJson(String json, Class<T> clazz);
}
```

**Access Points:**
```java
// For application code (serialization/deserialization)
JsonAdapter adapter = JsonSingleton.getInstance();
String json = adapter.toJson(myObject);
MyClass obj = adapter.fromJson(jsonString, MyClass.class);

// For Spring beans (configuration)
ObjectMapper mapper = JsonSingleton.registeredMapper();  // Returns a copy
```

### Thread Safety
- `getInstance()` is `synchronized` — safe for concurrent initialization
- Once initialized, the singleton is immutable and thread-safe
- `registeredMapper()` returns a **copy** — safe for concurrent modification by Spring

---

## Related Patterns

- **Eventual Consistency:** Event listeners use JsonSingleton to parse DTO payloads from RabbitMQ
- **Outbox Pattern:** Stores domain events as JSON strings using JsonSingleton serialization
- **Event-Driven Architecture:** All inter-module communication serializes events via JsonSingleton
- **Layer Independence:** Infrastructure layer avoids Spring dependency injection for JSON operations

---

## Metadata

**Confidence:** `low`  
**Applies To:** `[company, shipmentorder, commons, all future modules]`  
**Replaces:** `[copilot-instructions.md § JSON Serialization scattered examples]`  
**Token Cost:** `~200 lines`  
**Related Skills:** `[eventual-consistency-pattern, outbox-pattern, event-listener-pattern]`  
**Created:** `2026-02-24`  
**Last Updated:** `2026-02-24`
