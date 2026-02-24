# Entity Relationship Diagrams (ERD) & Domain Models

This document provides visual representations of the domain models, entity relationships, and data structures within each bounded context.

---

## Company Bounded Context - Domain Model

### Class Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                     COMPANY BOUNDED CONTEXT                         │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────────────────────┐                              │
│  │ ◆ Company (Root Aggregate)       │                              │
│  ├──────────────────────────────────┤                              │
│  │ - companyId: CompanyId           │                              │
│  │ - name: String                   │                              │
│  │ - cnpj: Cnpj                     │                              │
│  │ - types: Set<CompanyType>        │                              │
│  │ - configurations: Map<S,Object>  │                              │
│  │ - domainEvents: List<Event>      │◄────────┐                   │
│  │ - persistentMetadata: Map<S,O>   │         │                   │
│  ├──────────────────────────────────┤         │                   │
│  │ + createCompany(..):Company      │         │                   │
│  │ + updateName(String):Company     │         │                   │
│  │ + delete():void                  │         │                   │
│  │ + getCompanyId():CompanyId       │         │                   │
│  │ + getName():String               │         │                   │
│  └──────────────────────────────────┘         │                   │
│         │         │         │                 │                   │
│         │ owns    │         │ publishes        │                   │
│         │         │         │                 │                   │
│         ▼         ▼         ▼                 │                   │
│                                              │                   │
│  ┌──────────────────┐  ┌────────────────┐   │                   │
│  │ ◆ Agreement      │  │ Domain Events: │◄──┴                   │
│  ├──────────────────┤  ├────────────────┤                       │
│  │ - agreementId: A │  │ CompanyCreated │                       │
│  │ - companyId: CI  │  │ CompanyUpdated │                       │
│  │ - type: AgrType  │  │ CompanyDeleted │                       │
│  │ - conditions: Set│  │                │                       │
│  │ - active: Bool   │  │ ◄ Published to │                       │
│  └──────────────────┘  │   RabbitMQ     │                       │
│         │              │   (Async)      │                       │
│         │ contains     └────────────────┘                       │
│         ▼                                                        │
│  ┌──────────────────────────────────┐                          │
│  │ ◆ AgreementCondition (ValueObj)  │                          │
│  ├──────────────────────────────────┤                          │
│  │ - conditionId: AgreementCondId   │                          │
│  │ - type: AgreementConditionType   │                          │
│  │ - details: Map<String, Object>   │                          │
│  └──────────────────────────────────┘                          │
│                                                                 │
│  ┌──────────────────────────────────┐                          │
│  │ VALUE OBJECTS                     │                          │
│  ├──────────────────────────────────┤                          │
│  │ • CompanyId(UUID)                │                          │
│  │ • AgreementId(UUID)              │                          │
│  │ • AgreementConditionId(UUID)     │                          │
│  │ • Cnpj(String) [validates]       │                          │
│  │ • CompanyType enum {             │                          │
│  │   SHIPPER, CARRIER,              │                          │
│  │   WAREHOUSE, DISTRIBUTOR         │                          │
│  │ }                                 │                          │
│  │ • AgreementType enum {           │                          │
│  │   SERVICE, COMMERCIAL,           │                          │
│  │   OPERATIONAL                    │                          │
│  │ }                                 │                          │
│  │ • AgreementConditionType enum {  │                          │
│  │   PRICING, SLA, CAPACITY,        │                          │
│  │   DELIVERY_TIME                  │                          │
│  │ }                                 │                          │
│  └──────────────────────────────────┘                          │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Database Schema (Company Context)

```sql
┌─────────────────────────────────────────────────────────────┐
│ Schema: company                                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Table: companies                                           │
│  ┌─────────────────────────────────────────────────────┐  │
│  │ id (UUID) ◄─── PK                                  │  │
│  │ name (VARCHAR 255) ◄─── Required                   │  │
│  │ cnpj (VARCHAR 14) ◄─── Required, Unique            │  │
│  │ types (JSONB) ◄─── Array of types                  │  │
│  │ configurations (JSONB) ◄─── Configuration data     │  │
│  │ persistent_metadata (JSONB) ◄─── System metadata   │  │
│  │ created_at (TIMESTAMP) ◄─── Auto                   │  │
│  │ updated_at (TIMESTAMP) ◄─── Auto                   │  │
│  └─────────────────────────────────────────────────────┘  │
│           ▲                                                 │
│           │ 1..* (one company has many agreements)        │
│           │                                                 │
│  Table: agreements                                          │
│  ┌─────────────────────────────────────────────────────┐  │
│  │ id (UUID) ◄─── PK                                  │  │
│  │ company_id (UUID) ◄─── FK → companies.id           │  │
│  │ type (VARCHAR 50)                                   │  │
│  │ active (BOOLEAN) ◄─── Status                       │  │
│  │ created_at (TIMESTAMP)                              │  │
│  │ updated_at (TIMESTAMP)                              │  │
│  └─────────────────────────────────────────────────────┘  │
│           │                                                 │
│           │ 1..* (one agreement has many conditions)      │
│           ▼                                                 │
│  Table: agreement_conditions                               │
│  ┌─────────────────────────────────────────────────────┐  │
│  │ id (UUID) ◄─── PK                                  │  │
│  │ agreement_id (UUID) ◄─── FK → agreements.id        │  │
│  │ type (VARCHAR 50)                                   │  │
│  │ details (JSONB)                                     │  │
│  │ created_at (TIMESTAMP)                              │  │
│  └─────────────────────────────────────────────────────┘  │
│                                                             │
│  Table: outbox ◄─── Event persistence (Outbox Pattern)    │
│  ┌─────────────────────────────────────────────────────┐  │
│  │ id (UUID) ◄─── PK                                  │  │
│  │ aggregate_id (UUID)                                 │  │
│  │ event_type (VARCHAR 255) ◄─── Event class name    │  │
│  │ payload (JSONB) ◄─── Serialized event             │  │
│  │ published (BOOLEAN) ◄─── Delivery status          │  │
│  │ created_at (TIMESTAMP)                              │  │
│  │ published_at (TIMESTAMP)                            │  │
│  └─────────────────────────────────────────────────────┘  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Entity Relationships

```
companies (1) ──────────────┐
                            │ FK: company_id
                            │
agreements (*) ─────────────┘
    │
    │
    └────────────────┐
                     │ FK: agreement_id
                     │
agreement_conditions (*)

Cardinalities:
  1 Company has 0..* Agreements
  1 Agreement has 1..* Conditions
  1 Company has 0..* Conditions (transitively)
```

---

## Shipment Order Bounded Context - Domain Model

### Class Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                 SHIPMENT ORDER BOUNDED CONTEXT                      │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────────────────────────┐                          │
│  │ ◆ ShipmentOrder (Root Aggregate)     │                          │
│  ├──────────────────────────────────────┤                          │
│  │ - shipmentOrderId: ShipmentOrderId   │                          │
│  │ - companyId: CompanyId               │                          │
│  │ - shipperId: UUID                    │                          │
│  │ - externalId: String [unique]        │                          │
│  │ - companyData: CompanyData ◄────┐    │                          │
│  │ - domainEvents: List<Event>     │    │◄─────────────────┐       │
│  │ - persistentMetadata: Map<S,O>  │    │                 │       │
│  ├──────────────────────────────────────┤                 │       │
│  │ + createOrder(..):ShipmentOrder │    │                 │       │
│  │ + retrieve():void               │    │                 │       │
│  │ + getShipmentOrderId():SId      │    │                 │       │
│  │ + getCompanyId():CompanyId      │    │                 │       │
│  └──────────────────────────────────────┘                 │       │
│           │               │                               │       │
│           │ publishes     │ references                    │       │
│           │               │                               │       │
│           ▼               ▼                               │       │
│                                                          │       │
│  ┌──────────────────┐  ┌──────────────────────────────┐ │       │
│  │ Domain Events:   │  │ ◆ CompanyData (ValueObj)    │ │       │
│  ├──────────────────┤  ├──────────────────────────────┤ │       │
│  │ ShipmentCreated  │  │ - companyId: UUID           │ │       │
│  │ ShipmentRetrieved│  │ - data: Map<String, Object> │ │       │
│  │                  │  ├──────────────────────────────┤ │       │
│  │ ◄ Published to   │  │ + getName():String          │ │       │
│  │   RabbitMQ       │  │ + getTypes():Set<String>    │ │       │
│  │   (Async)        │  │ + getConfigData(..):Object  │ │       │
│  │                  │  └──────────────────────────────┘ │       │
│  │ ◄ Consumed from: │         ▲                        │       │
│  │   CompanyCreated │         │ Contains synchronized  │       │
│  │   CompanyUpdated │         │ Company context data   │       │
│  └──────────────────┘         │                        │       │
│                               │                        │       │
│  ┌──────────────────────────────────────┐              │       │
│  │ VALUE OBJECTS                         │              │       │
│  ├──────────────────────────────────────┤              │       │
│  │ • ShipmentOrderId(UUID)               │              │       │
│  │ • CompanyId(UUID)                     │◄─ Ref to────┘       │
│  │                                       │  Company context    │
│  └──────────────────────────────────────┘                      │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Database Schema (Shipment Order Context)

```sql
┌─────────────────────────────────────────────────────────────┐
│ Schema: shipmentorder                                       │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Table: shipment_orders                                     │
│  ┌─────────────────────────────────────────────────────┐  │
│  │ id (UUID) ◄─── PK                                  │  │
│  │ company_id (UUID) ◄─── Reference to Company       │  │
│  │ shipper_id (UUID) ◄─── External shipper ref       │  │
│  │ external_id (VARCHAR 255) ◄─── Unique external ID │  │
│  │ company_data (JSONB) ◄─── Denormalized data       │  │
│  │ created_at (TIMESTAMP) ◄─── Auto                  │  │
│  │ updated_at (TIMESTAMP) ◄─── Auto                  │  │
│  └─────────────────────────────────────────────────────┘  │
│           │                                                 │
│           │ References (Eventual Consistency)              │
│           │ [May not exist until Company sync completes]   │
│           │                                                 │
│           └─────────┐                                       │
│                     ▼                                       │
│  Table: companies ◄─── Local Company Data Cache            │
│  ┌─────────────────────────────────────────────────────┐  │
│  │ id (UUID) ◄─── PK                                  │  │
│  │ data (JSONB) ◄─── From CompanyCreated/Updated      │  │
│  │ created_at (TIMESTAMP)                              │  │
│  │ updated_at (TIMESTAMP)                              │  │
│  └─────────────────────────────────────────────────────┘  │
│                                                             │
│  Table: outbox ◄─── Event persistence (Outbox Pattern)    │
│  ┌─────────────────────────────────────────────────────┐  │
│  │ id (UUID) ◄─── PK                                  │  │
│  │ aggregate_id (UUID)                                 │  │
│  │ event_type (VARCHAR 255) ◄─── Event class name    │  │
│  │ payload (JSONB) ◄─── Serialized event             │  │
│  │ published (BOOLEAN) ◄─── Delivery status          │  │
│  │ created_at (TIMESTAMP)                              │  │
│  │ published_at (TIMESTAMP)                            │  │
│  └─────────────────────────────────────────────────────┘  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Entity Relationships

```
companies (1) ─────────┐
                       │ Synchronized data
                       │ (via Events)
                       │
shipment_orders (*)────┘

Cardinalities:
  1 Company can have 0..* ShipmentOrders
  1 ShipmentOrder belongs to 1 Company
  
Consistency:
  - ShipmentOrder.company_id references Company
  - But relationship is eventual (not enforced foreign key)
  - Local companies table is denormalized copy
  - Actual Company exists in Company context
```

---

## Cross-Context Data Flow

### Complete Data Model with Integration

```
┌──────────────────────────────────────────────────────────────────────┐
│                    COMPLETE SYSTEM MODEL                             │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  Company Context                  Data Sync (Events)                │  ShipmentOrder Context
│  ───────────────                  ──────────────────                │  ──────────────────────
│                                                                      │
│  Entities:                         ┌──────────────────────────┐     │  Entities:
│  • Company                         │                          │     │  • ShipmentOrder
│  • Agreement                       │  CompanyCreated Event    │     │
│  • AgreementCondition             │  companyId               │     │  Synchronized:
│                                   │  company (name)          │     │  • CompanyData
│  Stores:                          │  types                   │     │    (local copy)
│  ├─ company.companies      ◄──┐  │                          │     │
│  ├─ company.agreements     │  │  │  ┌─────────────────┐     │     │  Stores:
│  ├─ company.agreement_      │  │  │  │   RabbitMQ      │     │     │  ├─ shipmentorder.
│  │  conditions             │  │  │  │  (Async Bus)    │     │     │  │  shipment_orders
│  └─ company.outbox        │  │  │  └─────────────────┘     │     │  ├─ shipmentorder.
│                           │  │  │         │                 │     │  │  companies
│  Published Events:        │  │  │         │                 │     │  └─ shipmentorder.outbox
│  ├─ CompanyCreated    ───┘  │  │         │                 │     │
│  ├─ CompanyUpdated    ─────┴──┼─────────┤                 │     │  Published Events:
│  └─ CompanyDeleted         │  │         │                 │     │  ├─ ShipmentOrderCreated ──┐
│                            │  │         ▼                 │     │  └─ ShipmentOrderRetrieved │
│  Consumed Events:          │  │   Listener:               │     │                            │
│  └─ ShipmentOrderCreated ──┴──┼─► CompanyCreatedListener  │     │  Consumed Events:         │
│                            │  │   CompanyUpdatedListener  │     │  ├─ CompanyCreated ───────┤
│                            │  │   (No CompanyDeleted yet) │     │  └─ CompanyUpdated ───────┤
│                            │  │         │                 │     │                            │
│                            │  │         ├─► UseCase:      │     │  Processing:              │
│                            │  │         │  SynchronizeC.. │     │  └─ IncrementShipmentO..  │
│                            │  │         │       │          │     │     (tracks metrics)      │
│                            │  │         └─► INSERT        │     │                            │
│                            │  │            shipmentorder. │     │                            │
│                            │  │            companies      │     │                            │
│                            │  └──────────────────────────┘     │
│                            │                                    │
│                            │    ┌─────────────────┐             │
│                            │    │  Event Bus      │             │
│                            │    │  (Reverse       │             │
│                            └───►│  Direction)     │◄────────────┤
│                                 └─────────────────┘             │
│                                        ▲                        │
│                                        │                        │
│                              ShipmentOrderCreated                │
│                              (Triggers counter inc.)             │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

---

## Key Relationships Summary

### Company Context
```
Company (1) ──one-to-many──> Agreement (many)
     │
     └──────────────────────────────> Agreement Conditions
              (one-to-many per agreement)

Relationships:
  ✓ Company.id (PK) ← Agreement.company_id (FK)
  ✓ Agreement.id (PK) ← AgreementCondition.agreement_id (FK)
```

### Shipment Order Context
```
ShipmentOrder (1) ──references──> CompanyData (1) ────┐
                                   (ValueObject,        │
                                    Not FK)             │
                                                        │
                                   Eventually           │
                                   consistent with:     │
                                   Company.id (from     │
                                   Company context)     │
                                   (in companies table)
```

### Cross-Context
```
Company.id ─────────┐
                    │ Same UUID
                    │
ShipmentOrder.company_id
ShipmentOrder.companyData.companyId
shipmentorder.companies.id
```

---

## Data Lifecycle

### Company Creation & Sync

```
Timeline:

T=0s    User creates Company
        ↓
        POST /companies
        ├─ CompanyRepository.create()
        ├─ INSERT company.companies
        ├─ INSERT company.outbox
        └─ Response: ✓ (Synchronous)

T=0-1s  Background processor
        ├─ SELECT company.outbox (PENDING)
        ├─ Publish to RabbitMQ
        └─ UPDATE company.outbox (PUBLISHED)

T=1-2s  RabbitMQ delivery
        ├─ Route to appropriate queue
        └─ Await consumer

T=2-3s  ShipmentOrder listener
        ├─ Receive message
        ├─ CompanyCreatedListener
        ├─ SynchronizeCompanyUseCase
        ├─ INSERT shipmentorder.companies
        └─ Acknowledge message ✓

T=3s+   Company data available in ShipmentOrder
        ├─ Now can create orders
        └─ Relationship established
```

### Order Creation & Notification

```
Timeline:

T=0s    User creates ShipmentOrder
        ↓
        POST /orders
        ├─ Validate company exists
        │  └─ Query shipmentorder.companies ✓
        ├─ ShipmentOrderRepository.create()
        ├─ INSERT shipmentorder.shipment_orders
        ├─ INSERT shipmentorder.outbox
        └─ Response: ✓ (Synchronous)

T=0-1s  Background processor
        ├─ SELECT shipmentorder.outbox (PENDING)
        ├─ Publish to RabbitMQ
        └─ UPDATE shipmentorder.outbox (PUBLISHED)

T=1-2s  RabbitMQ delivery
        ├─ Route to appropriate queue
        └─ Await consumer

T=2-3s  Company listener
        ├─ Receive message
        ├─ IncrementShipmentOrderListener
        ├─ IncrementShipmentOrderUseCase
        ├─ UPDATE company.companies (increment counter)
        └─ Acknowledge message ✓

T=3s+   Company metrics updated
        ├─ Order count reflects new order
        └─ Tracking complete
```

---

## Model Validation Rules

### Company Aggregate
- ✓ CompanyId is never null
- ✓ Name is not empty
- ✓ CNPJ matches regex `\d{14}`
- ✓ CNPJ is unique per context
- ✓ At least one CompanyType
- ✓ Immutable (updates return new instance)

### ShipmentOrder Aggregate
- ✓ ShipmentOrderId is never null
- ✓ CompanyId is never null
- ✓ ExternalId is unique per order
- ✓ CompanyData must be valid VO
- ✓ Immutable after creation
- ✓ Cannot delete, only archive (future)

### CompanyData Value Object
- ✓ CompanyId is never null
- ✓ Data map defaults to empty if null
- ✓ Immutable (record type)
- ✓ Safe to synchronize multiple times

---

## Performance Considerations

### Company Context
- **indexes**: (cnpj), (id)
- **Outbox**: Indexed by published (for polling)
- **Read/Write**: Balanced

### Shipment Order Context
- **indexes**: (company_id), (external_id), (id)
- **companies table**: Indexed by id (for sync lookup)
- **Outbox**: Indexed by published (for polling)
- **Read**: Heavy (queries by company)
- **Write**: Heavy (order creation)

### Cross-Context
- No direct foreign keys between contexts
- Referential integrity not enforced at DB level
- Data consistency maintained at application level
- Eventual consistency acceptance required
