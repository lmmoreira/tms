# TMS Glossary

**Purpose:** Domain-specific terminology for the TMS project. For complete DDD/technical terms, see [GLOSSARY_FULL.md](GLOSSARY_FULL.md).

**Note for AI:** Modern LLMs already understand DDD, CQRS, and architectural patterns. This glossary focuses on TMS-specific domain terms.

---

## Domain Terms (TMS-Specific)

### Company Module

#### Company
An organization participating in logistics operations. Can be a **shipper** (sends goods), **carrier** (transports goods), or **logistics provider** (manages transportation).

**Type:** Aggregate Root  
**Key Fields:** CompanyId, name, CNPJ, types, configurations, agreements

#### CNPJ
**Cadastro Nacional da Pessoa Jurídica** - Brazilian business tax identification number (14 digits). Unique identifier for legal entities in Brazil.

**Format:** `XX.XXX.XXX/XXXX-XX` (stored as 14 digits)  
**Example:** `12.345.678/0001-90`  
**Type:** Value Object  
**Validation:** Required, must be 14 digits

#### Company Type
Classification of a company's role:
- **SHIPPER** - Sends goods (e.g., e-commerce marketplaces)
- **CARRIER** - Transports goods (e.g., delivery companies)
- **LOGISTICS_PROVIDER** - Manages transportation operations

**Type:** Enum

#### Agreement
Contract between companies defining service terms, pricing, and conditions for transportation services.

**Type:** Entity (within Company aggregate)  
**Key Fields:** Agreement ID, type, conditions, active status

#### Agreement Type
- **SUPPLIER** - Company provides services
- **CLIENT** - Company receives services

**Type:** Enum

#### Configuration
Key-value settings customizing company operations and behavior.

**Type:** Value Object  
**Implementation:** `Map<String, Object>`  
**Examples:** Notification preferences, API endpoints, business rules

---

### ShipmentOrder Module

#### Shipment Order
Request to transport goods from origin to destination. Represents full lifecycle of a transportation operation.

**Type:** Aggregate Root  
**Synonyms:** Order, Transportation Request  
**Status:** Created, In Transit, Delivered, Cancelled

#### Volume
Physical cargo characteristics:
- Weight (kg)
- Dimensions (length × width × height in cm)
- Quantity (packages)

**Purpose:** Calculate shipping costs, plan vehicle capacity

#### Quotation
Price estimate for transportation services.

**Based On:**
- Origin and destination
- Volume and weight
- Service type and urgency
- Route and carrier

**Status:** Pending, Approved, Rejected, Expired

---

## Technical Terms (TMS-Specific)

### CompanyId
UUID v7 wrapper for Company identification.

**Implementation:** `public record CompanyId(UUID value) { ... }`  
**Generation:** `CompanyId.unique()` uses `Id.unique()`

### ShipmentOrderId
UUID v7 wrapper for ShipmentOrder identification.

**Implementation:** `public record ShipmentOrderId(UUID value) { ... }`

### Shipment Order Counter
Number tracking how many shipment orders a company has processed. Incremented via domain events.

**Purpose:** Business analytics, volume tracking  
**Updated By:** Event listener receiving `ShipmentOrderCreated` events

---

## Queue Naming Convention

**Pattern:** `integration.{target-module}.{source-module}.{event}`

**Examples:**
- `integration.company.shipmentorder.created` - Company module listens to ShipmentOrder events
- `integration.shipmentorder.company.updated` - ShipmentOrder listens to Company events

---

## Module Names

| Module | Package | Purpose |
|--------|---------|---------|
| **commons** | `br.com.logistics.tms.commons` | Shared primitives & infrastructure |
| **company** | `br.com.logistics.tms.company` | Company management |
| **shipmentorder** | `br.com.logistics.tms.shipmentorder` | Order management |

---

## Common Abbreviations

- **TMS** - Transportation Management System
- **DDD** - Domain-Driven Design
- **CQRS** - Command Query Responsibility Segregation
- **DTO** - Data Transfer Object
- **JPA** - Java Persistence API
- **CNPJ** - Cadastro Nacional da Pessoa Jurídica (Brazilian tax ID)

---

## Technology-Specific Terms

### UUID v7 (ULID)
Time-based UUID variant used for all entity IDs in TMS.

**Format:** Sequential, sortable by creation time  
**Generation:** `Id.unique()` (commons module)  
**Reference:** [ADR-001](../adr/ADR-001-ID-Format.md)

### Outbox Table
Database table storing domain events before publishing to message broker.

**Tables:**
- `company_outbox` - Company module events
- `shipmentorder_outbox` - ShipmentOrder events

**Columns:** event_id, aggregate_id, event_type, payload, created_at, published_at

---

## For Complete Reference

See [GLOSSARY_FULL.md](GLOSSARY_FULL.md) for:
- Complete DDD terminology (Aggregate, Entity, Value Object, etc.)
- Architectural pattern definitions (Hexagonal, CQRS, Event-Driven)
- Technical concepts (Layer architecture, Repository pattern, etc.)
- Testing terminology

**Note:** AI assistants typically don't need DDD definitions - focus on TMS-specific domain terms above.

---

**Last Updated:** 2025-11-05

**Related:** [ARCHITECTURE.md](ARCHITECTURE.md) | [CODEBASE_CONTEXT.md](CODEBASE_CONTEXT.md)
