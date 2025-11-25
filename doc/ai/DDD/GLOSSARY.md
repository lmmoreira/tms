# DDD Glossary for TMS

A comprehensive glossary of Domain-Driven Design and TMS-specific terminology.

---

## Core DDD Concepts

### Aggregate
**Definition**: A cluster of associated objects treated as a unit for the purpose of data changes.

**In TMS**:
- **Company Aggregate**: Root aggregate consisting of Company + Agreements
- **ShipmentOrder Aggregate**: Single entity aggregate
- **Boundary**: Clear entry point through root entity
- **Invariants**: Business rules maintained within aggregate

**Key Principle**: Changes to aggregate are all-or-nothing transactions.

```
Company Aggregate:
┌─────────────────────────────┐
│ Company (Root)              │
│ ├─ Agreement 1              │
│ │  ├─ Condition 1           │
│ │  └─ Condition 2           │
│ └─ Agreement 2              │
│    ├─ Condition 1           │
│    └─ Condition 2           │
└─────────────────────────────┘

When you save Company, all related Agreements
and Conditions are saved together.
```

**See Also**: Root Aggregate, Invariant, Boundary

---

### Aggregate Root
**Definition**: The entity that is the entry point to the aggregate. All external access goes through the root.

**In TMS**:
- **Company** is the root aggregate in Company context
- **ShipmentOrder** is the root aggregate in ShipmentOrder context
- Only the root has a public repository
- Only the root is directly persisted

**Key Principle**: External objects should hold references to the root only.

```
✓ CORRECT:
  ShipmentOrder.getCompanyId()  // Access via root
  
✗ WRONG:
  ShipmentOrder.getCompanyData().getProperties()  // Direct access
  order.agreements  // Direct access to nested entities
```

**See Also**: Aggregate, Repository, Invariant

---

### Bounded Context
**Definition**: A demarcation of a subsystem, a product, or effort where a particular model applies.

**In TMS**:
1. **Company Bounded Context**
   - Models: Company, Agreement
   - Domain: Company management and agreements
   - Database: `company.*` schema
   - Team: Company domain team

2. **Shipment Order Bounded Context**
   - Models: ShipmentOrder
   - Domain: Order management and tracking
   - Database: `shipmentorder.*` schema
   - Team: Shipment domain team

**Key Principle**: Each context has its own model, terminology, and database.

```
┌─────────────────────┐       ┌──────────────────┐
│ COMPANY CONTEXT     │       │ SHIPMENT CONTEXT │
│                     │       │                  │
│ Uses: Company       │       │ Uses: Order      │
│       Agreement     │       │       CompanyData│
│                     │       │                  │
└─────────────────────┘       └──────────────────┘
```

**See Also**: Context Map, Ubiquitous Language

---

### Context Map
**Definition**: A visual representation showing bounded contexts and their relationships.

**In TMS**:
- Shows Company and ShipmentOrder contexts
- Shows communication channels (RabbitMQ)
- Shows which context publishes which events
- Shows data flows and synchronization

**Key Principle**: Helps understand system as a whole.

**See Also**: Bounded Context, Relationship

---

### Domain Event
**Definition**: An event that represents something of importance to the business that has happened.

**In TMS**:
```
✓ CompanyCreated    - Important: Company registered
✓ CompanyUpdated    - Important: Company information changed
✓ CompanyDeleted    - Important: Company removed
✓ ShipmentOrderCreated - Important: Order placed
✓ ShipmentOrderRetrieved - Important: Order retrieved
```

**Characteristics**:
- Past tense (Created, not Create)
- Immutable once published
- Contains all data needed for consumers
- Should be serializable

**Example**:
```java
public class CompanyCreated extends AbstractDomainEvent {
    private final UUID companyId;
    private final String company;
    private final Set<String> types;
    // Contains everything consumers need to know
}
```

**See Also**: Event Sourcing, Outbox Pattern

---

### Entity
**Definition**: An object that has identity and changes over time.

**In TMS**:
- **Company**: Has identity (CompanyId), can change
- **Agreement**: Has identity (AgreementId), can change
- **ShipmentOrder**: Has identity (ShipmentOrderId), immutable

**Characteristics**:
- Same identity = same entity (even if attributes differ)
- Mutable (in many systems, immutable in TMS)
- Lives in a specific bounded context

```
// Same company (same identity, different state)
Company company1 = company.updateName("New Name");
Company company2 = company.updateName("New Name");

// Same companyId = same entity
company1.getCompanyId().equals(company2.getCompanyId())  // true

// But objects are different instances
company1 == company2  // false
```

**See Also**: Value Object, Aggregate, Root Aggregate

---

### Invariant
**Definition**: A business rule or constraint that must always be true for an aggregate.

**In TMS**:

**Company Invariants**:
- CNPJ must be valid (14 digits)
- Name cannot be empty
- Must have at least one type
- CNPJ must be unique within context

**ShipmentOrder Invariants**:
- CompanyId must reference existing company
- ExternalId must be unique
- Cannot be modified after creation

**Key Principle**: Aggregate guarantees invariants are maintained.

```
// ✓ Maintains invariant: CNPJ valid
Company company = new Company(..., new Cnpj("34028316000152"), ...);

// ✗ Violates invariant: Invalid CNPJ
Company company = new Company(..., new Cnpj("invalid"), ...);
                                     ↑
                        Validation error thrown
```

**See Also**: Aggregate, Boundary, Aggregate Root

---

### Repository
**Definition**: A mechanism for encapsulating storage, retrieval, and search behavior of aggregates.

**In TMS**:

**Company Repository Interface**:
```java
interface CompanyRepository {
    Company create(Company company);
    Company update(Company company);
    void delete(CompanyId id);
    Optional<Company> getCompanyById(CompanyId id);
    Optional<Company> getCompanyByCnpj(Cnpj cnpj);
}
```

**Characteristics**:
- One repository per root aggregate
- Only for root aggregates (not for nested entities)
- Handles persistence and retrieval
- Handles event outbox (Outbox Pattern)

**See Also**: Aggregate, Aggregate Root, Outbox Pattern

---

### Value Object
**Definition**: An object that represents a concept, but has no identity. Equality is based on value, not identity.

**In TMS**:

**Examples**:
```
✓ CompanyId(UUID)
✓ Cnpj(String)
✓ CompanyType (enum)
✓ AgreementId(UUID)
✓ ShipmentOrderId(UUID)
✓ CompanyData(companyId, data)
```

**Characteristics**:
- Immutable (in TMS, uses records)
- No identity (two objects with same value are equal)
- Validation in constructor
- Can be shared without issue
- Lightweight

**Example**:
```java
// Value Objects
record CompanyId(UUID value) {
    public CompanyId {
        if (value == null) throw new ValidationException(...);
    }
}

record Cnpj(String value) {
    public Cnpj {
        if (!isValid(value)) throw new ValidationException(...);
    }
}

// Two CompanyIds with same value are equal
CompanyId id1 = new CompanyId(uuid);
CompanyId id2 = new CompanyId(uuid);
id1.equals(id2)  // true
```

**See Also**: Entity, Immutable, Record

---

### Ubiquitous Language
**Definition**: A shared language between developers and domain experts, using domain terminology in code.

**In TMS**:

**Business Language** ↔ **Code**:
```
Business: "A company is registered"
Code: CompanyCreated event

Business: "We need to track shipments"
Code: ShipmentOrder aggregate

Business: "Company's registration number"
Code: Cnpj value object

Business: "Types of companies"
Code: CompanyType enum

Business: "Agreements with conditions"
Code: Agreement and AgreementCondition entities
```

**Key Principle**: Same terms used in conversations and code.

```
// ✓ Uses domain language
class Company {
    private final Cnpj cnpj;
    private final Set<CompanyType> types;
}

// ✗ Generic language
class Company {
    private final String registrationNumber;
    private final Set<String> classifications;
}
```

**See Also**: Bounded Context

---

## Integration Patterns

### Anti-Corruption Layer
**Definition**: A mechanism that prevents a bounded context from being corrupted by external models.

**In TMS**:
- **CompanyData** value object acts as anti-corruption layer
- Shields ShipmentOrder from Company context changes
- Transforms event data at boundary

```
Company Context              Anti-Corruption         ShipmentOrder Context
──────────────              ───────────────         ──────────────────

Company entity              CompanyData VO           ShipmentOrder
 ├─ id                      ├─ companyId             ├─ companyData (VO)
 ├─ name                    └─ data: Map             └─ references above
 ├─ cnpj
 ├─ types
 └─ configurations

Company event ─► parse ─► transform ─► CompanyData VO ─► Store locally
                                           ↑
                                  Isolation from Company changes
```

**See Also**: Bounded Context, Integration Pattern

---

### Event Bus
**Definition**: A mechanism for publishing and subscribing to events across bounded contexts.

**In TMS**:
- **Implementation**: RabbitMQ
- **Model**: Publish/Subscribe
- **Reliability**: At-least-once delivery
- **Ordering**: Per-queue ordering

```
Company Context            RabbitMQ                  ShipmentOrder Context
───────────────            ────────                  ──────────────────

CompanyCreated ──────────► Exchange ───────────────► Queue ────────► Listener
                          (integration.company)    (integration....) ├─ Deserialize
                                                                      ├─ UseCase
                                                                      └─ Store
```

**See Also**: Event-Driven, Eventual Consistency

---

### Event Sourcing
**Definition**: Storing the state of an entity as a sequence of immutable state-changing events.

**In TMS**:
- **Partially implemented**: Outbox pattern for events
- **Full implementation**: Would store only events, rebuild state
- **Currently**: Traditional storage + events for integration

**See Also**: Outbox Pattern, Domain Event

---

### Eventual Consistency
**Definition**: A consistency model where updates to data across bounded contexts are guaranteed to happen eventually.

**In TMS**:
```
Company created:        T=0s  (Company context)
                        ↓
Event published:        T=0-1s (Outbox processor)
                        ↓
Event in queue:         T=1-2s (RabbitMQ)
                        ↓
Listener processes:     T=2-3s (ShipmentOrder)
                        ↓
Data synchronized:      T=3s+ (Both contexts consistent)

Consistency window: ~2-3 seconds (typical)
```

**Key Principle**: Acceptable for cross-context references.

**See Also**: Event-Driven, CQRS

---

### Outbox Pattern
**Definition**: A pattern that ensures transactional consistency between aggregates and event publishing.

**In TMS**:

**Problem Solved**: Dual-write consistency
```
Without outbox:
  Save to DB ──┐
               ├─ Both may fail inconsistently
  Publish ────┘

With outbox:
  Save to DB ──┬─ Aggregate table
               ├─ Outbox table (same transaction)
               └─ Both succeed or both fail
                       ↓
  Background processor publishes
                       ↓
  Update outbox status: PUBLISHED
```

**Implementation**:
```
1. Save Company + Events in single transaction
2. Background job polls outbox table
3. Publishes unpublished events
4. Marks as published
```

**See Also**: Event Sourcing, Domain Event

---

### Saga Pattern
**Definition**: A pattern for managing distributed transactions across bounded contexts.

**In TMS**:
- **Not currently used**
- **Could use for**: Complex multi-step operations
- **Alternative**: Event-driven eventual consistency

**When to use**:
- Need ACID guarantees across contexts
- Complex choreography between contexts
- Compensations needed on failure

**See Also**: Event-Driven

---

## Data Synchronization

### Denormalization
**Definition**: Storing redundant copies of data to optimize queries.

**In TMS**:
- **shipmentorder.companies** is denormalized copy of **company.companies**
- Allows ShipmentOrder to operate independently
- Updated via events

```
company.companies          shipmentorder.companies
┌─────────────────┐       ┌────────────────────┐
│ id              │       │ id                 │
│ name            │◄──────│ data (JSON)        │
│ types           │ Event │                    │
│ configurations  │ Sync  └────────────────────┘
└─────────────────┘
```

**Benefits**:
- Fast local queries
- Independent scaling
- Context autonomy

**Downsides**:
- Eventual consistency
- Synchronization overhead
- Data duplication

**See Also**: Eventual Consistency, Anti-Corruption Layer

---

### Synchronization
**Definition**: Keeping copies of data across systems in sync.

**In TMS**:
```
Company updates name
        ↓
CompanyUpdated event
        ↓
Published to RabbitMQ
        ↓
ShipmentOrder listener receives
        ↓
Updates shipmentorder.companies
```

**Approaches**:
1. **Event-driven** (TMS approach): Changes trigger sync
2. **Polling**: Periodically check source
3. **Batch**: Bulk synchronization at intervals

**See Also**: Event-Driven, Eventual Consistency

---

## Database Patterns

### Schema Per Context
**Definition**: Each bounded context has its own database schema.

**In TMS**:
- **company schema**: All Company context tables
- **shipmentorder schema**: All ShipmentOrder context tables
- **No cross-schema foreign keys**

```
Database: tms
├── Schema: company
│   ├── companies
│   ├── agreements
│   ├── agreement_conditions
│   └── outbox
└── Schema: shipmentorder
    ├── shipment_orders
    ├── companies (denormalized)
    └── outbox
```

**Benefits**:
- Clear boundaries
- Independent scaling
- Migration independence
- Multi-database ready

**See Also**: Bounded Context, Database Migration

---

## Lifecycle Concepts

### Immutability
**Definition**: Once created, an object cannot be changed.

**In TMS**:
- **Company aggregate**: Immutable
  - Updates return NEW instance
  - Original unchanged
  
- **ShipmentOrder aggregate**: Immutable
  - Cannot change after creation
  - Deletion not supported (archive instead)

- **Value Objects**: Immutable
  - Records enforce immutability
  - No setters

```java
// Immutable update
Company updated = company.updateName("New Name");
// company is unchanged
// updated is new instance
company.getName()    // Still "Old Name"
updated.getName()    // "New Name"
```

**Benefits**:
- Thread-safe
- Easy to reason about
- Reduces bugs

**See Also**: Aggregate, Value Object

---

### Lifecycle
**Definition**: The states an entity passes through.

**Company Lifecycle**:
```
Created ──► Active ──► Updated ──► Active ──► Deleted
  │          │          │                        │
  ├─ Event: CompanyCreated
  ├─ Event: CompanyUpdated (multiple times)
  └─ Event: CompanyDeleted
```

**ShipmentOrder Lifecycle**:
```
Created ──► Active ──► Retrieved ──► Completed
  │
  ├─ Event: ShipmentOrderCreated
  └─ Event: ShipmentOrderRetrieved
```

**See Also**: State, Event Sourcing

---

## Quality Attributes

### Cohesion
**Definition**: How closely entities in a module are related.

**In TMS**:
- **High cohesion**: Each context has related entities
- **Company context**: Company, Agreement (related)
- **ShipmentOrder context**: ShipmentOrder, CompanyData (related)

**See Also**: Bounded Context, Coupling

---

### Coupling
**Definition**: Degree of interdependence between modules.

**In TMS**:
- **Low coupling between contexts**: Via events only
- **No direct calls between contexts**
- **Eventual consistency**: Accepts temporal coupling

```
✓ LOW COUPLING:
  Company ──event──> RabbitMQ ──event──> ShipmentOrder

✗ HIGH COUPLING:
  Company ──direct call──> ShipmentOrder
```

**See Also**: Cohesion, Bounded Context

---

### Scalability
**Definition**: System's ability to handle growth.

**In TMS**:
- **Horizontal**: Add more instances per context
- **Asynchronous**: Events decouple load
- **Independent**: Each context scales separately

**See Also**: Event-Driven, Schema Per Context

---

## Testing Concepts

### Integration Test
**Definition**: Testing multiple components working together.

**In TMS**:
- **Context-level**: Company context creates, ShipmentOrder consumes
- **Cross-context**: Full flow with both contexts
- **Infrastructure**: Databases, RabbitMQ included

**See Also**: Unit Test

---

### Unit Test
**Definition**: Testing a single component in isolation.

**In TMS**:
- **Aggregate tests**: Domain logic
- **No external dependencies**: No DB, no RabbitMQ
- **Fast execution**

**See Also**: Integration Test

---

## Operational Concepts

### Deployment
**Definition**: Releasing code to production.

**In TMS**:
- **Independent**: Each context deployed separately
- **Backward compatibility**: Maintain event formats
- **Rolling**: Update one service at a time

**See Also**: Versioning

---

### Versioning
**Definition**: Managing changes to contracts.

**In TMS**:
- **Events**: Never delete fields (add new, deprecate old)
- **DTOs**: Support multiple versions
- **API**: Semantic versioning

**See Also**: Deployment, Migration

---

## Process Concepts

### Event Storming
**Definition**: Workshop technique to discover events and processes.

**In TMS**:
- **Participants**: Developers, domain experts, stakeholders
- **Output**: Event catalog, context map, processes
- **Result**: Documented bounded contexts

**See Also**: Domain Event, Bounded Context

---

## Quick Reference Table

| Term | Definition | In TMS |
|------|-----------|--------|
| Aggregate | Cluster of objects, unit of change | Company, ShipmentOrder |
| Bounded Context | Subsystem with its own model | Company, ShipmentOrder |
| Domain Event | Business event that happened | CompanyCreated, OrderCreated |
| Entity | Object with identity, changes over time | Company, ShipmentOrder |
| Value Object | No identity, immutable | Cnpj, CompanyId, CompanyData |
| Repository | Storage & retrieval for aggregates | CompanyRepository, ShipmentOrderRepository |
| Ubiquitous Language | Shared domain terminology | Used throughout TMS |
| Anti-Corruption Layer | Boundary protection | CompanyData VO |
| Event Bus | Inter-context communication | RabbitMQ |
| Eventual Consistency | Eventually consistent across contexts | Company ↔ ShipmentOrder |
| Outbox Pattern | Transactional event publishing | company.outbox, shipmentorder.outbox |
| Schema Per Context | Separate DB per context | company.*, shipmentorder.* |

---

## Resources

- [Event Storming Documentation](./EVENT_STORMING.md)
- [Bounded Contexts Documentation](./BOUNDED_CONTEXTS.md)
- [Context Map Documentation](./CONTEXT_MAP.md)
- [Integration Patterns Documentation](./INTEGRATION_PATTERNS.md)
- [Entity Relationship Diagrams](./ENTITY_RELATIONSHIP_DIAGRAMS.md)
