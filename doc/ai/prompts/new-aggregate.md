# Prompt: Create New Aggregate

## ⚡ TL;DR

- **When:** Creating a new business entity with identity and lifecycle
- **Why:** DDD aggregate pattern with immutability and domain events
- **Pattern:** Private constructor + factory methods, update returns new instance
- **See:** Read on for complete template

---

## Purpose
Template for creating a new aggregate root in a TMS module.

---

## Instructions for AI Assistant

Create a complete aggregate implementation following DDD and TMS immutability patterns.

### Required Information

**Aggregate Details:**
- **Aggregate Name:** `[EntityName]` (e.g., Company, ShipmentOrder, Quotation)
- **Module:** `[company | shipmentorder | quotation | other]`
- **Description:** `[Brief description of the aggregate's purpose]`

**Aggregate Root ID:**
```
- ID Type: {Name}Id (e.g., CompanyId, QuotationId)
```

**Value Objects:**
```
- ValueObject1: Type (description)
- ValueObject2: Type (description)
```

**Entities (if any within aggregate):**
```
- Entity1: Description
- Entity2: Description
```

**Business Operations:**
```
- Operation1: Description (e.g., "Create new aggregate", "Update name", "Add agreement")
- Operation2: Description
```

**Domain Events:**
```
- Event1: {Aggregate}Created (when aggregate is first created)
- Event2: {Aggregate}Updated (when aggregate properties change)
- Event3: Custom events (describe when they occur)
```

**Business Invariants (Rules that must always be true):**
```
- Invariant1: [Description]
- Invariant2: [Description]
```

---

## Implementation Checklist

Generate the following files:

### 1. Aggregate Root Class
**Location:** `src/main/java/br/com/logistics/tms/{module}/domain/{Aggregate}.java`

**Pattern (TL;DR):**
```java
public class {Aggregate} extends AbstractAggregateRoot {
    // All fields MUST be final (immutability)
    private final {Aggregate}Id {aggregate}Id;
    private final String name;
    private final {ValueObject} valueObject;
    private final Set<{Entity}> entities;

    // Private constructor - forces use of factory methods
    private {Aggregate}(/* all params */) {
        super(new HashSet<>(domainEvents), new HashMap<>(persistentMetadata));
        // Invariant validation
        if ({aggregate}Id == null) throw new ValidationException("Invalid {aggregate}Id");
        this.{aggregate}Id = {aggregate}Id;
        this.name = name;
        // ... (other assignments)
    }

    // Factory method for creation
    public static {Aggregate} create{Aggregate}(...) {
        {Aggregate} aggregate = new {Aggregate}({Aggregate}Id.unique(), ...);
        aggregate.placeDomainEvent(new {Aggregate}Created(...));
        return aggregate;
    }

    // Update method - returns NEW instance (immutable)
    public {Aggregate} updateName(String name) {
        if (this.name.equals(name)) return this;
        {Aggregate} updated = new {Aggregate}(this.{aggregate}Id, name, ...);
        updated.placeDomainEvent(new {Aggregate}Updated(...));
        return updated;
    }

    // Getters only - NO SETTERS
    public {Aggregate}Id get{Aggregate}Id() { return {aggregate}Id; }
    public String getName() { return name; }
}

// Full implementation with all methods: doc/ai/examples/complete-aggregate.md
// Pattern guide: .squad/skills/immutable-aggregate-update/SKILL.md
```

### 2. Aggregate Root ID (Value Object)
**Location:** `src/main/java/br/com/logistics/tms/{module}/domain/{Aggregate}Id.java`

**Pattern:**
```java
public record {Aggregate}Id(UUID value) {

    public {Aggregate}Id {
        if (value == null) {
            throw new ValidationException("Invalid {aggregate}Id");
        }
    }

    public static {Aggregate}Id unique() {
        return new {Aggregate}Id(Id.unique());
    }

    public static {Aggregate}Id of(UUID value) {
        return new {Aggregate}Id(value);
    }

    public static {Aggregate}Id of(String value) {
        return new {Aggregate}Id(UUID.fromString(value));
    }
}
```

### 3. Domain Events
**Location:** `src/main/java/br/com/logistics/tms/{module}/domain/{Aggregate}Created.java`

**{Aggregate}Created Event:**
```java
public class {Aggregate}Created extends AbstractDomainEvent {

    private final UUID aggregateId;
    private final String payload;

    public {Aggregate}Created(UUID aggregateId, String payload) {
        super(Id.unique(), aggregateId, Instant.now());
        this.aggregateId = aggregateId;
        this.payload = payload;
    }

    public UUID getAggregateId() { return aggregateId; }
    public String getPayload() { return payload; }
}
```

**{Aggregate}Updated Event:**
```java
public class {Aggregate}Updated extends AbstractDomainEvent {

    private final UUID aggregateId;
    private final String fieldName;
    private final Object oldValue;
    private final Object newValue;

    public {Aggregate}Updated(UUID aggregateId, String fieldName, Object oldValue, Object newValue) {
        super(Id.unique(), aggregateId, Instant.now());
        this.aggregateId = aggregateId;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public UUID getAggregateId() { return aggregateId; }
    public String getFieldName() { return fieldName; }
    public Object getOldValue() { return oldValue; }
    public Object getNewValue() { return newValue; }
}
```

### 4. Repository Interface
**Location:** `src/main/java/br/com/logistics/tms/{module}/application/repositories/{Aggregate}Repository.java`

**Pattern:**
```java
public interface {Aggregate}Repository {
    {Aggregate} create({Aggregate} aggregate);
    Optional<{Aggregate}> get{Aggregate}ById({Aggregate}Id id);
    {Aggregate} update({Aggregate} aggregate);
    void deleteById({Aggregate}Id id);
    
    // Custom queries if needed
    Optional<{Aggregate}> get{Aggregate}By{Field}({FieldType} field);
}
```

### 5. Domain Tests
**Location:** `src/test/java/br/com/logistics/tms/{module}/domain/{Aggregate}Test.java`

**Pattern:**
```java
class {Aggregate}Test {

    @Test
    void shouldCreate{Aggregate}WithValidData() {
        // Given
        String name = "Test Name";
        {ValueObject} valueObject = new {ValueObject}(...);

        // When
        {Aggregate} aggregate = {Aggregate}.create{Aggregate}(name, valueObject, ...);

        // Then
        assertNotNull(aggregate.get{Aggregate}Id());
        assertEquals(name, aggregate.getName());
        assertEquals(valueObject, aggregate.get{ValueObject}());
        
        // Verify domain event
        assertEquals(1, aggregate.getDomainEvents().size());
        assertTrue(aggregate.getDomainEvents().stream()
                .anyMatch(e -> e instanceof {Aggregate}Created));
    }

    @Test
    void shouldThrowExceptionWhenCreatingWithInvalidData() {
        // Given
        String invalidName = "";

        // When & Then
        assertThrows(ValidationException.class, () ->
                {Aggregate}.create{Aggregate}(invalidName, ...)
        );
    }

    @Test
    void shouldUpdateNameAndReturnNewInstance() {
        // Given
        {Aggregate} aggregate = {Aggregate}.create{Aggregate}("Original", ...);
        String newName = "Updated";

        // When
        {Aggregate} updated = aggregate.updateName(newName);

        // Then
        assertNotSame(aggregate, updated);  // Different instances
        assertEquals("Original", aggregate.getName());  // Original unchanged
        assertEquals(newName, updated.getName());  // Updated has new value
        
        // Verify domain event
        assertTrue(updated.getDomainEvents().stream()
                .anyMatch(e -> e instanceof {Aggregate}Updated));
    }

    @Test
    void shouldReturnSameInstanceWhenUpdateWithSameValue() {
        // Given
        String name = "Test";
        {Aggregate} aggregate = {Aggregate}.create{Aggregate}(name, ...);

        // When
        {Aggregate} result = aggregate.updateName(name);

        // Then
        assertSame(aggregate, result);  // Same instance returned
    }

    @Test
    void shouldAdd{Entity}ToAggregate() {
        // Given
        {Aggregate} aggregate = {Aggregate}.create{Aggregate}("Test", ...);
        {Entity} entity = new {Entity}(...);

        // When
        {Aggregate} updated = aggregate.add{Entity}(entity);

        // Then
        assertNotSame(aggregate, updated);
        assertTrue(updated.get{Entities}().contains(entity));
        assertFalse(aggregate.get{Entities}().contains(entity));  // Original unchanged
    }
}
```

---

## Special Considerations

### Immutability Rules
1. **All fields must be `final`**
2. **Private constructors only**
3. **Public factory methods for creation** (e.g., `create{Aggregate}()`)
4. **Update methods return new instances**
5. **Collections must be copied** (use `new HashSet<>(collection)`)
6. **Return unmodifiable collections** in getters

### Domain Events
1. **Place events in aggregate methods**, not in use cases
2. **Always include aggregate ID** in event
3. **Use past tense naming** (Created, Updated, Deleted)
4. **Events are immutable** - all fields final

### Validation
1. **Constructor validates all invariants**
2. **Throw `ValidationException`** for business rule violations
3. **Validate in factory methods** before construction
4. **Update methods validate** before creating new instance

### Entities Within Aggregates
- If aggregate contains other entities, they should:
  - Have IDs (value objects)
  - Be immutable
  - Be accessed only through aggregate root
  - Not be referenced by other aggregates directly

---

## Example Request

```
Aggregate: Quotation
Module: quotation
Description: Price estimate for transportation services

Aggregate Root ID: QuotationId

Value Objects:
- Origin: Address (pick-up location)
- Destination: Address (delivery location)
- Volume: Physical characteristics (weight, dimensions)
- Price: Money amount with currency

Business Operations:
- Create quotation with route and volume
- Approve quotation
- Reject quotation
- Expire quotation (time-based)

Domain Events:
- QuotationCreated
- QuotationApproved
- QuotationRejected
- QuotationExpired

Invariants:
- Quotation must have valid origin and destination
- Volume must be positive
- Price must be positive
- Approved quotation cannot be modified
```

---

## Validation Checklist

Before submitting, verify:

- [ ] Aggregate extends `AbstractAggregateRoot`
- [ ] All fields are `final`
- [ ] Constructor is private
- [ ] Factory method exists (e.g., `create{Aggregate}()`)
- [ ] Update methods return new instances
- [ ] Domain events placed in aggregate methods
- [ ] Getters only, no setters
- [ ] Collections returned as unmodifiable
- [ ] Invariants validated in constructor
- [ ] Domain tests cover creation, updates, and validation
- [ ] ID value object created
- [ ] Repository interface defined

---

## References

- **Pattern Example:** `/doc/ai/examples/complete-aggregate.md`
- **Existing Aggregates:** `src/main/java/br/com/logistics/tms/{module}/domain/`
- **Architecture Guide:** `/doc/ai/ARCHITECTURE.md`
- **DDD Concepts:** `/doc/GLOSSARY.md#aggregate`
