# Event Storming & Event Catalog

This document presents the results of event storming for the TMS system, cataloging all domain events, their sources, consumers, and their roles in business processes.

## Event Storming Overview

Event storming is a collaborative workshop technique for discovering events and processes in a complex system. This document captures the events discovered in TMS.

### Event Storming Timeline

```
TIME ─────────────────────────────────────────────────────────────────►

┌─────────────────────────────────────────────────────────────────────┐
│                      COMPANY CONTEXT TIMELINE                       │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  [User]                                                             │
│    │                                                                │
│    ▼ Create                                                         │
│  [CreateCompanyUseCase]                                             │
│    │                                                                │
│    ├──► Validate CNPJ uniqueness                                    │
│    │    (CompanyRepository.getCompanyByCnpj)                       │
│    │                                                                │
│    ├──► Create aggregate                                            │
│    │    (Company.createCompany factory)                            │
│    │                                                                │
│    ▼                                                                │
│  ⭐ [CompanyCreated]  ◄─ Domain Event Emitted                      │
│    │                                                                │
│    ├──► Publish to RabbitMQ                                         │
│    │    (integration.company.shipmentorder.created)               │
│    │                                                                │
│    └──► Return to caller                                           │
│                                                                     │
│  (Time passes... eventual consistency window)                       │
│                                                                     │
│  [User]                                                             │
│    │                                                                │
│    ▼ Update                                                         │
│  [UpdateCompanyUseCase]                                             │
│    │                                                                │
│    ├──► Load Company aggregate                                      │
│    │                                                                │
│    ├──► Call updateName()                                          │
│    │    (returns new instance with change)                         │
│    │                                                                │
│    ▼                                                                │
│  ⭐ [CompanyUpdated]  ◄─ Domain Event Emitted                      │
│    │                                                                │
│    ├──► Publish to RabbitMQ                                         │
│    │    (integration.company.shipmentorder.updated)               │
│    │                                                                │
│    └──► Return to caller                                           │
│                                                                     │
│  [User]                                                             │
│    │                                                                │
│    ▼ Delete                                                         │
│  [DeleteCompanyByIdUseCase]                                         │
│    │                                                                │
│    ├──► Load Company aggregate                                      │
│    │                                                                │
│    ├──► Validate deletion allowed                                   │
│    │                                                                │
│    ▼                                                                │
│  ⭐ [CompanyDeleted]   ◄─ Domain Event Emitted                      │
│    │                                                                │
│    ├──► Publish to RabbitMQ                                         │
│    │    (integration.company.shipmentorder.deleted)               │
│    │                                                                │
│    └──► Return to caller                                           │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                  SHIPMENT ORDER CONTEXT TIMELINE                    │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  (Passive: Awaiting external events)                               │
│                                                                     │
│  ◄─ [CompanyCreated] ─────────────────────────────────────────     │
│     (from Company context via RabbitMQ)                            │
│                                                                     │
│     Queue: integration.shipmentorder.company.created               │
│     │                                                               │
│     ▼                                                               │
│  [CompanyCreatedListener]                                           │
│     │                                                               │
│     ▼                                                               │
│  [SynchronizeCompanyUseCase]                                        │
│     │                                                               │
│     ├──► Extract company data                                       │
│     │                                                               │
│     ├──► Store/Update in local schema                               │
│     │    (shipmentorder.companies table)                           │
│     │                                                               │
│     └──► Return (void)                                              │
│                                                                     │
│  ◄─ [CompanyUpdated] ──────────────────────────────────────────   │
│     (from Company context via RabbitMQ)                            │
│                                                                     │
│     Queue: integration.shipmentorder.company.updated               │
│     │                                                               │
│     ▼                                                               │
│  [CompanyUpdatedListener]                                           │
│     │                                                               │
│     ▼                                                               │
│  [SynchronizeCompanyUseCase]                                        │
│     │                                                               │
│     ├──► Update specific property                                   │
│     │                                                               │
│     ├──► Update in local schema                                     │
│     │                                                               │
│     └──► Return (void)                                              │
│                                                                     │
│  [User]                                                             │
│    │                                                                │
│    ▼ Create Order                                                   │
│  [CreateShipmentOrderUseCase]                                       │
│    │                                                                │
│    ├──► Validate company exists                                     │
│    │    (check shipmentorder.companies)                            │
│    │                                                                │
│    ├──► Create aggregate                                            │
│    │    (ShipmentOrder.createOrder factory)                        │
│    │                                                                │
│    ▼                                                                │
│  ⭐ [ShipmentOrderCreated]  ◄─ Domain Event Emitted                │
│    │                                                                │
│    ├──► Publish to RabbitMQ                                         │
│    │    (integration.company.shipmentorder.created)               │
│    │                                                                │
│    └──► Return to caller                                           │
│                                                                     │
│  ◄─ [ShipmentOrderCreated] ────────────────────────────────────   │
│     (own event consumed by Company context)                        │
│                                                                     │
│     Queue: integration.company.shipmentorder.created               │
│     │                                                               │
│     ▼                                                               │
│  [Company] IncrementShipmentOrderListener                           │
│     │                                                               │
│     ▼                                                               │
│  [IncrementShipmentOrderUseCase]                                    │
│     │                                                               │
│     └──► Increment order counter for company                        │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Events Catalog

### 1. CompanyCreated

**Classification**: Domain Event  
**Context**: Company (Publisher)  
**Aggregate**: Company  
**Time-based**: Yes (Instant.now())

#### Purpose
Signals that a new company has been registered in the system. This event triggers synchronization in the Shipment Order context.

#### Event Structure
```java
public class CompanyCreated extends AbstractDomainEvent {
    private final UUID companyId;           // Root aggregate ID
    private final String company;           // Company name
    private final Set<String> types;        // Company types (SHIPPER, CARRIER, etc.)
}
```

#### Attributes
| Field | Type | Example | Purpose |
|-------|------|---------|---------|
| domainEventId | UUID | 550e8400-e29b-41d4-a716-446655440000 | Event identifier |
| companyId | UUID | 550e8400-e29b-41d4-a716-446655440001 | Company aggregate ID |
| company | String | "Amazon Brasil" | Company name |
| types | Set<String> | ["SHIPPER", "CARRIER"] | Company classifications |
| occurredOn | Instant | 2024-11-25T10:30:00Z | When event occurred |

#### Triggering Use Case
```
CreateCompanyUseCase.execute()
  │
  ├─ Validate CNPJ is unique
  ├─ Create Company aggregate
  ├─ Persist via CompanyRepository.create()
  └─ Domain event automatically published to RabbitMQ
```

#### Message Queue
- **Topic/Exchange**: `integration.company`
- **Queue**: `integration.company.shipmentorder.created`
- **Routing Key**: `shipmentorder.company.created`

#### Consumers
1. **ShipmentOrderContext** - `CompanyCreatedListener`
   - Use Case: `SynchronizeCompanyUseCase`
   - Action: Store company data in local schema
   - Database: `shipmentorder.companies`

#### Event Sequence Number
1st event type published by Company context

#### Related Events
- **Preconditions**: Company must not exist with same CNPJ
- **Postconditions**: 
  - Company exists in company.companies table
  - Event in company.outbox table
  - Event published to RabbitMQ

---

### 2. CompanyUpdated

**Classification**: Domain Event  
**Context**: Company (Publisher)  
**Aggregate**: Company  
**Time-based**: Yes (Instant.now())

#### Purpose
Signals that company information has been modified. This event keeps the Shipment Order context synchronized with the latest company data.

#### Event Structure
```java
public class CompanyUpdated extends AbstractDomainEvent {
    private final UUID companyId;           // Root aggregate ID
    private final String property;          // Which property changed
    private final String oldValue;          // Previous value
    private final String newValue;          // New value
}
```

#### Attributes
| Field | Type | Example | Purpose |
|-------|------|---------|---------|
| domainEventId | UUID | 550e8400-e29b-41d4-a716-446655440002 | Event identifier |
| companyId | UUID | 550e8400-e29b-41d4-a716-446655440001 | Company aggregate ID |
| property | String | "name" or "configurations" | Field that changed |
| oldValue | String | "Old Company Name" | Previous state |
| newValue | String | "New Company Name" | Current state |
| occurredOn | Instant | 2024-11-25T11:45:00Z | When event occurred |

#### Triggering Use Case
```
UpdateCompanyUseCase.execute()
  │
  ├─ Load Company aggregate
  ├─ Call updateName() [returns new instance]
  ├─ Persist via CompanyRepository.update()
  └─ Domain event automatically published to RabbitMQ
```

#### Message Queue
- **Topic/Exchange**: `integration.company`
- **Queue**: `integration.shipmentorder.company.updated`
- **Routing Key**: `shipmentorder.company.updated`

#### Consumers
1. **ShipmentOrderContext** - `CompanyUpdatedListener`
   - Use Case: `SynchronizeCompanyUseCase`
   - Action: Update specific property in local schema
   - Database: `shipmentorder.companies`

#### Event Sequence Number
2nd event type published by Company context

#### Related Events
- **Preconditions**: Company must exist
- **Postconditions**:
  - Company updated in company.companies table
  - Event in company.outbox table
  - Event published to RabbitMQ

#### Change Tracking
Property-based tracking allows consumers to:
- Apply partial updates
- Track what changed
- Implement selective synchronization

---

### 3. CompanyDeleted

**Classification**: Domain Event  
**Context**: Company (Publisher)  
**Aggregate**: Company  
**Time-based**: Yes (Instant.now())

#### Purpose
Signals that a company has been removed from the system. This event notifies the Shipment Order context to clean up associated data.

#### Event Structure
```java
public class CompanyDeleted extends AbstractDomainEvent {
    private final UUID companyId;           // Root aggregate ID
    private final String company;           // Company name (for reference)
}
```

#### Attributes
| Field | Type | Example | Purpose |
|-------|------|---------|---------|
| domainEventId | UUID | 550e8400-e29b-41d4-a716-446655440003 | Event identifier |
| companyId | UUID | 550e8400-e29b-41d4-a716-446655440001 | Company aggregate ID |
| company | String | "Amazon Brasil" | Company name for traceability |
| occurredOn | Instant | 2024-11-25T12:00:00Z | When event occurred |

#### Triggering Use Case
```
DeleteCompanyByIdUseCase.execute()
  │
  ├─ Load Company aggregate
  ├─ Validate deletion allowed
  ├─ Delete via CompanyRepository.delete()
  └─ Domain event automatically published to RabbitMQ
```

#### Message Queue
- **Topic/Exchange**: `integration.company`
- **Queue**: `integration.shipmentorder.company.deleted`
- **Routing Key**: `shipmentorder.company.deleted`

#### Consumers
1. **ShipmentOrderContext** - (Currently no listener, but available for future use)
   - Potential Action: Remove or archive company data from local schema
   - Database: `shipmentorder.companies`

#### Event Sequence Number
3rd event type published by Company context

#### Related Events
- **Preconditions**: Company must exist
- **Postconditions**:
  - Company deleted from company.companies table
  - Event in company.outbox table
  - Event published to RabbitMQ

---

### 4. ShipmentOrderCreated

**Classification**: Domain Event  
**Context**: Shipment Order (Publisher)  
**Aggregate**: ShipmentOrder  
**Time-based**: Yes (Instant.now())

#### Purpose
Signals that a new shipment order has been created. This event notifies the Company context to increment the order counter for tracking purposes.

#### Event Structure
```java
public class ShipmentOrderCreated extends AbstractDomainEvent {
    private final UUID shipmentOrderId;     // Root aggregate ID
    private final UUID companyId;           // Reference to company
    private final UUID shipperId;           // External shipper reference
    private final String externalId;        // External system reference
}
```

#### Attributes
| Field | Type | Example | Purpose |
|-------|------|---------|---------|
| domainEventId | UUID | 550e8400-e29b-41d4-a716-446655440004 | Event identifier |
| shipmentOrderId | UUID | 550e8400-e29b-41d4-a716-446655440101 | Order aggregate ID |
| companyId | UUID | 550e8400-e29b-41d4-a716-446655440001 | Company this order belongs to |
| shipperId | UUID | 550e8400-e29b-41d4-a716-446655440102 | Shipper identifier |
| externalId | String | "ORD-2024-001" | Reference to external system |
| occurredOn | Instant | 2024-11-25T13:15:00Z | When event occurred |

#### Triggering Use Case
```
CreateShipmentOrderUseCase.execute()
  │
  ├─ Validate company exists (in shipmentorder.companies)
  ├─ Create ShipmentOrder aggregate
  ├─ Persist via ShipmentOrderRepository.create()
  └─ Domain event automatically published to RabbitMQ
```

#### Message Queue
- **Topic/Exchange**: `integration.shipmentorder`
- **Queue**: `integration.company.shipmentorder.created`
- **Routing Key**: `company.shipmentorder.created`

#### Consumers
1. **CompanyContext** - `IncrementShipmentOrderListener`
   - Use Case: `IncrementShipmentOrderUseCase`
   - Action: Increment order counter for the company
   - Database: `company.companies` (metadata field)

#### Event Sequence Number
1st event type published by Shipment Order context

#### Related Events
- **Preconditions**: Referenced company must exist
- **Postconditions**:
  - ShipmentOrder created in shipmentorder.shipment_orders table
  - Event in shipmentorder.outbox table
  - Event published to RabbitMQ
  - Company order counter incremented

#### External Identifiers
The externalId field allows:
- Tracking orders across systems
- Idempotent processing
- Integration with external systems

---

### 5. ShipmentOrderRetrieved

**Classification**: Domain Event  
**Context**: Shipment Order (Publisher)  
**Aggregate**: ShipmentOrder  
**Time-based**: Yes (Instant.now())

#### Purpose
Signals that a shipment order's information has been retrieved or synchronized from an external system. This indicates a status update or data refresh.

#### Event Structure
```java
public class ShipmentOrderRetrieved extends AbstractDomainEvent {
    private final UUID orderId;             // Root aggregate ID
    private final String externalId;        // External reference
}
```

#### Attributes
| Field | Type | Example | Purpose |
|-------|------|---------|---------|
| domainEventId | UUID | 550e8400-e29b-41d4-a716-446655440005 | Event identifier |
| orderId | UUID | 550e8400-e29b-41d4-a716-446655440101 | Order aggregate ID |
| externalId | String | "ORD-2024-001" | Reference to external system |
| occurredOn | Instant | 2024-11-25T14:30:00Z | When event occurred |

#### Triggering Use Case
```
Currently not triggered by any use case - available for future implementation
Example scenarios:
- Status check from external API
- Webhook callback from logistics provider
- Scheduled sync job
```

#### Message Queue
- **Topic/Exchange**: `integration.shipmentorder`
- **Queue**: (Available for future consumers)
- **Routing Key**: `shipmentorder.retrieved`

#### Consumers
- (None currently, available for future implementation)
- Could trigger: Notification system, reporting, analytics

#### Event Sequence Number
2nd event type published by Shipment Order context

#### Related Events
- **Associated with**: ShipmentOrderCreated
- **Use cases**: 
  - Track when external systems have been queried
  - Update local cache with latest status
  - Generate audit trail

---

## Event Statistics

| Metric | Value |
|--------|-------|
| **Total Events** | 5 |
| **Publisher Contexts** | 2 |
| **Consuming Contexts** | 2 |
| **Event Listeners** | 3 |
| **Integration Points** | 2 |
| **Queues** | 4 |

### Event Distribution by Context

**Company Context - Published**:
- CompanyCreated
- CompanyUpdated
- CompanyDeleted

**Shipment Order Context - Published**:
- ShipmentOrderCreated
- ShipmentOrderRetrieved

**Shipment Order Context - Consumed**:
- CompanyCreated (→ SynchronizeCompanyUseCase)
- CompanyUpdated (→ SynchronizeCompanyUseCase)

**Company Context - Consumed**:
- ShipmentOrderCreated (→ IncrementShipmentOrderUseCase)

---

## Event Flow Diagrams

### Happy Path: Company Registration & Order Creation

```
TIME ────────────────────────────────────────────────────────►

Company Context                    Event Bus                   ShipmentOrder Context
─────────────────────────────────────────────────────────────────────────────────

User Input
  │
  ├─► CreateCompanyUseCase.execute()
  │     ├─ Validate CNPJ
  │     └─ Company.createCompany()
  │
  └─ CompanyCreated Event
         │
         ▼
    [Outbox Table]
         │
         ├──────────────────► RabbitMQ ◄────────────────┐
         │                  (integration.company)       │
         │                                              │
         │                                    [Queue: integration.shipmentorder.company.created]
         │                                              │
         │                                              ▼
         │                                   CompanyCreatedListener
         │                                         │
         │                                         └─► SynchronizeCompanyUseCase
         │                                              └─ Store in local schema
         │
         └─ Return to User
              │
              ▼
           [Company Created]

         (User creates shipment order)
                                                     │
                                                     ▼
                                          CreateShipmentOrderUseCase.execute()
                                                     │
                                          ├─ Validate company exists
                                          │  (check shipmentorder.companies)
                                          │
                                          └─ ShipmentOrder.createOrder()
                                                     │
                                          ShipmentOrderCreated Event
                                                     │
                                                     ▼
                                               [Outbox Table]
                                                     │
                                          ┌──────────┴──────────┐
                                          │                     │
                                          ▼                     ▼
                                    [Local Persist]    RabbitMQ Event
                                          │            (integration.shipmentorder)
                                          │                     │
                                          │                     ├──────────────────┐
                                          │                     │                  │
                                          │                     │        [Queue: integration.company.shipmentorder.created]
                                          │                     │                  │
                                          │                     │                  ▼
                                          │                     │        IncrementShipmentOrderListener
Company Context                          │                     │                  │
─────────────────────────────────────────┼─────────────────────┼──────────────────┤
                                          │                     │        IncrementShipmentOrderUseCase
User gets response                       │                     │          └─ Increment counter
                                          │                     │
         ◄─────────────────────────────────┴─────────────────────┤
         │                                              │
         ▼                                              ▼
    [ShipmentOrder Created]              [Company Order Counter Updated]
```

### Branch: Company Data Synchronization

```
Company Context                    Event Bus                   ShipmentOrder Context
─────────────────────────────────────────────────────────────────────────────────

User Updates Company
  │
  ├─► UpdateCompanyUseCase.execute()
  │     ├─ Load Company
  │     └─ company.updateName()  [returns new instance]
  │
  └─ CompanyUpdated Event
         │
         ▼
    [Outbox Table]
         │
         └──────────────────► RabbitMQ ◄────────────────┐
         │                  (integration.company)       │
         │                                              │
         │                         [Queue: integration.shipmentorder.company.updated]
         │                                              │
         │                                              ▼
         │                                   CompanyUpdatedListener
         │                                         │
         │                                         └─► SynchronizeCompanyUseCase
         │                                              └─ Update local schema
         │
         └─ Return to User
```

---

## Event Processing Guarantees

All events in the TMS system follow these guarantees:

### Delivery Guarantees
- **At-Least-Once**: RabbitMQ ensures events are delivered at least once
- **Idempotent Consumers**: Listeners are designed to handle duplicate events safely
- **Ordered Delivery**: Within a single queue, events maintain order

### Consistency Model
- **Eventual Consistency**: Cross-context references are eventually consistent
- **Strong Consistency**: Within-context operations are strongly consistent
- **Event Sourcing**: All domain changes recorded as events

### Retry & Error Handling
- **Max Retries**: Configured per listener (typically 3-5 retries)
- **Dead Letter Queue**: Failed events go to DLQ for investigation
- **Manual Intervention**: DLQ messages reviewed by operations team

---

## Event Naming Conventions

All events in TMS follow these naming rules:

1. **Past Tense**: `CompanyCreated` not `CreateCompany`
2. **Aggregate Name**: `ShipmentOrderCreated` indicates ShipmentOrder aggregate
3. **Clarity**: Event name clearly indicates what happened
4. **No Prefixes**: Don't use "Event" suffix (it's implicit)

Examples:
- ✅ `CompanyCreated`
- ✅ `ShipmentOrderRetrieved`
- ❌ `OnCompanyCreated`
- ❌ `CompanyWasCreated`
- ❌ `CreateCompanyEvent`
