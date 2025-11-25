# Context Map

The Context Map shows how the Company and Shipment Order bounded contexts relate to and communicate with each other. This document maps out the relationships, integration patterns, and data flows between contexts.

## Context Relationship Overview

```
┌──────────────────────────────────────────────────────────────────────┐
│                        TMS CONTEXT MAP                               │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│   ┌──────────────────────────┐       ┌──────────────────────────┐   │
│   │                          │       │                          │   │
│   │   COMPANY CONTEXT        │       │  SHIPMENT ORDER CONTEXT  │   │
│   │                          │       │                          │   │
│   │  ┌────────────────────┐  │       │  ┌────────────────────┐  │   │
│   │  │  Company (Root)    │  │       │  │ ShipmentOrder Root │  │   │
│   │  └────────────────────┘  │       │  └────────────────────┘  │   │
│   │                          │       │                          │   │
│   │  ┌────────────────────┐  │       │  ┌────────────────────┐  │   │
│   │  │ Agreement (Root)   │  │       │  │ CompanyData (VO)   │  │   │
│   │  └────────────────────┘  │       │  │ [Synchronized]     │  │   │
│   │                          │       │  └────────────────────┘  │   │
│   │  Database: company..*    │       │  Database: shipmentorder.*  │   │
│   │                          │       │                          │   │
│   └────────────┬─────────────┘       └──────────────┬─────────────┘   │
│                │                                    │                 │
│                │  ┌──────────────────────────────────────────────┐   │
│                │  │   Integration Channel                        │   │
│                │  │   Type: Event-Based Async                   │   │
│                │  │   Medium: RabbitMQ                          │   │
│                │  │   Pattern: Publish/Subscribe                │   │
│                │  └──────────────────────────────────────────────┘   │
│                │                                    │                 │
│                ├───► PublishEvent ─────────────────┤                 │
│                │     CompanyCreated                 │                 │
│                │     CompanyUpdated                 ▼                 │
│                │     CompanyDeleted            ConsumeEvent           │
│                │                               SynchronizeCompany     │
│                │                                                      │
│                │  ┌──────────────────────────────────────────────┐   │
│                │  │ Synchronization Data                         │   │
│                │  │                                              │   │
│                │  │ ShipmentOrder stores:                        │   │
│                │  │  • companyId (reference)                    │   │
│                │  │  • companyData (denormalized copy)           │   │
│                │  └──────────────────────────────────────────────┘   │
│                │                                    │                 │
│                │  ┌──────────────────────────────────────────────┐   │
│                │  │ Return Events to Company                     │   │
│                │  └──────────────────────────────────────────────┘   │
│                │                                    │                 │
│                ▼                                    ├───► PublishEvent │
│           ConsumeEvent                             │     ShipmentOrder│
│           ShipmentOrderCreated                     │     Created      │
│                │                                    │                 │
│                ├─ IncrementShipmentOrderCounter    │                 │
│                │                                    │                 │
│                └─ Track metrics                    └─ Return to sender│
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Relationship Type: PARTNERSHIP

The Company and Shipment Order contexts have a **PARTNERSHIP** relationship.

### Characteristics
- **Symmetrical Communication**: Both contexts publish and consume events
- **Peer Relationship**: Neither context is subordinate to the other
- **Shared Responsibility**: Each manages its own domain while coordinating
- **Event-Driven**: Loose coupling through asynchronous messaging
- **Eventual Consistency**: References eventually consistent across contexts

### Nature of the Partnership
```
Company          ShipmentOrder
  │                   │
  ├─ Owns: Company    ├─ Owns: Orders
  │  data             │  and Tracking
  │                   │
  ├─ Publishes:       ├─ Publishes:
  │  Company events   │  Order events
  │                   │
  ├─ Consumes:        ├─ Consumes:
  │  Order events     │  Company events
  │                   │
  └─ Uses Data:       └─ Uses Data:
     N/A                 Company info
                         (synchronized)
```

---

## Integration Channels

### Channel 1: Company → Shipment Order

**Direction**: Company publishes, ShipmentOrder consumes  
**Pattern**: Publish/Subscribe  
**Transport**: RabbitMQ  
**Consistency Model**: Eventual  

#### Published Events
1. **CompanyCreated**
   - Publishes to: `integration.company` exchange
   - Queue: `integration.shipmentorder.company.created`
   - Consumer: `CompanyCreatedListener`
   - Action: Store company in local schema

2. **CompanyUpdated**
   - Publishes to: `integration.company` exchange
   - Queue: `integration.shipmentorder.company.updated`
   - Consumer: `CompanyUpdatedListener`
   - Action: Update company in local schema

3. **CompanyDeleted**
   - Publishes to: `integration.company` exchange
   - Queue: `integration.shipmentorder.company.deleted`
   - Consumer: (None currently, available for future)
   - Action: (To be implemented)

#### Data Synchronization
```
Company Context                    Shipment Order Context
───────────────────────────────────────────────────────

Company Aggregate                  [Event Received]
  ├─ companyId                         │
  ├─ name                              ├─ Parse event
  ├─ cnpj                              │
  ├─ types                             ├─ Extract companyId, company, types
  └─ configurations                    │
         │                             ├─ Store in shipmentorder.companies
         │                             │
         ├─ Publish Event  ───────────┤
         │                             ├─ CompanyData VO created
         │                             │
         │                             └─ Ready for order validation
         │
    [Outbox Published to RMQ]
```

#### Synchronization Guarantees
- **At-Least-Once**: Company events will be delivered at least once
- **Idempotent**: ShipmentOrder can safely process duplicate CompanyCreated events
- **Ordering**: Events for same company maintain order

### Channel 2: Shipment Order → Company

**Direction**: ShipmentOrder publishes, Company consumes  
**Pattern**: Publish/Subscribe  
**Transport**: RabbitMQ  
**Consistency Model**: Eventual  

#### Published Events
1. **ShipmentOrderCreated**
   - Publishes to: `integration.shipmentorder` exchange
   - Queue: `integration.company.shipmentorder.created`
   - Consumer: `IncrementShipmentOrderListener`
   - Action: Increment order counter for company

2. **ShipmentOrderRetrieved**
   - Publishes to: `integration.shipmentorder` exchange
   - Queue: (None currently)
   - Consumer: (None currently)
   - Action: (Available for future)

#### Notification & Tracking
```
Shipment Order Aggregate           Company Context
────────────────────────────────────────────────

ShipmentOrder created              [Event Received]
  ├─ shipmentOrderId                   │
  ├─ companyId                         ├─ Parse event
  ├─ shipperId                         │
  └─ externalId                        ├─ Extract companyId
         │                             │
         │                             ├─ Increment shipmentOrderCount
         ├─ Publish Event  ───────────┤   for company
         │                             │
         │                             └─ Update company metadata
         │
    [Outbox Published to RMQ]
```

#### Tracking Guarantees
- **Metrics**: Accurate order counts per company
- **Idempotent**: Company can safely increment same event multiple times

---

## Shared Entities & Anti-Corruption Layer

### Shared References: Company ID

The `companyId` (UUID) is a shared reference between contexts.

```
Company Context                    Shipment Order Context
───────────────────────────────────────────────────────

Company Aggregate                  ShipmentOrder Aggregate
  └─ companyId: UUID                └─ companyId: UUID
                                         └─ Reference to Company
                                            context's entity

                                     CompanyData Value Object
                                       └─ Denormalized copy of
                                          company information
```

### Anti-Corruption Layer: CompanyData

The ShipmentOrder context doesn't import Company entities directly. Instead, it uses **CompanyData** value object as an anti-corruption layer.

**Purpose**:
- Isolate ShipmentOrder from Company schema changes
- Maintain clear context boundaries
- Enable independent testing and deployment
- Support local data synchronization

**Implementation**:
```
Company Event ──► Parse ──► Extract data ──► Store as CompanyData VO
                                                    │
                                          Local schema (shipmentorder)
                                                    │
                                          Used for validation
```

---

## Context Dependencies

### Forward Dependencies (What each context depends on)

#### Company Context Dependencies
- **Internal**: Company Aggregate, Agreement Aggregate
- **External**: 
  - Event from ShipmentOrder (ShipmentOrderCreated)
  - Implicit dependency: Company must exist before orders

#### Shipment Order Context Dependencies
- **Internal**: ShipmentOrder Aggregate
- **External**:
  - Data from Company (via CompanyData synchronization)
  - Company must be created first (validation dependency)
  - Company updates (for consistency)

### Dependency Graph
```
ShipmentOrderCreated ──────────────┐
                                   │
                                   ▼
                          [Company Context]
                                   │
                     CompanyId must exist ◄─┐
                                           │
                          ┌────────────────┘
                          │
                          ▼
                  [ShipmentOrder Context]
                    (Can create orders)

Data Flow:
Company ─► CompanyCreated ──► RabbitMQ ──► ShipmentOrder
                                              (Sync data)
```

---

## Integration Points Detail

### Integration Point 1: Company Data Synchronization

**Trigger**: CompanyCreated or CompanyUpdated event

**Flow**:
```
[1] Company publishes CompanyCreated
    ├─ Event contains: companyId, name, types
    │
[2] ShipmentOrder context receives event
    │
[3] CompanyCreatedListener invoked
    ├─ Deserializes CompanyCreatedDTO
    │
[4] SynchronizeCompanyUseCase.execute()
    ├─ Extract company data from DTO
    ├─ Create CompanyData VO
    ├─ Store/Update in shipmentorder.companies table
    │
[5] Synchronization complete
    └─ ShipmentOrder can now validate company exists
```

**Database Tables Involved**:
- **Source**: `company.companies` (Company context)
- **Destination**: `shipmentorder.companies` (ShipmentOrder context)

**Data Synchronized**:
- companyId (PK)
- name
- types
- configurations
- Created/updated timestamps

**Synchronization Frequency**: 
- Immediate on CompanyCreated
- Immediate on CompanyUpdated
- One-time on CompanyDeleted (future)

### Integration Point 2: Order Counting

**Trigger**: ShipmentOrderCreated event

**Flow**:
```
[1] ShipmentOrder publishes ShipmentOrderCreated
    ├─ Event contains: shipmentOrderId, companyId, shipperId, externalId
    │
[2] Company context receives event
    │
[3] IncrementShipmentOrderListener invoked
    ├─ Deserializes ShipmentOrderCreatedDTO
    │
[4] IncrementShipmentOrderUseCase.execute()
    ├─ Extract companyId from event
    ├─ Load Company aggregate
    ├─ Increment shipmentOrderCount in metadata
    ├─ Persist updated Company
    │
[5] Counter incremented
    └─ Company metadata reflects accurate order count
```

**Database Tables Involved**:
- **Source**: `shipmentorder.shipment_orders` (ShipmentOrder context)
- **Destination**: `company.companies` (Company context)

**Data Synchronized**:
- companyId
- Increment operation only

**Synchronization Frequency**: 
- Immediate on ShipmentOrderCreated
- Eventual consistency (RabbitMQ delivery guarantee)

---

## Network of Contexts Diagram

```
                    ┌─────────────────────────────┐
                    │       External Systems      │
                    │   (Not in scope)            │
                    └─────────────────────────────┘
                                 ▲
                                 │
                    ┌────────────┴────────────┐
                    │                         │
          Integration Points:         Integration Points:
          • Create Company             • Create Order
          • Update Company             • Query Orders
          • Delete Company             • Sync Company
                    │                         │
                    ▼                         ▼
         ┌──────────────────┐       ┌──────────────────┐
         │  COMPANY CONTEXT │ ◄───► │ SHIPMENT ORDER   │
         │                  │       │ CONTEXT          │
         │  Database:       │       │                  │
         │  company.*       │       │ Database:        │
         │                  │       │ shipmentorder.*  │
         └──────────────────┘       └──────────────────┘
                    ▲                         ▲
                    │                         │
                    └──────────┬──────────────┘
                               │
                    ┌──────────▼──────────┐
                    │   RabbitMQ (Event   │
                    │   Transport Layer)  │
                    │                     │
                    │  Exchanges:         │
                    │  • integration.     │
                    │    company          │
                    │  • integration.     │
                    │    shipmentorder    │
                    │                     │
                    │  Queues:            │
                    │  • integration.*    │
                    │  • integration.*    │
                    │    (4 total)        │
                    └─────────────────────┘
```

---

## Communication Protocol

### Synchronous vs. Asynchronous

```
Within Context (Company Context)
────────────────────────────────
Synchronous:
  CreateCompanyUseCase ──► CompanyRepository ──► Database
  Response: Immediate


Between Contexts (Company ↔ ShipmentOrder)
──────────────────────────────────────────
Asynchronous:
  CompanyCreated Event ──► RabbitMQ ──► ShipmentOrder
  Response: Fire-and-forget
  Acknowledgment: At-least-once guarantee
  Latency: Eventual (milliseconds to seconds)
```

### Event Publishing & Consumption Model

```
Command Processing:
  Command ──► UseCase ──► Aggregate ──► Event ──► Outbox
                                           │
                                           └──► Published
                                                   │
Event Handling:
  Listener ◄── Queue ◄── Event ◄── RabbitMQ ◄── Outbox
    │
    ├─ Deserialize DTO
    ├─ Execute UseCase
    ├─ Acknowledge message
    │
    └─ Complete

Error Handling:
  Failed ──► Retry (3-5x) ──► Dead Letter Queue ──► Manual Review
```

---

## Consistency & Transactions

### Transaction Boundaries

**Within Context** (Strong Consistency):
```
Company Context:
  BeginTransaction
    ├─ Load Company
    ├─ Update aggregate
    ├─ Publish event to outbox
    ├─ Update database
  CommitTransaction
  
Result: Company + Event both persisted, or both rolled back
```

**Between Contexts** (Eventual Consistency):
```
Company Context               ShipmentOrder Context
    └─ Event published              │
       to RabbitMQ                   │
                                     ├─ Event in queue
                                     │  (may not be received yet)
                                     │
                                     ├─ Listener processes
                                     │  (async, separate transaction)
                                     │
                                     └─ ShipmentOrder data
                                        eventually consistent
```

### Consistency Window
- **Best Case**: < 100ms (same machine)
- **Typical Case**: < 1 second (local network)
- **Worst Case**: < 30 seconds (with RabbitMQ retries)
- **Guarantee**: At-least-once delivery

---

## Data Flows

### Complete Flow: Create Company → Create Order

```
1. HTTP POST /companies
   └─► CreateCompanyUseCase
       │
       ├─ Validate CNPJ unique
       │  └─ Query: company.companies
       │
       ├─ Create Company aggregate
       │  └─ CompanyCreated event placed
       │
       ├─ Save to DB
       │  ├─ INSERT company.companies
       │  └─ INSERT company.outbox
       │
       ├─ Publish event
       │  └─ RabbitMQ: integration.company
       │
       └─► Response: CompanyId

2. [Async] RabbitMQ delivers CompanyCreated
   │
   └─► ShipmentOrder Context
       │
       ├─ CompanyCreatedListener
       │
       ├─ SynchronizeCompanyUseCase
       │  ├─ Deserialize event
       │  ├─ Create CompanyData VO
       │  └─ INSERT shipmentorder.companies
       │
       └─ (Async complete, no response)

3. HTTP POST /orders
   └─► CreateShipmentOrderUseCase
       │
       ├─ Validate company exists
       │  └─ Query: shipmentorder.companies
       │     (Created by event sync in step 2)
       │
       ├─ Create ShipmentOrder aggregate
       │  └─ ShipmentOrderCreated event placed
       │
       ├─ Save to DB
       │  ├─ INSERT shipmentorder.shipment_orders
       │  └─ INSERT shipmentorder.outbox
       │
       ├─ Publish event
       │  └─ RabbitMQ: integration.shipmentorder
       │
       └─► Response: ShipmentOrderId

4. [Async] RabbitMQ delivers ShipmentOrderCreated
   │
   └─► Company Context
       │
       ├─ IncrementShipmentOrderListener
       │
       ├─ IncrementShipmentOrderUseCase
       │  ├─ Load Company
       │  ├─ Increment orderCount
       │  └─ UPDATE company.companies
       │
       └─ (Async complete, no response)
```

---

## Testing Contexts in Integration

### Testing Company Context in Isolation
```
Test Setup:
  ├─ Create Company aggregate
  └─ Verify events placed

No need to:
  ├─ Start RabbitMQ
  └─ Start ShipmentOrder service
```

### Testing ShipmentOrder Context in Isolation
```
Test Setup:
  ├─ Pre-populate shipmentorder.companies
  └─ Create ShipmentOrder aggregate

Challenges:
  ├─ Must have valid CompanyData
  └─ Can't test company sync without Company context
```

### Integration Testing Both Contexts
```
Test Setup:
  ├─ Start Company service
  ├─ Start ShipmentOrder service
  ├─ Start RabbitMQ (Testcontainers)
  └─ Start PostgreSQL (Testcontainers)

Full Flow Test:
  ├─ Create Company (Company context)
  ├─ Wait for sync event
  ├─ Verify company in ShipmentOrder context
  ├─ Create Order (ShipmentOrder context)
  ├─ Wait for event
  └─ Verify order count in Company context
```

---

## Monitoring & Observability

### Key Metrics to Track

1. **Event Throughput**
   - CompanyCreated/min
   - ShipmentOrderCreated/min

2. **Event Latency**
   - Time from creation to outbox
   - Time from outbox to queue
   - Time from queue to listener execution

3. **Synchronization Lag**
   - Company created → data in ShipmentOrder
   - Time to consistency

4. **Error Rates**
   - Event publish failures
   - Listener processing failures
   - Dead letter queue depth

### Debugging Integration Issues

```
Issue: Order creation fails with "company not found"

Root Cause Analysis:
  1. Check if company creation succeeded
     └─ Query company.companies
  
  2. Check if event was published
     └─ Query company.outbox
  
  3. Check if event reached RabbitMQ
     └─ Check RabbitMQ admin console
  
  4. Check if listener received event
     └─ Check application logs
  
  5. Check if data was synced
     └─ Query shipmentorder.companies

Expected Timeline:
  Company created: T+0s
  Event published: T+0-1s
  Listener triggered: T+1-5s
  Data in ShipmentOrder: T+5-10s
  Order creation can proceed: T+10+s
```

---

## Context Evolution

### Adding New Integration Points

If you need to add new events or listeners:

1. **Define Event** in source context domain layer
2. **Place Event** in aggregate method
3. **Configure Queue** in infrastructure
4. **Create Listener** in consuming context
5. **Update DTO** for message deserialization
6. **Implement UseCase** to process event
7. **Add Tests** for event flow

### Removing Integration Points

When removing event-based communication:

1. **Check Consumers** - What depends on this event?
2. **Migrate Data** - How will consumers get data after removal?
3. **Deprecate Listeners** - Add feature flags before removal
4. **Update Documentation** - Remove from context map
5. **Archive Queues** - Keep for audit trail, mark deprecated

---

## Summary

The Company and ShipmentOrder contexts form a **PARTNERSHIP** relationship where:

- ✅ Both are equally important
- ✅ Both publish and consume events
- ✅ Communication is asynchronous and event-driven
- ✅ Consistency is eventual within typical SLA
- ✅ Each context maintains its own database
- ✅ Clear anti-corruption layer (CompanyData) prevents tight coupling
- ✅ Integration is testable and observable
