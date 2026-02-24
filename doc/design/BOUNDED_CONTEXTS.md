# Bounded Contexts in TMS

## Overview

The Transportation Management System is organized into two primary bounded contexts, each with its own domain model, repository, and business rules. These contexts share a common infrastructure for event processing and communication.

```
┌─────────────────────────────────────────────────────────────────┐
│                    Transportation Management System             │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────────────┐           ┌──────────────────────┐  │
│  │   COMPANY CONTEXT    │           │  SHIPMENT ORDER      │  │
│  │                      │           │    CONTEXT           │  │
│  │  ┌────────────────┐  │           │                      │  │
│  │  │ Root Aggregate │  │           │  ┌────────────────┐  │  │
│  │  │                │  │           │  │ Root Aggregate │  │  │
│  │  │ • Company      │  │           │  │                │  │  │
│  │  │ • Agreement    │  │           │  │ • ShipmentOrder│  │  │
│  │  │                │  │           │  │                │  │  │
│  │  └────────────────┘  │           │  └────────────────┘  │  │
│  │                      │           │                      │  │
│  │  ┌────────────────┐  │           │  ┌────────────────┐  │  │
│  │  │Value Objects:  │  │           │  │Value Objects:  │  │  │
│  │  │                │  │           │  │                │  │  │
│  │  │ • CompanyId    │  │           │  │ • ShipmentId   │  │  │
│  │  │ • Cnpj         │  │           │  │ • CompanyData  │  │  │
│  │  │ • CompanyType  │  │           │  │   (Sync copy)  │  │  │
│  │  │ • AgreementId  │  │           │  │                │  │  │
│  │  │                │  │           │  └────────────────┘  │  │
│  │  └────────────────┘  │           │                      │  │
│  │                      │           │                      │  │
│  └────────────┬─────────┘           └────────────┬─────────┘  │
│               │                                  │              │
│               │ Events: CompanyCreated           │ Events:      │
│               │          CompanyUpdated          │ ShipmentOrder│
│               │          CompanyDeleted          │ Created      │
│               │                                  │ Retrieved    │
│               └──────────────┬───────────────────┘              │
│                              │                                 │
│                    ┌─────────▼──────────┐                      │
│                    │  Event Bus         │                      │
│                    │  (RabbitMQ Queues) │                      │
│                    └────────────────────┘                      │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 1. Company Bounded Context

**Responsibility**: Manage company information, configurations, types, and agreements.

### Domain Model

#### Root Aggregate: Company

The `Company` aggregate is the main entity in this context. It represents a real-world company that uses the TMS system.

**Identity**: `CompanyId` (UUID)

**Key Properties**:
- `companyId`: Unique identifier (CompanyId)
- `name`: Company name (String)
- `cnpj`: Corporate registration number (Cnpj - Value Object)
- `types`: Set of company types (CompanyType enum)
- `configurations`: Map of configuration data (JSON)
- `domainEvents`: Collection of events that have occurred
- `persistentMetadata`: Metadata for persistence

**Invariants**:
- CNPJ must be valid (14 digits)
- Name cannot be empty
- Company must have at least one type
- CompanyId is immutable

**Example Structure**:
```
Company {
  id: CompanyId("12345678-1234-5678-1234-567812345678"),
  name: "Amazon Brasil",
  cnpj: Cnpj("34028316000152"),
  types: {CompanyType.SHIPPER, CompanyType.CARRIER},
  configurations: {
    "warehouse_address": "São Paulo, SP",
    "contact_email": "contact@amazon.com"
  }
}
```

#### Root Aggregate: Agreement

Represents contractual agreements between companies.

**Identity**: `AgreementId` (UUID)

**Key Properties**:
- `agreementId`: Unique identifier
- `companyId`: Reference to company (foreign key)
- `type`: Agreement type (AgreementType enum)
- `conditions`: Set of agreement conditions
- `active`: Boolean flag for active status

**Relationships**:
- **Belongs to**: Company (via CompanyId)
- **Contains**: AgreementCondition (value objects)

**Example Structure**:
```
Agreement {
  id: AgreementId("87654321-8765-4321-8765-432187654321"),
  companyId: CompanyId("12345678-1234-5678-1234-567812345678"),
  type: AgreementType.SERVICE,
  conditions: {
    AgreementCondition {
      id: AgreementConditionId(...),
      type: AgreementConditionType.PRICING,
      details: {...}
    }
  },
  active: true
}
```

### Value Objects

#### CompanyId
- **Type**: Value Object (Record)
- **Format**: UUID
- **Immutable**: Yes
- **Validation**: Non-null UUID

#### Cnpj
- **Type**: Value Object (Record)
- **Format**: 14-digit string
- **Immutable**: Yes
- **Validation**: Must match regex `\d{14}`

#### CompanyType
- **Type**: Enum
- **Values**: 
  - `SHIPPER` - Company ships goods
  - `CARRIER` - Company transports goods
  - `WAREHOUSE` - Company stores goods
  - `DISTRIBUTOR` - Company distributes goods
- **Immutable**: Yes

#### AgreementId
- **Type**: Value Object (Record)
- **Format**: UUID
- **Immutable**: Yes

#### AgreementType
- **Type**: Enum
- **Values**:
  - `SERVICE` - Service agreement
  - `COMMERCIAL` - Commercial agreement
  - `OPERATIONAL` - Operational agreement

#### AgreementConditionType
- **Type**: Enum
- **Values**:
  - `PRICING` - Pricing condition
  - `SLA` - Service level agreement
  - `CAPACITY` - Capacity constraint
  - `DELIVERY_TIME` - Time constraint

### Events Generated

1. **CompanyCreated**
   - When: New company is registered
   - Contains: companyId, name, types
   - Used by: ShipmentOrder context (synchronize)

2. **CompanyUpdated**
   - When: Company data changes
   - Contains: companyId, property, oldValue, newValue
   - Used by: ShipmentOrder context (update local copy)

3. **CompanyDeleted**
   - When: Company is removed
   - Contains: companyId, name
   - Used by: ShipmentOrder context (cleanup)

### Use Cases

1. **CreateCompanyUseCase** - Register new company
2. **GetCompanyByIdUseCase** - Retrieve company details
3. **UpdateCompanyUseCase** - Modify company information
4. **DeleteCompanyByIdUseCase** - Remove company
5. **AddConfigurationToCompanyUseCase** - Add configuration entry
6. **IncrementShipmentOrderUseCase** - Track shipment orders (from external event)

### Repository Interface

```
CompanyRepository {
  create(company: Company): Company
  update(company: Company): Company
  delete(companyId: CompanyId): void
  getCompanyById(companyId: CompanyId): Optional<Company>
  getCompanyByCnpj(cnpj: Cnpj): Optional<Company>
}
```

### Database Schema

**Table**: `company.companies`

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | UUID | PRIMARY KEY | Company identifier |
| name | VARCHAR(255) | NOT NULL | Company name |
| cnpj | VARCHAR(14) | NOT NULL, UNIQUE | Corporate registration |
| types | JSONB | NOT NULL | Set of company types |
| configurations | JSONB | NOT NULL | Configuration data |
| persistent_metadata | JSONB | | System metadata |
| created_at | TIMESTAMP | NOT NULL | Creation timestamp |
| updated_at | TIMESTAMP | | Last update timestamp |

---

## 2. Shipment Order Bounded Context

**Responsibility**: Manage shipment orders, their lifecycle, and tracking.

### Domain Model

#### Root Aggregate: ShipmentOrder

The main entity representing a shipment order in the system.

**Identity**: `ShipmentOrderId` (UUID)

**Key Properties**:
- `shipmentOrderId`: Unique identifier (ShipmentOrderId)
- `companyId`: Reference to company (CompanyId)
- `shipperId`: External shipper ID (UUID)
- `externalId`: Reference to external system ID (String)
- `companyData`: Synchronized copy of company info (CompanyData - Value Object)
- `domainEvents`: Emitted events
- `persistentMetadata`: Metadata

**Invariants**:
- ShipmentOrderId is immutable
- CompanyId must exist (validated against synchronized copy)
- ExternalId must be unique
- ShipmentOrder cannot be modified after retrieval

**Example Structure**:
```
ShipmentOrder {
  id: ShipmentOrderId("99999999-9999-9999-9999-999999999999"),
  companyId: CompanyId("12345678-1234-5678-1234-567812345678"),
  shipperId: UUID("11111111-1111-1111-1111-111111111111"),
  externalId: "ORDER-001-2024",
  companyData: CompanyData {
    companyId: UUID("12345678-1234-5678-1234-567812345678"),
    data: {
      "name": "Amazon Brasil",
      "warehouse_address": "São Paulo, SP"
    }
  }
}
```

### Value Objects

#### ShipmentOrderId
- **Type**: Value Object (Record)
- **Format**: UUID
- **Immutable**: Yes

#### CompanyData
- **Type**: Value Object (Record)
- **Properties**:
  - `companyId`: UUID (external reference)
  - `data`: Map<String, Object> (synchronized data)
- **Purpose**: Local copy of company data for validation and reference
- **Immutable**: Yes
- **Synchronization**: Updated when Company context sends CompanyUpdated events

### Events Generated

1. **ShipmentOrderCreated**
   - When: New shipment order is created
   - Contains: shipmentOrderId, companyId, shipperId, externalId
   - Published to: Company context (triggers IncrementShipmentOrderUseCase)

2. **ShipmentOrderRetrieved**
   - When: Shipment order is retrieved from external system
   - Contains: orderId, externalId
   - Indicates: Order status updated

### External Events Consumed

1. **CompanyCreated** (from Company context)
   - Triggers: SynchronizeCompanyUseCase
   - Action: Store company data locally

2. **CompanyUpdated** (from Company context)
   - Triggers: SynchronizeCompanyUseCase
   - Action: Update local company data

### Use Cases

1. **CreateShipmentOrderUseCase** - Register new shipment order
2. **GetShipmentOrderByCompanyIdUseCase** - Retrieve orders for company
3. **SynchronizeCompanyUseCase** - Sync company data (from events)

### Repository Interface

```
ShipmentOrderRepository {
  create(order: ShipmentOrder): ShipmentOrder
  getShipmentOrderById(id: ShipmentOrderId): Optional<ShipmentOrder>
  getShipmentOrdersByCompanyId(companyId: CompanyId): List<ShipmentOrder>
}
```

### Database Schema

**Table**: `shipmentorder.shipment_orders`

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | UUID | PRIMARY KEY | Order identifier |
| company_id | UUID | NOT NULL | Reference to company |
| shipper_id | UUID | NOT NULL | External shipper |
| external_id | VARCHAR(255) | UNIQUE | External system reference |
| company_data | JSONB | NOT NULL | Synchronized company info |
| created_at | TIMESTAMP | NOT NULL | Creation timestamp |

**Table**: `shipmentorder.companies` (Denormalized/Synchronized)

| Column | Type | Purpose |
|--------|------|---------|
| id | UUID | Company identifier |
| data | JSONB | Synchronized company data |
| created_at | TIMESTAMP | Sync timestamp |
| updated_at | TIMESTAMP | Last update |

---

## Context Responsibilities Summary

| Aspect | Company | Shipment Order |
|--------|---------|-----------------|
| **Owns** | Company info, Agreements | Orders, Order tracking |
| **Publishes** | CompanyCreated, Updated, Deleted | ShipmentCreated, Retrieved |
| **Consumes** | ShipmentOrderCreated | CompanyCreated, Updated |
| **Database** | company schema | shipmentorder schema |
| **Query Model** | Direct access | Synchronized copy |

---

## Key Principles

1. **Autonomy** - Each context owns its data and operations
2. **Eventual Consistency** - References between contexts are eventually consistent
3. **Immutability** - Domain objects don't change, new instances are created
4. **Event-Driven** - Communication happens through published events
5. **Clear Boundaries** - No direct calls between contexts, only events

---

## Context Statistics

### Company Context
- Root Aggregates: 2 (Company, Agreement)
- Value Objects: 5 (CompanyId, Cnpj, CompanyType, AgreementId, AgreementType)
- Events Published: 3
- Use Cases: 6
- Database Tables: 1

### Shipment Order Context
- Root Aggregates: 1 (ShipmentOrder)
- Value Objects: 2 (ShipmentOrderId, CompanyData)
- Events Published: 2
- Events Consumed: 2
- Use Cases: 3
- Database Tables: 2
