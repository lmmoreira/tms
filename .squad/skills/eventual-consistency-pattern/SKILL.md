# Eventual Consistency Pattern

## ⚡ TL;DR

- **When:** Module needs to validate references to another module's data (e.g., ShipmentOrder validates Company exists)
- **Why:** Maintain module autonomy, avoid direct repository calls between modules
- **Steps:** Local table (JSONB) → simplified aggregate → sync use case → listeners (Created/Updated)
- **Confidence:** High (proven in shipmentorder → company sync)

---

## Pattern Overview (5 Steps)

### Step 1: Create Local Table with JSONB

```sql
CREATE TABLE {target_schema}.{entity} (
    {entity}_id UUID PRIMARY KEY,
    data JSONB NOT NULL
);
```

Example: `CREATE TABLE shipmentorder.company (company_id UUID PRIMARY KEY, data JSONB NOT NULL);`

---

### Step 2: Create Simplified Aggregate

```java
public class Company extends AbstractAggregateRoot {
    private final CompanyId companyId;
    private final CompanyData data;

    public static Company createCompany(final UUID id, final Map<String, Object> data) {
        return new Company(CompanyId.with(id), CompanyData.with(data), Status.active());
    }

    public Company updateData(final Map<String, Object> newData) {
        final Map<String, Object> merged = new HashMap<>(this.data.value());
        merged.putAll(newData);
        return new Company(this.companyId, CompanyData.with(merged), this.status);
    }
}
```

---

### Step 3: Create Synchronize Use Case

```java
@DomainService
@Cqrs(DatabaseRole.WRITE)
public class SynchronizeCompanyUseCase implements VoidUseCase<Input> {
    private final CompanyRepository companyRepository;

    @Override
    public void execute(final Input input) {
        final Optional<Company> existing = companyRepository.findById(CompanyId.with(input.companyId()));
        
        if (existing.isEmpty()) {
            companyRepository.save(Company.createCompany(input.companyId(), input.data()));
        } else {
            companyRepository.save(existing.get().updateData(input.data()));
        }
    }

    public record Input(UUID companyId, Map<String, Object> data) {}
}
```

---

### Step 4: Create Event Listeners

```java
@Component
@Cqrs(DatabaseRole.WRITE)
@Lazy(false)
public class CompanyCreatedListener {
    private final VoidUseCaseExecutor executor;
    private final SynchronizeCompanyUseCase useCase;
    private final Mapper mapper;

    @RabbitListener(queues = "integration.{target}.{entity}.created")
    public void handle(final CompanyCreatedDTO event, final Message msg, final Channel ch) {
        executor.from(useCase)
            .withInput(new Input(event.companyId(), mapper.map(event, Map.class)))
            .execute();
    }
}
```

**Updated listener:** Same pattern, queue = `integration.{target}.{entity}.updated`

**⚠️ JSON:** Use `JsonSingleton` (not ObjectMapper injection) — see `json-singleton-usage` skill.

---

### Step 5: Add Validation

```java
@Override
public Output execute(final Input input) {
    // Validate against local copy
    final Company company = companyRepository.findById(new CompanyId(input.companyId()))
        .orElseThrow(() -> new NotFoundException("Company not found"));

    // Proceed with domain logic
    final ShipmentOrder order = ShipmentOrder.createOrder(company.getCompanyId(), input.orderNumber());
    return new Output(order.getShipmentOrderId().value());
}
```

---

## When to Use

✅ Module needs to validate another module's entity exists  
✅ Eventual consistency acceptable (not transactional)  
✅ Modules must stay decoupled  
❌ Don't use if real-time strong consistency required

---

## Anti-Pattern: Cross-Module Repository Calls

❌ **WRONG:** `br.com.logistics.tms.company.application.repositories.CompanyRepository` (calling other module's repo)

✅ **CORRECT:** `br.com.logistics.tms.shipmentorder.application.repositories.CompanyRepository` (local copy)

---

## Architecture Flow

```
Company Module                     ShipmentOrder Module
──────────────                     ────────────────────
Company created/updated         →  CompanyCreatedListener
  └─> Event to outbox           →    └─> SynchronizeCompanyUseCase
  └─> RabbitMQ publish          →    └─> Local company table updated
                                 →  CreateShipmentOrderUseCase validates ✅
```

---

## Metadata

**Confidence:** `high`  
**Applies To:** `[shipmentorder, future modules needing cross-module validation]`  
**Replaces:** `[prompts/eventual-consistency.md (740 lines)]`  
**Token Cost:** `~150 lines (80% reduction from original 740-line guide)`  
**Related Skills:** `[json-singleton-usage]`  
**Real Implementation:** `shipmentorder → company sync (V8 migration, CompanyCreatedListener, SynchronizeCompanyUseCase)`  
**Created:** `2025-02-24`  
**Last Updated:** `2025-02-24`
