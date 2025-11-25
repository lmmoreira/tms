# Event Catalog Summary

Quick reference for all events in the TMS system with their key attributes and flows.

---

## Events Overview Table

| Event | Context | Type | Triggered By | Consumer | Status |
|-------|---------|------|--------------|----------|--------|
| **CompanyCreated** | Company | Published | CreateCompanyUseCase | ShipmentOrder (SynchronizeCompanyUseCase) | ✓ Active |
| **CompanyUpdated** | Company | Published | UpdateCompanyUseCase | ShipmentOrder (SynchronizeCompanyUseCase) | ✓ Active |
| **CompanyDeleted** | Company | Published | DeleteCompanyByIdUseCase | - | ✓ Available |
| **ShipmentOrderCreated** | ShipmentOrder | Published | CreateShipmentOrderUseCase | Company (IncrementShipmentOrderUseCase) | ✓ Active |
| **ShipmentOrderRetrieved** | ShipmentOrder | Published | - | - | ✓ Available |

---

## Event Details Matrix

### CompanyCreated

```
┌─────────────────────────────────────────────────────┐
│ COMPANY CREATED                                     │
├─────────────────────────────────────────────────────┤
│ Context:     Company (Publisher)                    │
│ Trigger:     CreateCompanyUseCase.execute()         │
│ Location:    Company.createCompany() (factory)      │
│ Serializable: Yes (JSON)                            │
│ Critical:    Yes (triggers synchronization)         │
│                                                     │
│ PAYLOAD:                                            │
│ ├─ domainEventId: UUID                             │
│ ├─ companyId: UUID                                 │
│ ├─ company: String (name)                          │
│ ├─ types: Set<String>                              │
│ └─ occurredOn: Instant                             │
│                                                     │
│ PUBLICATION:                                        │
│ ├─ Exchange: integration.company                   │
│ ├─ RoutingKey: company.created                     │
│ ├─ Queue: integration.shipmentorder.company.       │
│ │          created                                  │
│ └─ Guarantee: At-least-once                        │
│                                                     │
│ CONSUMER:                                           │
│ ├─ Listener: CompanyCreatedListener                │
│ ├─ UseCase: SynchronizeCompanyUseCase              │
│ ├─ Action: Store company in shipmentorder.         │
│ │          companies table                         │
│ └─ idempotent: Yes                                 │
│                                                     │
│ METRICS:                                            │
│ ├─ Expected latency: < 5 seconds                   │
│ ├─ Failure rate: < 0.1%                            │
│ └─ Volume: Low (depends on company creation rate)  │
└─────────────────────────────────────────────────────┘
```

### CompanyUpdated

```
┌─────────────────────────────────────────────────────┐
│ COMPANY UPDATED                                     │
├─────────────────────────────────────────────────────┤
│ Context:     Company (Publisher)                    │
│ Trigger:     UpdateCompanyUseCase.execute()         │
│ Location:    Company.updateXxx() (various methods) │
│ Serializable: Yes (JSON)                            │
│ Critical:    Yes (triggers synchronization)         │
│                                                     │
│ PAYLOAD:                                            │
│ ├─ domainEventId: UUID                             │
│ ├─ companyId: UUID                                 │
│ ├─ property: String (field name)                   │
│ ├─ oldValue: String (previous value)               │
│ ├─ newValue: String (new value)                    │
│ └─ occurredOn: Instant                             │
│                                                     │
│ PUBLICATION:                                        │
│ ├─ Exchange: integration.company                   │
│ ├─ RoutingKey: company.updated                     │
│ ├─ Queue: integration.shipmentorder.company.       │
│ │          updated                                  │
│ └─ Guarantee: At-least-once                        │
│                                                     │
│ CONSUMER:                                           │
│ ├─ Listener: CompanyUpdatedListener                │
│ ├─ UseCase: SynchronizeCompanyUseCase              │
│ ├─ Action: Update specific field in               │
│ │          shipmentorder.companies table           │
│ └─ idempotent: Yes (applies same change)          │
│                                                     │
│ METRICS:                                            │
│ ├─ Expected latency: < 5 seconds                   │
│ ├─ Failure rate: < 0.1%                            │
│ └─ Volume: Medium (depends on update frequency)    │
└─────────────────────────────────────────────────────┘
```

### CompanyDeleted

```
┌─────────────────────────────────────────────────────┐
│ COMPANY DELETED                                     │
├─────────────────────────────────────────────────────┤
│ Context:     Company (Publisher)                    │
│ Trigger:     DeleteCompanyByIdUseCase.execute()     │
│ Location:    Company domain logic                   │
│ Serializable: Yes (JSON)                            │
│ Critical:    Medium (cleanup signal)                │
│                                                     │
│ PAYLOAD:                                            │
│ ├─ domainEventId: UUID                             │
│ ├─ companyId: UUID                                 │
│ ├─ company: String (name)                          │
│ └─ occurredOn: Instant                             │
│                                                     │
│ PUBLICATION:                                        │
│ ├─ Exchange: integration.company                   │
│ ├─ RoutingKey: company.deleted                     │
│ ├─ Queue: integration.shipmentorder.company.       │
│ │          deleted                                  │
│ └─ Guarantee: At-least-once                        │
│                                                     │
│ CONSUMER:                                           │
│ ├─ Listener: (Not implemented yet)                 │
│ ├─ UseCase: (Available for future)                 │
│ ├─ Potential Action: Archive or delete from       │
│ │                   shipmentorder.companies        │
│ └─ idempotent: Yes                                 │
│                                                     │
│ METRICS:                                            │
│ ├─ Expected latency: < 5 seconds                   │
│ ├─ Failure rate: < 0.1%                            │
│ └─ Volume: Very low (depends on deletion rate)     │
└─────────────────────────────────────────────────────┘
```

### ShipmentOrderCreated

```
┌─────────────────────────────────────────────────────┐
│ SHIPMENT ORDER CREATED                              │
├─────────────────────────────────────────────────────┤
│ Context:     ShipmentOrder (Publisher)              │
│ Trigger:     CreateShipmentOrderUseCase.execute()   │
│ Location:    ShipmentOrder.createOrder() (factory) │
│ Serializable: Yes (JSON)                            │
│ Critical:    Yes (metrics tracking)                 │
│                                                     │
│ PAYLOAD:                                            │
│ ├─ domainEventId: UUID                             │
│ ├─ shipmentOrderId: UUID                           │
│ ├─ companyId: UUID                                 │
│ ├─ shipperId: UUID                                 │
│ ├─ externalId: String                              │
│ └─ occurredOn: Instant                             │
│                                                     │
│ PUBLICATION:                                        │
│ ├─ Exchange: integration.shipmentorder              │
│ ├─ RoutingKey: shipmentorder.created               │
│ ├─ Queue: integration.company.shipmentorder.       │
│ │          created                                  │
│ └─ Guarantee: At-least-once                        │
│                                                     │
│ CONSUMER:                                           │
│ ├─ Listener: IncrementShipmentOrderListener        │
│ ├─ UseCase: IncrementShipmentOrderUseCase          │
│ ├─ Action: Increment order counter in             │
│ │          company.companies metadata              │
│ └─ idempotent: Yes (same increment = same result) │
│                                                     │
│ METRICS:                                            │
│ ├─ Expected latency: < 5 seconds                   │
│ ├─ Failure rate: < 0.1%                            │
│ └─ Volume: High (depends on order creation rate)   │
└─────────────────────────────────────────────────────┘
```

### ShipmentOrderRetrieved

```
┌─────────────────────────────────────────────────────┐
│ SHIPMENT ORDER RETRIEVED                            │
├─────────────────────────────────────────────────────┤
│ Context:     ShipmentOrder (Publisher)              │
│ Trigger:     (Not currently triggered)              │
│ Location:    (Available for future use)             │
│ Serializable: Yes (JSON)                            │
│ Critical:    Low (informational)                    │
│                                                     │
│ PAYLOAD:                                            │
│ ├─ domainEventId: UUID                             │
│ ├─ orderId: UUID                                   │
│ ├─ externalId: String                              │
│ └─ occurredOn: Instant                             │
│                                                     │
│ PUBLICATION:                                        │
│ ├─ Exchange: integration.shipmentorder              │
│ ├─ RoutingKey: shipmentorder.retrieved             │
│ ├─ Queue: (Not configured yet)                     │
│ └─ Guarantee: At-least-once                        │
│                                                     │
│ POTENTIAL CONSUMERS:                                │
│ ├─ Notification system (alert user)                │
│ ├─ Analytics (track order status)                  │
│ ├─ Reporting (dashboards)                          │
│ └─ Audit trail                                      │
│                                                     │
│ WHEN TO USE:                                        │
│ ├─ Order synced with external system               │
│ ├─ Status check completed                          │
│ └─ Webhook callback from logistics provider        │
└─────────────────────────────────────────────────────┘
```

---

## Event Flow Choreography

### Scenario 1: New Company Registration and Order Creation

```
STEP 1: Create Company
┌─────────────────────────────────────────────────────┐
│ User POST /companies (with name, cnpj, types)      │
│         ↓                                            │
│ CreateCompanyUseCase.execute()                      │
│         ├─ Validate CNPJ unique                     │
│         ├─ Company.createCompany() [factory]        │
│         └─ placeDomainEvent(CompanyCreated)         │
│         ↓                                            │
│ CompanyRepository.create()                          │
│         ├─ INSERT company.companies                 │
│         ├─ INSERT company.outbox                    │
│         └─ COMMIT ✓                                 │
│         ↓                                            │
│ Return: CompanyId                                   │
└─────────────────────────────────────────────────────┘

STEP 2: Background Processor
┌─────────────────────────────────────────────────────┐
│ EventOutboxProcessor (every 5 seconds)              │
│         ↓                                            │
│ SELECT company.outbox WHERE published = false       │
│         ↓                                            │
│ Deserialize CompanyCreated                          │
│         ↓                                            │
│ rabbitTemplate.convertAndSend(                      │
│   exchange: "integration.company",                  │
│   routingKey: "company.created",                    │
│   message: CompanyCreated                           │
│ )                                                    │
│         ↓                                            │
│ UPDATE company.outbox SET published = true          │
└─────────────────────────────────────────────────────┘

STEP 3: ShipmentOrder Consumes
┌─────────────────────────────────────────────────────┐
│ @RabbitListener                                     │
│ Queue: integration.shipmentorder.company.created    │
│         ↓                                            │
│ CompanyCreatedListener.handle()                     │
│         ├─ Deserialize CompanyCreatedDTO            │
│         └─ mapper.map(event, Map.class)             │
│         ↓                                            │
│ SynchronizeCompanyUseCase.execute()                 │
│         ├─ Create CompanyData(companyId, data)      │
│         └─ Save to shipmentorder.companies          │
│         ↓                                            │
│ Message acknowledged ✓                              │
└─────────────────────────────────────────────────────┘

STEP 4: Create Order
┌─────────────────────────────────────────────────────┐
│ User POST /orders (with companyId, shipperId, ...)  │
│         ↓                                            │
│ CreateShipmentOrderUseCase.execute()                │
│         ├─ Validate company exists                  │
│         │  └─ Query shipmentorder.companies ✓       │
│         ├─ ShipmentOrder.createOrder() [factory]    │
│         └─ placeDomainEvent(ShipmentOrderCreated)   │
│         ↓                                            │
│ ShipmentOrderRepository.create()                    │
│         ├─ INSERT shipmentorder.shipment_orders     │
│         ├─ INSERT shipmentorder.outbox              │
│         └─ COMMIT ✓                                 │
│         ↓                                            │
│ Return: ShipmentOrderId                             │
└─────────────────────────────────────────────────────┘

STEP 5: Background Processor
┌─────────────────────────────────────────────────────┐
│ EventOutboxProcessor (every 5 seconds)              │
│         ↓                                            │
│ SELECT shipmentorder.outbox WHERE published=false   │
│         ↓                                            │
│ Deserialize ShipmentOrderCreated                    │
│         ↓                                            │
│ rabbitTemplate.convertAndSend(                      │
│   exchange: "integration.shipmentorder",            │
│   routingKey: "shipmentorder.created",              │
│   message: ShipmentOrderCreated                     │
│ )                                                    │
│         ↓                                            │
│ UPDATE shipmentorder.outbox SET published = true    │
└─────────────────────────────────────────────────────┘

STEP 6: Company Consumes
┌─────────────────────────────────────────────────────┐
│ @RabbitListener                                     │
│ Queue: integration.company.shipmentorder.created    │
│         ↓                                            │
│ IncrementShipmentOrderListener.handle()             │
│         └─ Deserialize ShipmentOrderCreatedDTO      │
│         ↓                                            │
│ IncrementShipmentOrderUseCase.execute()             │
│         ├─ Extract companyId                        │
│         ├─ Load Company aggregate                   │
│         ├─ Increment orderCount in metadata         │
│         └─ Update company.companies                 │
│         ↓                                            │
│ Message acknowledged ✓                              │
└─────────────────────────────────────────────────────┘

RESULT:
  ✓ Company created in company.companies
  ✓ Company synchronized to shipmentorder.companies
  ✓ Order created in shipmentorder.shipment_orders
  ✓ Order counter incremented in company.companies
  ✓ Full traceability via domain events
```

### Scenario 2: Update Company Information

```
User updates company name:
  ↓
UpdateCompanyUseCase
  ├─ Load Company aggregate
  ├─ company.updateName("New Name")  [returns new instance]
  └─ placeDomainEvent(CompanyUpdated)
  ↓
CompanyRepository.update()
  ├─ UPDATE company.companies
  ├─ INSERT company.outbox
  └─ COMMIT
  ↓
Background processor publishes event
  ↓
ShipmentOrder CompanyUpdatedListener
  ├─ SynchronizeCompanyUseCase
  ├─ Update "name" in shipmentorder.companies
  └─ Complete
  ↓
RESULT: Both contexts have updated name
```

---

## Event Statistics & Monitoring

### Expected Metrics

```
CompanyCreated
├─ Frequency: 1-10 per day (depends on business)
├─ Size: ~200 bytes JSON
├─ Latency (outbox→queue): 0-5 seconds
├─ Latency (queue→processed): 0-3 seconds
└─ Success rate: > 99.9%

CompanyUpdated
├─ Frequency: 10-50 per day
├─ Size: ~300 bytes JSON
├─ Latency (outbox→queue): 0-5 seconds
├─ Latency (queue→processed): 0-3 seconds
└─ Success rate: > 99.9%

ShipmentOrderCreated
├─ Frequency: 100-1000 per day
├─ Size: ~250 bytes JSON
├─ Latency (outbox→queue): 0-5 seconds
├─ Latency (queue→processed): 0-3 seconds
└─ Success rate: > 99.9%
```

### Health Checks

```
✓ Company outbox queue depth: < 10 items
✓ ShipmentOrder outbox queue depth: < 10 items
✓ RabbitMQ queue depths: Normal (< 100)
✓ Event processing latency: < 1 second p99
✓ Dead letter queue depth: 0
```

---

## Future Events (For Enhancement)

Potential events that could be added:

```
1. CompanyDeactivated
   - Triggered when company temporarily disabled
   - Consumer: ShipmentOrder (prevent new orders)

2. OrderStatusChanged
   - Triggered at each order status update
   - Consumer: Company (tracking), Notifications

3. CapacityUpdated
   - Triggered when company capacity changes
   - Consumer: ShipmentOrder (validation)

4. AgreementCreated / AgreementTerminated
   - For agreement lifecycle events
   - Consumer: Company context only (internal)
```

---

## Debugging Guide

### Event Not Delivered

```
1. Check Source Context Outbox
   SELECT * FROM company.outbox WHERE published = false
   └─ If records exist: Background processor not running
   └─ If empty: Event was published

2. Check RabbitMQ
   └─ Check queue depth: integration.shipmentorder.company.created
   └─ Check dead letter queue

3. Check Consumer Logs
   └─ Look for exceptions in listener
   └─ Check if listener is subscribed

4. Check Event Format
   └─ Verify event serialization
   └─ Verify DTO mapping
```

### Listener Processing Error

```
1. Check Error Logs
   └─ Find exception and stacktrace
   
2. Identify Issue
   └─ Validation error?
   └─ Null pointer exception?
   └─ Database error?

3. Fix & Reprocess
   └─ Fix code
   └─ Manually replay event from DLQ
   └─ Monitor next execution

4. Prevent Recurrence
   └─ Add validation
   └─ Add defensive checks
   └─ Add monitoring alert
```

### Synchronization Lag

```
If ShipmentOrder can't create order due to company not found:

1. Wait and Retry
   └─ Company data may not be synchronized yet
   └─ Retry after 2-3 seconds

2. Check Background Processor
   └─ Verify EventOutboxProcessor is running
   └─ Check for configuration issues

3. Monitor Event Flow
   └─ Use timestamp to track end-to-end latency
   └─ Set up alerting for > 10 second lag
```

---

## Event Best Practices

✅ **DO**
- Include all data needed in event payload
- Use immutable event objects
- Always include aggregateId
- Use past tense event names
- Handle idempotency in listeners
- Version events if schema changes

❌ **DON'T**
- Send references to other aggregates (include data)
- Change event structure (publish new version instead)
- Use synchronous RPC across contexts
- Store sensitive data in events
- Assume order across different aggregates
- Ignore processing failures
