# Company Status Feature

**Status:** ✅ Implemented  
**Last Updated:** 2025-11-25  
**Related Files:** `Status.java`, `Company.java`, `SynchronizeCompanyUseCase.java`

---

## 🎯 Overview

The Company Status feature provides lifecycle management for companies using a **soft delete pattern** with three distinct states:

- **ACTIVE (A)** - Default state, accepts updates
- **SUSPENDED (S)** - Company restricted, no updates allowed
- **DELETED (D)** - Soft delete, no updates allowed, data preserved

---

## 📊 Status Lifecycle

```
CREATE                    SUSPEND                    DELETE
   │                         │                          │
   ▼                         ▼                          ▼
┌────────────┐           ┌──────────────┐           ┌────────────┐
│  ACTIVE    │──────────▶│  SUSPENDED   │──────────▶│  DELETED   │
│   (A)      │           │    (S)       │           │    (D)     │
└────────────┘           └──────────────┘           └────────────┘
   △                                                       │
   │                                                       │
   └───────────────── (can activate in future) ───────────┘

Transitions:
├─ ACTIVE → SUSPENDED: Via SuspendCompanyUseCase
├─ ACTIVE → DELETED: Via DeleteCompanyUseCase
├─ SUSPENDED → DELETED: Via DeleteCompanyUseCase
├─ SUSPENDED → SUSPENDED: Idempotent (no change)
└─ DELETED → DELETED: Idempotent (no change)
```

---

## 🏗️ Architecture

### Value Object: Status

**Location:** `commons/domain/Status.java`

Immutable value object shared across all modules:

```java
public record Status(char value) {
    public Status {
        if (value != 'A' && value != 'S' && value != 'D') {
            throw new ValidationException("Invalid status");
        }
    }

    // Factory methods
    public static Status active() { return new Status('A'); }
    public static Status suspended() { return new Status('S'); }
    public static Status deleted() { return new Status('D'); }
    public static Status of(char value) { return new Status(value); }
    public static Status of(String value) { return new Status(value.charAt(0)); }

    // Query methods
    public boolean isActive() { return value == 'A'; }
    public boolean isSuspended() { return value == 'S'; }
    public boolean isDeleted() { return value == 'D'; }
    public boolean isInactive() { return value == 'S' || value == 'D'; }
}
```

**Key Points:**
- ✅ Immutable record
- ✅ Validation in compact constructor
- ✅ Reusable across all entities
- ✅ Single source of truth for status logic

---

## 🔄 Company Aggregate Integration

### New Methods in Company

#### 1. Suspend Company

```java
public Company suspend() {
    // Idempotent: returns self if already suspended/deleted
    if (this.status.isSuspended() || this.status.isDeleted()) {
        return this;
    }
    return this.updateStatus(Status.suspended());
}
```

#### 2. Delete Company (Soft Delete)

```java
public Company delete() {
    // Idempotent: returns self if already suspended/deleted
    if (this.status.isSuspended() || this.status.isDeleted()) {
        return this;
    }
    return this.updateStatus(Status.deleted());
}
```

#### 3. Update Status

```java
public Company updateStatus(final Status newStatus) {
    if (this.status.equals(newStatus)) {
        return this;
    }
    
    final Company updated = new Company(
        this.companyId,
        this.name,
        this.cnpj,
        this.companyTypes,
        this.configurations,
        this.agreements,
        newStatus,  // Updated status
        this.getDomainEvents(),
        this.getPersistentMetadata()
    );
    
    // Publish event for synchronization
    updated.placeDomainEvent(new CompanyUpdated(
        updated.companyId.value(), 
        "status", 
        String.valueOf(this.status.value()), 
        String.valueOf(newStatus.value())
    ));
    return updated;
}
```

#### 4. Validation for Updates

```java
private void validateCanUpdate() {
    if (!this.status.isActive()) {
        throw new ValidationException(
            String.format("Cannot update company in %s status", 
                this.status.isDeleted() ? "DELETED" : "SUSPENDED"
            )
        );
    }
}
```

All update methods call `validateCanUpdate()` to prevent updates to inactive companies.

---

## 🎯 Use Cases

### 1. Suspend Company

```java
@DomainService
@Cqrs(DatabaseRole.WRITE)
public class SuspendCompanyUseCase 
    implements UseCase<SuspendCompanyUseCase.Input, SuspendCompanyUseCase.Output> {
    
    private final CompanyRepository companyRepository;

    public Output execute(Input input) {
        final Company company = companyRepository
            .getCompanyById(CompanyId.with(input.companyId()))
            .orElseThrow(() -> new CompanyNotFoundException(...));

        final Company suspended = company.suspend();
        companyRepository.update(suspended);

        return new Output(true);
    }

    public record Input(UUID companyId) {}
    public record Output(boolean suspended) {}
}
```

### 2. Delete Company (Soft Delete)

```java
@DomainService
@Cqrs(DatabaseRole.WRITE)
public class DeleteCompanyUseCase 
    implements UseCase<DeleteCompanyUseCase.Input, DeleteCompanyUseCase.Output> {
    
    private final CompanyRepository companyRepository;

    public Output execute(Input input) {
        final Company company = companyRepository
            .getCompanyById(CompanyId.with(input.companyId()))
            .orElseThrow(() -> new CompanyNotFoundException(...));

        final Company deleted = company.delete();
        companyRepository.update(deleted);

        return new Output(true);
    }

    public record Input(UUID companyId) {}
    public record Output(boolean deleted) {}
}
```

### 3. Update Company Status (ShipmentOrder Module)

For event listeners to synchronize status changes:

```java
@DomainService
@Cqrs(DatabaseRole.WRITE)
public class UpdateCompanyStatusUseCase 
    implements VoidUseCase<UpdateCompanyStatusUseCase.Input> {
    
    private final CompanyRepository companyRepository;

    public void execute(Input input) {
        companyRepository.findById(CompanyId.with(input.companyId()))
            .ifPresent(existing -> {
                final Company updated = existing.updateStatus(Status.of(input.status()));
                companyRepository.save(updated);
            });
    }

    public record Input(UUID companyId, char status) {}
}
```

---

## 📡 Event-Driven Synchronization

### Status Change Events

All status changes emit `CompanyUpdated` event:

```
CompanyUpdated {
  companyId: UUID,
  property: "status",        // ← Identifies status change
  oldValue: "A"/"S"/"D",      // ← Previous status
  newValue: "A"/"S"/"D"       // ← New status
}
```

### Synchronization Rules

When processing events in `SynchronizeCompanyUseCase`:

**Rule 1: Status updates have priority** over data updates
**Rule 2: Inactive companies (S/D) don't accept data updates**  
**Rule 3: Only ACTIVE companies can be modified**

```java
if (input.getStatus().isPresent()) {
    // Status update has priority
    updated = existing.updateStatus(input.getStatus().get());
} else if (existing.getStatus().isActive()) {
    // Only ACTIVE companies accept data updates
    updated = existing.updateData(input.getTypesKey());
} else {
    // SUSPENDED/DELETED: no changes
    updated = existing;
}
```

---

## 🛡️ Validation Rules

### Company Update Protection

```
updateName()              ✓ ACTIVE   ✗ SUSPENDED   ✗ DELETED
updateCnpj()              ✓ ACTIVE   ✗ SUSPENDED   ✗ DELETED
updateTypes()             ✓ ACTIVE   ✗ SUSPENDED   ✗ DELETED
updateConfigurations()    ✓ ACTIVE   ✗ SUSPENDED   ✗ DELETED
```

### ShipmentOrder Creation Validation

```java
if (company.getStatus().isInactive()) {
    throw new ValidationException(
        "Cannot create shipment order for an inactive company"
    );
}
```

---

## 💾 Database Schema

### Migration: V9__add_status_to_company.sql

```sql
ALTER TABLE company.company ADD COLUMN status CHAR(1) DEFAULT 'A' NOT NULL;
ALTER TABLE shipmentorder.company ADD COLUMN status CHAR(1) DEFAULT 'A' NOT NULL;
```

---

## 🧪 Testing

### Status Tests (CompanyStatusTest.java)

8 tests covering:
- Status creation (active, suspended, deleted)
- Status conversion (from char, from string)
- Status validation
- Status query methods

### Synchronization Tests (SynchronizeCompanyUseCaseTest.java)

18 tests covering:
- Status synchronization (DELETED, SUSPENDED, ACTIVE)
- Priority handling (status > data)
- Protection rules (suspended/deleted companies)
- Status transitions
- Data preservation during status changes

### Integration Tests (CompanyShipmentOrderIT.java)

Complete flow: Create → Update Types → Delete → Verify Sync

---

## 🔧 Test Assertion Helpers

### Company Module

```java
assertThatCompany(company)
    .hasStatus('A')      // Exact value
    .isActive()          // Query method
    .isSuspended()
    .isDeleted();
```

### ShipmentOrder Module

```java
assertThatCompany(domainCompany)
    .isActive()
    .isSuspended()
    .isDeleted()
    .isInactive();

assertThatShipmentOrderCompany(entity)
    .hasStatus('D')
    .isDeleted();
```

---

## 📋 REST API

### Delete Company (Soft Delete)

```
DELETE /companies/{companyId}

Response (204 No Content):
(empty)

Note: Uses soft delete - company marked as DELETED, data preserved
```

### Suspend Company (Future Endpoint)

```
PATCH /companies/{companyId}/suspend

Response (200 OK):
{
  "suspended": true
}
```

---

## 🎓 Key Concepts

### Soft Delete vs Hard Delete

```
Soft Delete (Implemented):
├─ Status → DELETED
├─ Data preserved
├─ Queries must filter by status
└─ Reversible in future

Hard Delete (Not Used):
├─ Data removed
├─ Can't query historically
├─ Irreversible
└─ Breaks referential integrity
```

### Immutability with Status

```java
// ✅ Returns NEW instance
Company updated = company.suspend();

// ✅ Status preserved in updates
Company renamed = company.updateName("new name");
assert renamed.getStatus() == company.getStatus();

// ❌ Never mutate
company.status = Status.deleted();  // ✗ WRONG
```

### Event-Driven Sync

```
Company                ShipmentOrder
─────────────────────────────────────
suspend()
    ├─ updateStatus(S)
    │
    ├─ placeDomainEvent(
    │     CompanyUpdated
    │     property="status"
    │     newValue='S'
    │   )
    │
    └─ companyRepository.update()
            │
            ├─ Outbox.save()
            │
            └─ RabbitMQ publishes
                    │
                    ├─ SynchronizeCompanyUseCase
                    │  (listens to CompanyUpdated)
                    │
                    ├─ UpdateCompanyStatusUseCase
                    │  (updates status = 'S')
                    │
                    └─ ShipmentOrderCompanyEntity
                       (status column = 'S')
```

---

## 🔗 Cross-Module Consistency

Companies have two representations:

| Module | Table | Status Column | Purpose |
|--------|-------|---------------|---------|
| Company | `company.company` | ✓ | Source of truth |
| ShipmentOrder | `shipmentorder.company` | ✓ | Local copy for validation |

**Synchronization ensures:**
- ✅ Status in ShipmentOrder matches Company module
- ✅ New shipment orders can't be created for inactive companies
- ✅ Eventual consistency across modules

---

**End of Document**
