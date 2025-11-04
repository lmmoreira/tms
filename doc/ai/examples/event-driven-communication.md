# Event-Driven Module Communication

This document demonstrates how modules communicate through domain events in TMS.

---

## Overview

Modules in TMS are decoupled and communicate **exclusively through domain events** using RabbitMQ. This ensures:
- ✅ No direct dependencies between modules
- ✅ Eventual consistency
- ✅ Resilient architecture
- ✅ Easier testing and scaling

---

## Complete Flow Example: Company → ShipmentOrder

### Scenario
When a `ShipmentOrder` is created, the `Company` module needs to increment its order counter.

---

## Step 1: Domain Event Definition

**Location:** `shipmentorder/domain/events/ShipmentOrderCreated.java`

```java
package br.com.logistics.tms.shipmentorder.domain.events;

import br.com.logistics.tms.commons.domain.AbstractDomainEvent;
import br.com.logistics.tms.commons.domain.Id;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event emitted when a shipment order is created.
 * Past tense naming convention.
 */
public class ShipmentOrderCreated extends AbstractDomainEvent {

    private final UUID shipmentOrderId;
    private final UUID companyId;
    private final String orderNumber;

    public ShipmentOrderCreated(UUID shipmentOrderId, UUID companyId, String orderNumber) {
        super(Id.unique(), shipmentOrderId, Instant.now());
        this.shipmentOrderId = shipmentOrderId;
        this.companyId = companyId;
        this.orderNumber = orderNumber;
    }

    public UUID getShipmentOrderId() {
        return shipmentOrderId;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    @Override
    public String toString() {
        return "ShipmentOrderCreated{" +
                "shipmentOrderId=" + shipmentOrderId +
                ", companyId=" + companyId +
                ", orderNumber='" + orderNumber + '\'' +
                '}';
    }
}
```

---

## Step 2: Event Placement in Aggregate

**Location:** `shipmentorder/domain/ShipmentOrder.java`

```java
package br.com.logistics.tms.shipmentorder.domain;

import br.com.logistics.tms.commons.domain.AbstractAggregateRoot;
import br.com.logistics.tms.commons.domain.AbstractDomainEvent;
import br.com.logistics.tms.shipmentorder.domain.events.ShipmentOrderCreated;

import java.util.*;

public class ShipmentOrder extends AbstractAggregateRoot {

    private final ShipmentOrderId shipmentOrderId;
    private final CompanyId companyId;
    private final String orderNumber;
    // ... other fields

    private ShipmentOrder(ShipmentOrderId shipmentOrderId,
                         CompanyId companyId,
                         String orderNumber,
                         Set<AbstractDomainEvent> domainEvents,
                         Map<String, Object> persistentMetadata) {
        super(new HashSet<>(domainEvents), new HashMap<>(persistentMetadata));
        this.shipmentOrderId = shipmentOrderId;
        this.companyId = companyId;
        this.orderNumber = orderNumber;
    }

    /**
     * Factory method: Creates new shipment order and places event.
     */
    public static ShipmentOrder createOrder(CompanyId companyId, String orderNumber) {
        ShipmentOrder order = new ShipmentOrder(
            ShipmentOrderId.unique(),
            companyId,
            orderNumber,
            new HashSet<>(),
            new HashMap<>()
        );

        // Event is placed HERE in the aggregate
        order.placeDomainEvent(new ShipmentOrderCreated(
            order.getShipmentOrderId().value(),
            order.getCompanyId().value(),
            order.getOrderNumber()
        ));

        return order;
    }

    // Getters...
    public ShipmentOrderId getShipmentOrderId() { return shipmentOrderId; }
    public CompanyId getCompanyId() { return companyId; }
    public String getOrderNumber() { return orderNumber; }
}
```

---

## Step 3: Use Case Creates Aggregate

**Location:** `shipmentorder/application/usecases/CreateShipmentOrderUseCase.java`

```java
package br.com.logistics.tms.shipmentorder.application.usecases;

import br.com.logistics.tms.commons.application.UseCase;
import br.com.logistics.tms.commons.application.annotations.Cqrs;
import br.com.logistics.tms.commons.application.annotations.DatabaseRole;
import br.com.logistics.tms.commons.application.annotations.DomainService;
import br.com.logistics.tms.shipmentorder.application.repositories.ShipmentOrderRepository;
import br.com.logistics.tms.shipmentorder.domain.CompanyId;
import br.com.logistics.tms.shipmentorder.domain.ShipmentOrder;

import java.util.UUID;

@DomainService
@Cqrs(DatabaseRole.WRITE)
public class CreateShipmentOrderUseCase 
    implements UseCase<CreateShipmentOrderUseCase.Input, CreateShipmentOrderUseCase.Output> {

    private final ShipmentOrderRepository shipmentOrderRepository;

    public CreateShipmentOrderUseCase(ShipmentOrderRepository shipmentOrderRepository) {
        this.shipmentOrderRepository = shipmentOrderRepository;
    }

    @Override
    public Output execute(Input input) {
        // 1. Create aggregate (event is placed inside)
        ShipmentOrder order = ShipmentOrder.createOrder(
            new CompanyId(input.companyId()),
            input.orderNumber()
        );

        // 2. Repository persists both entity AND events to outbox
        order = shipmentOrderRepository.create(order);

        // 3. Return output
        return new Output(order.getShipmentOrderId().value());
    }

    public record Input(UUID companyId, String orderNumber) {}
    public record Output(UUID shipmentOrderId) {}
}
```

---

## Step 4: Repository Saves Events to Outbox

**Location:** `shipmentorder/infrastructure/repositories/ShipmentOrderRepositoryImpl.java`

```java
package br.com.logistics.tms.shipmentorder.infrastructure.repositories;

import br.com.logistics.tms.commons.infrastructure.outbox.OutboxService;
import br.com.logistics.tms.shipmentorder.application.repositories.ShipmentOrderRepository;
import br.com.logistics.tms.shipmentorder.domain.ShipmentOrder;
import br.com.logistics.tms.shipmentorder.infrastructure.jpa.ShipmentOrderJpaEntity;
import br.com.logistics.tms.shipmentorder.infrastructure.jpa.ShipmentOrderJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ShipmentOrderRepositoryImpl implements ShipmentOrderRepository {

    private final ShipmentOrderJpaRepository jpaRepository;
    private final OutboxService outboxService;

    public ShipmentOrderRepositoryImpl(ShipmentOrderJpaRepository jpaRepository,
                                      OutboxService outboxService) {
        this.jpaRepository = jpaRepository;
        this.outboxService = outboxService;
    }

    @Override
    @Transactional
    public ShipmentOrder create(ShipmentOrder order) {
        // 1. Save entity
        ShipmentOrderJpaEntity entity = ShipmentOrderJpaEntity.from(order);
        entity = jpaRepository.save(entity);

        // 2. Save events to outbox (same transaction)
        outboxService.saveEvents(order.getDomainEvents());

        // 3. Return domain object
        return entity.toDomain();
    }
}
```

---

## Step 5: Outbox Publishes to RabbitMQ

The `OutboxPublisher` (background job) reads events from the outbox table and publishes them to RabbitMQ:

```java
// This is automatic, handled by OutboxPublisher
// Events are published to: integration.shipmentorder.created
```

---

## Step 6: Event Listener in Company Module

**Location:** `company/infrastructure/listener/IncrementShipmentOrderListener.java`

```java
package br.com.logistics.tms.company.infrastructure.listener;

import br.com.logistics.tms.commons.application.VoidUseCaseExecutor;
import br.com.logistics.tms.commons.application.annotations.Cqrs;
import br.com.logistics.tms.commons.application.annotations.DatabaseRole;
import br.com.logistics.tms.company.application.usecases.IncrementShipmentOrderUseCase;
import br.com.logistics.tms.company.infrastructure.dto.ShipmentOrderCreatedDTO;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Listens to ShipmentOrderCreated events from shipmentorder module.
 * Updates company's order counter.
 */
@Component
@Cqrs(DatabaseRole.WRITE)
@Lazy(false) // Ensure listener is registered at startup
public class IncrementShipmentOrderListener {

    private final VoidUseCaseExecutor voidUseCaseExecutor;
    private final IncrementShipmentOrderUseCase incrementShipmentOrderUseCase;

    public IncrementShipmentOrderListener(VoidUseCaseExecutor voidUseCaseExecutor,
                                         IncrementShipmentOrderUseCase incrementShipmentOrderUseCase) {
        this.voidUseCaseExecutor = voidUseCaseExecutor;
        this.incrementShipmentOrderUseCase = incrementShipmentOrderUseCase;
    }

    /**
     * Handles ShipmentOrderCreated events.
     * Queue: integration.company.shipmentorder.created
     */
    @RabbitListener(queues = "integration.company.shipmentorder.created")
    public void handle(ShipmentOrderCreatedDTO dto,
                      Message message,
                      Channel channel,
                      @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            // Execute use case to increment counter
            voidUseCaseExecutor
                .from(incrementShipmentOrderUseCase)
                .withInput(new IncrementShipmentOrderUseCase.Input(dto.companyId()))
                .execute();

            // Acknowledge message
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            // Handle error (could reject and requeue)
            try {
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ioException) {
                // Log error
            }
        }
    }
}
```

### Event DTO

**Location:** `company/infrastructure/dto/ShipmentOrderCreatedDTO.java`

```java
package br.com.logistics.tms.company.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * DTO for receiving ShipmentOrderCreated events.
 */
public record ShipmentOrderCreatedDTO(
    @JsonProperty("shipmentOrderId") UUID shipmentOrderId,
    @JsonProperty("companyId") UUID companyId,
    @JsonProperty("orderNumber") String orderNumber
) {}
```

---

## Step 7: Use Case Handles Event

**Location:** `company/application/usecases/IncrementShipmentOrderUseCase.java`

```java
package br.com.logistics.tms.company.application.usecases;

import br.com.logistics.tms.commons.application.VoidUseCase;
import br.com.logistics.tms.commons.application.annotations.Cqrs;
import br.com.logistics.tms.commons.application.annotations.DatabaseRole;
import br.com.logistics.tms.commons.application.annotations.DomainService;
import br.com.logistics.tms.commons.exception.NotFoundException;
import br.com.logistics.tms.company.application.repositories.CompanyRepository;
import br.com.logistics.tms.company.domain.Company;
import br.com.logistics.tms.company.domain.CompanyId;

import java.util.UUID;

/**
 * Increments company's shipment order counter.
 * Triggered by ShipmentOrderCreated event.
 */
@DomainService
@Cqrs(DatabaseRole.WRITE)
public class IncrementShipmentOrderUseCase implements VoidUseCase<IncrementShipmentOrderUseCase.Input> {

    private final CompanyRepository companyRepository;

    public IncrementShipmentOrderUseCase(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public void execute(Input input) {
        // 1. Retrieve company
        Company company = companyRepository.getCompanyById(new CompanyId(input.companyId()))
            .orElseThrow(() -> new NotFoundException("Company not found"));

        // 2. Update counter (immutable)
        Company updatedCompany = company.incrementOrderCounter();

        // 3. Persist
        companyRepository.update(updatedCompany);
    }

    public record Input(UUID companyId) {}
}
```

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    ShipmentOrder Module                      │
│                                                              │
│  1. CreateShipmentOrderUseCase                              │
│     └─> ShipmentOrder.createOrder()                         │
│         └─> placeDomainEvent(ShipmentOrderCreated)          │
│                                                              │
│  2. Repository.create()                                      │
│     ├─> Save ShipmentOrderJpaEntity                         │
│     └─> OutboxService.saveEvents()                          │
│         └─> INSERT INTO outbox (event_type, payload)        │
└─────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                      Outbox Publisher                        │
│  (Background Job - every 5 seconds)                         │
│                                                              │
│  3. SELECT * FROM outbox WHERE published = false            │
│  4. Publish to RabbitMQ                                     │
│     └─> Topic: integration.shipmentorder.created            │
│  5. UPDATE outbox SET published = true                      │
└─────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                        RabbitMQ                              │
│                                                              │
│  Exchange: integration.shipmentorder.events                 │
│  Routing: integration.shipmentorder.created                 │
│     └─> Queue: integration.company.shipmentorder.created    │
└─────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                      Company Module                          │
│                                                              │
│  6. IncrementShipmentOrderListener                          │
│     @RabbitListener(queue = "integration.company...")       │
│     └─> VoidUseCaseExecutor                                 │
│         └─> IncrementShipmentOrderUseCase                   │
│             └─> company.incrementOrderCounter()             │
│                 └─> companyRepository.update()              │
└─────────────────────────────────────────────────────────────┘
```

---

## Key Principles

✅ **Events in Aggregates:** Domain events are placed in aggregate methods
✅ **Outbox Pattern:** Events saved transactionally with entity
✅ **Eventually Consistent:** Listeners process events asynchronously
✅ **Decoupled Modules:** No direct dependencies between modules
✅ **Queue Naming:** `integration.{target-module}.{event-source}`
✅ **Error Handling:** Use `basicNack` to requeue failed messages
✅ **Idempotency:** Ensure use cases can handle duplicate events

---

## Testing Event Flow

```java
@SpringBootTest
@Testcontainers
class ShipmentOrderEventIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");
    
    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3-management");

    @Test
    void shouldIncrementCompanyCounterWhenOrderCreated() {
        // 1. Create company
        // 2. Create shipment order
        // 3. Wait for event processing
        // 4. Assert company counter incremented
    }
}
```

---

## Common Patterns

### Pattern 1: One-Way Notification
Module A notifies Module B of an event (no response needed).

### Pattern 2: Data Replication
Module A publishes full entity data; Module B maintains read model.

### Pattern 3: Saga
Multiple modules coordinate via events (e.g., order fulfillment workflow).

---

## Anti-Patterns ❌

❌ **Direct Repository Calls:** Never call another module's repository
❌ **Shared Database Tables:** Each module owns its tables
❌ **Synchronous Calls:** Don't use REST between modules
❌ **Events in Use Cases:** Place events in aggregates only
