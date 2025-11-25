# Integration Patterns in TMS

This document details the patterns used to integrate the Company and Shipment Order bounded contexts. These patterns ensure loose coupling, eventual consistency, and reliable inter-context communication.

---

## Pattern 1: Event-Driven Integration

### Overview

Contexts communicate through published events on a shared event bus (RabbitMQ). This pattern enables:
- **Loose Coupling**: Contexts don't need to know about each other's implementation
- **Scalability**: Each context can scale independently
- **Resilience**: Failure in one context doesn't block others
- **Flexibility**: Easy to add new listeners or consumers

### Implementation in TMS

```
Publishing Context           Event Bus          Consuming Context
─────────────────────────────────────────────────────────────────

Company Aggregate            RabbitMQ            ShipmentOrder Context
  │                            │
  ├─ Place event             ┌─► Exchange
  │  (in-memory)             │   (integration.company)
  │                          │
  ├─ Persist event           ├─► Queue
  │  to outbox               │   (integration.shipmentorder.company.created)
  │                          │
  └─ Publish event ──────────┴─► Listener
                                  (CompanyCreatedListener)
                                    │
                                    └─► UseCase
                                        (SynchronizeCompanyUseCase)
                                          │
                                          └─► Database
                                              (shipmentorder.companies)
```

### Event Lifecycle

```
1. Command Execution
   ├─ Command arrives: CreateCompanyRequest
   ├─ UseCase: CreateCompanyUseCase.execute()
   └─ Result: Company aggregate created

2. Event Emission
   ├─ Location: Company.createCompany() factory method
   ├─ Method: company.placeDomainEvent(new CompanyCreated(...))
   ├─ Storage: Event held in-memory in aggregate
   └─ Visibility: Event not yet published

3. Event Persistence
   ├─ Repository: CompanyRepository.create(company)
   ├─ Action: Save aggregate to company.companies table
   ├─ Also: Save event to company.outbox table
   ├─ Transaction: Both succeed or both fail (strong consistency)
   └─ Guarantee: At-least-once publication

4. Event Publishing
   ├─ Process: Event Outbox Processor (background job)
   ├─ Frequency: Every 1-5 seconds
   ├─ Action: Read unpublished events from outbox
   ├─ Target: Publish to RabbitMQ
   ├─ Mark: Mark as published in outbox
   └─ Guarantee: No message loss (outbox pattern)

5. Event Delivery
   ├─ Transport: RabbitMQ message queue
   ├─ Routing: Message to appropriate queue
   ├─ Delivery: At-least-once guarantee
   ├─ Ordering: Per-queue ordering maintained
   └─ Durability: Persisted on disk

6. Event Consumption
   ├─ Listener: Activated when message arrives
   ├─ Deserialization: Message converted to DTO
   ├─ Processing: UseCase executes with event data
   ├─ Transaction: Use case in separate transaction
   └─ Idempotency: Safe to process duplicate messages

7. Event Acknowledgment
   ├─ Success: Message acknowledged to RabbitMQ
   ├─ Failure: Message requeued for retry
   ├─ Retries: Up to 3-5 attempts
   ├─ Max: Failed messages → Dead Letter Queue
   └─ Result: Event fully processed
```

### Example: CompanyCreated Event

```java
// 1. Event defined in Company context
public class CompanyCreated extends AbstractDomainEvent {
    private final UUID companyId;
    private final String company;
    private final Set<String> types;
}

// 2. Event placed in aggregate
public class Company {
    public static Company createCompany(String name, ...) {
        Company company = new Company(...);
        company.placeDomainEvent(
            new CompanyCreated(
                company.getCompanyId().value(),
                company.getName(),
                company.getTypes()
            )
        );
        return company;
    }
}

// 3. Event persisted with aggregate
CompanyRepository.create(company)
  ├─ Save Company (to company.companies)
  └─ Save Events (to company.outbox)

// 4. Event published by background processor
company.outbox:
  ├─ status: 'PENDING'
  ├─ event: CompanyCreated
  └─ published_at: null
      │
      ├─ Background processor picks up
      ├─ Publishes to RabbitMQ
      │
      └─ status: 'PUBLISHED'
         published_at: 2024-11-25T14:30:00Z

// 5. Event received by listener
@RabbitListener(queues = "integration.shipmentorder.company.created")
public void handle(CompanyCreatedDTO event) {
    // Listener receives deserialized event
}

// 6. UseCase processes event
SynchronizeCompanyUseCase.execute(
    new Input(event.companyId(), mapper.map(event, Map.class))
)
  ├─ Create CompanyData value object
  ├─ Store in shipmentorder.companies
  └─ Complete successfully
```

---

## Pattern 2: Eventual Consistency

### Definition

Eventual consistency is a consistency model where:
- **Immediate**: Changes within a single context are immediately consistent
- **Eventually**: Changes between contexts become consistent within a time window
- **Guaranteed**: All updates will eventually propagate (assuming no permanent failures)

### In TMS

```
T=0s    Company Context                    ShipmentOrder Context
        ────────────────                   ─────────────────────
        Company Created ✓                  (Unknown)
        │
        ├─ Outbox: PENDING
        │
T=0.5s  └─► Published to RabbitMQ
              │
T=1s          ├─► In Queue
              │
T=1.5s        ├─► Listener triggered
              │
T=2s          └─► SynchronizeCompanyUseCase
                  │
T=2.5s            └─► INSERT shipmentorder.companies
                       Company Data ✓

RESULT:
  Time 0-2.5s: Inconsistent (Company in one context, not the other)
  Time 2.5s+: Consistent (Company in both contexts)
  Window: ~2-3 seconds (typical)
```

### Guarantees

1. **Consistency Eventually Achieved**
   - Even if delayed, all updates propagate
   - No state is permanently inconsistent
   - Ordering of updates preserved per aggregate

2. **Strong Consistency Within Context**
   - Changes to Company are immediately visible
   - No stale reads within same context
   - Transaction boundaries respected

3. **Idempotent Consumers**
   - Same event can be processed multiple times
   - Result is always the same
   - No duplicated or missing data

### Handling Inconsistency

**Problem**: Order creation fails because company sync hasn't completed yet

**Solution 1: Polling**
```java
// Wait for company data to appear
int maxRetries = 10;
for (int i = 0; i < maxRetries; i++) {
    Optional<Company> company = 
        shipmentOrderRepository.getCompanyData(companyId);
    
    if (company.isPresent()) {
        return createOrder(company);
    }
    
    Thread.sleep(500); // Wait and retry
}

throw new CompanyNotYetSynchronizedException(companyId);
```

**Solution 2: Validation with Messaging**
```java
// Include company data in order creation request
// Client waits until company appears before attempting
public ShipmentOrder createOrder(CreateOrderRequest request) {
    // Company must already be synchronized
    CompanyData company = shipmentOrderRepository
        .getCompanyData(request.companyId())
        .orElseThrow(() -> new NotFoundException(
            "Company not yet synchronized. Please retry."
        ));
    
    return ShipmentOrder.create(company, ...);
}
```

**Solution 3: Asynchronous Processing**
```java
// Accept order, process async when company available
public void acceptOrder(CreateOrderRequest request) {
    // Enqueue for processing
    orderQueue.enqueue(request);
    return accepted();
}

// Process when company is available
@Scheduled(fixedRate = 1000)
public void processQueuedOrders() {
    for (CreateOrderRequest order : orderQueue) {
        try {
            createOrder(order);
            orderQueue.remove(order);
        } catch (CompanyNotFoundException e) {
            // Retry next iteration
        }
    }
}
```

---

## Pattern 3: Outbox Pattern

### Problem It Solves

**Without Outbox**:
```
Issue: Dual Write Problem

Save to DB             Publish Event
      │                      │
      └─── Transaction ──────┘
           May fail!
           
Scenarios:
1. Saved OK, publish fails → Data in DB but event not sent
2. Publish OK, save fails → Event sent but no data
3. Both fail → Inconsistent state
```

**With Outbox**:
```
Consistent Dual Write

Save to DB ─┬─ Company table
            │
            ├─ Event table (Outbox)
            │
            └─ Single transaction
                (both succeed or both fail)
                      │
                      ▼
              Background job
                      │
              Publishes event
              when confirmed
                      │
                      ▼
              Updates outbox status
```

### Implementation in TMS

#### Step 1: Define Outbox Table

```sql
-- company.outbox
CREATE TABLE company.outbox (
    id UUID PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload JSONB NOT NULL,
    published BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP NULL
);

-- shipmentorder.outbox
CREATE TABLE shipmentorder.outbox (
    id UUID PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload JSONB NOT NULL,
    published BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP NULL
);
```

#### Step 2: Save Event & Aggregate Together

```java
@Transactional  // Single transaction
public Company create(Company company) {
    // Save aggregate
    companyJpaRepository.save(company);
    
    // Save events in same transaction
    company.getDomainEvents().forEach(event -> {
        CompanyOutboxEntity outboxEntry = new CompanyOutboxEntity(
            event.getEventId(),
            event.getAggregateId(),
            event.getClass().getSimpleName(),
            jsonSingleton.toJson(event),
            false,  // published = false
            Instant.now(),
            null    // published_at = null
        );
        outboxJpaRepository.save(outboxEntry);
    });
    
    return company;
}
// Both Company and Outbox saved, or both rolled back
```

#### Step 3: Background Processor Publishes

```java
@Component
public class EventOutboxProcessor {
    
    @Scheduled(fixedRate = 5000)  // Every 5 seconds
    public void publishPendingEvents() {
        // Get all unpublished events
        List<CompanyOutboxEntity> pending = 
            outboxJpaRepository.findByPublishedFalse();
        
        for (CompanyOutboxEntity entry : pending) {
            try {
                // Deserialize event
                AbstractDomainEvent event = 
                    jsonSingleton.fromJson(entry.payload, 
                        Class.forName(entry.eventType));
                
                // Publish to RabbitMQ
                rabbitTemplate.convertAndSend(
                    "integration.company",
                    "company." + entry.eventType.toLowerCase(),
                    event
                );
                
                // Mark as published
                entry.setPublished(true);
                entry.setPublishedAt(Instant.now());
                outboxJpaRepository.save(entry);
                
            } catch (Exception e) {
                // Log error, retry next iteration
                logger.error("Failed to publish event: {}", 
                    entry.id, e);
            }
        }
    }
}
```

#### Step 4: Verify Delivery & Cleanup

```java
@Scheduled(fixedRate = 3600000)  // Every hour
public void cleanupPublishedEvents() {
    // Remove events older than 7 days that were published
    LocalDateTime sevenDaysAgo = 
        LocalDateTime.now().minusDays(7);
    
    outboxJpaRepository.deleteByPublishedTrueAndCreatedAtBefore(
        sevenDaysAgo
    );
}
```

### Guarantees Provided

1. **No Message Loss**
   - Events persisted before publishing
   - Safe to retry publishing

2. **No Duplicate Saves**
   - Single atomic transaction
   - Company + Events both save or both fail

3. **Ordered Delivery**
   - Events published in order they were created
   - Within single aggregate, ordering guaranteed

4. **Recoverable**
   - Unpublished events persist until successfully published
   - Can replay if RabbitMQ goes down

---

## Pattern 4: Anti-Corruption Layer

### Problem It Solves

Without anti-corruption layer, consuming context becomes tightly coupled to producer's schema:

```
Company Context                ShipmentOrder Context
─────────────────              ─────────────────────

class Company {                class ShipmentOrder {
    UUID id;                       CompanyId companyId;
    String name;                   Company company;  ❌ Direct dep
    Cnpj cnpj;                     // Problem:
    Set<CompanyType> types;        // - Breaks on company schema change
}                                  // - Circular dependency
                                   // - Can't deploy independently
```

### Solution: Anti-Corruption Layer (CompanyData)

```
Company Context                Event              ShipmentOrder Context
─────────────────              ─────              ─────────────────────

class Company {                CompanyCreated      class CompanyData {
    UUID id;                   {                       UUID companyId;
    String name;                 companyId,            Map<String, Object>
    Cnpj cnpj;                   company,              data;
    Set<CompanyType>             types             }
    types;                     }
}                                                  class ShipmentOrder {
                                  │                    CompanyId companyId;
                                  │                    CompanyData company;
                                  │                    // Decoupled! ✓
                                  │                }
                                  │
                                  └─── Listener ─┐
                                       Deserialize│
                                       Extract    │
                                       Transform  │
                                                  ▼
                                              CompanyData VO
                                              (Isolated)
```

### Implementation in TMS

#### CompanyData Value Object (Anti-Corruption Layer)

```java
// Location: shipmentorder/domain/CompanyData.java
public record CompanyData(
    UUID companyId,
    Map<String, Object> data
) {
    public CompanyData {
        if (companyId == null) 
            throw new ValidationException("Invalid companyId");
        if (data == null) 
            data = new HashMap<>();
    }
    
    // Methods to access synchronized data
    public String getName() {
        return (String) data.get("name");
    }
    
    public Set<String> getTypes() {
        return (Set<String>) data.get("types");
    }
    
    // Isolated from Company context changes
}
```

#### Transformation in Listener

```java
@Component
public class CompanyCreatedListener {
    
    @RabbitListener(queues = "...")
    public void handle(CompanyCreatedDTO event, ...) {
        voidUseCaseExecutor
            .from(synchronizeCompanyUseCase)
            .withInput(
                new SynchronizeCompanyUseCase.Input(
                    event.companyId(),
                    // Transform Company DTO → Map
                    mapper.map(event, Map.class)
                )
            )
            .execute();
    }
}
```

#### Storage in Local Schema

```java
// Location: shipmentorder/infrastructure/jpa/CompanyDataJpaEntity.java
@Entity
@Table(name = "companies", schema = "shipmentorder")
public class CompanyDataJpaEntity {
    
    @Id
    private UUID id;
    
    // Store as JSON, not as foreign key
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data")
    private Map<String, Object> data;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### Benefits

1. **Loose Coupling**
   - ShipmentOrder doesn't depend on Company's class structure
   - Can change Company schema without breaking ShipmentOrder

2. **Independent Deployment**
   - Contexts can be deployed in any order
   - No shared type dependencies

3. **Clear Boundaries**
   - Data explicitly transformed at boundary
   - Easy to see what's shared

4. **Testability**
   - Can test ShipmentOrder without Company context
   - Mock data easily

---

## Pattern 5: Saga (Distributed Transaction)

### When to Use

Use saga pattern when you need to:
- Coordinate actions across multiple contexts
- Ensure atomicity of distributed operations
- Compensate for failures

### In TMS (Current State)

Current system doesn't use explicit saga pattern, but follows eventual consistency:

```
Create Order → Publish Event → Increment Counter (Async)
              (Decoupled)
```

**Characteristics**:
- No explicit saga orchestration
- Events trigger side effects
- No global transaction
- Eventual consistency acceptable

### If We Needed Saga Pattern (Future Enhancement)

**Example**: "Reserve Capacity" saga

```
Request: ReserveCapacity for 100 units

Step 1: Reserve Capacity (Company Context)
  UseCase: ReserveCapacityUseCase
  Event: CapacityReserved
    │
    ├─ SUCCESS
    │
    └─ FAILURE
       │
       └─ Publish: ReservationFailed (Compensating)

Step 2: Create ShipmentOrder (ShipmentOrder Context)
  Listener: CapacityReservedListener
  UseCase: CreateShipmentOrderUseCase
  Event: ShipmentOrderCreated
    │
    ├─ SUCCESS
    │
    └─ FAILURE
       │
       └─ Publish: OrderCreationFailed
          (Triggers Step 1 Compensation)

Compensation: ReleaseCapacity
  Listener: ReservationFailedListener
  UseCase: ReleaseCapacityUseCase
  Action: Undo Step 1
```

---

## Summary of Integration Patterns

| Pattern | Use Case | Coupling | Consistency | TMS Usage |
|---------|----------|----------|-------------|-----------|
| **Event-Driven** | Async notifications | Loose | Eventual | ✓ Primary |
| **Eventual Consistency** | Cross-context changes | Loose | Eventual | ✓ Active |
| **Outbox** | Reliable publishing | N/A | Strong (atomic) | ✓ Implemented |
| **Anti-Corruption** | Boundary protection | Loose | N/A | ✓ Used |
| **Saga** | Distributed transactions | Loose | Eventually atomic | (Future) |

---

## Best Practices

1. ✅ **Always use Outbox Pattern**
   - Prevents message loss
   - Ensures atomic saves

2. ✅ **Implement Idempotent Listeners**
   - Handle duplicate event processing
   - Same event processed multiple times = same result

3. ✅ **Use Value Objects as Anti-Corruption Layer**
   - Don't import consuming context types
   - Transform at boundaries

4. ✅ **Design for Eventual Consistency**
   - Don't expect immediate cross-context updates
   - Build retry logic into consumers

5. ✅ **Monitor Event Flow**
   - Track latency end-to-end
   - Alert on queue depth
   - Monitor dead letter queues

6. ❌ **Avoid**
   - ❌ Direct database calls between contexts
   - ❌ Synchronous HTTP calls for integration
   - ❌ Shared entities between contexts
   - ❌ Global transactions
