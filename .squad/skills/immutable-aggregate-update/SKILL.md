# Immutable Aggregate Update Pattern

## ⚡ TL;DR

- **Rule:** Domain objects NEVER mutate in place
- **Pattern:** Update methods return NEW instances with changed fields
- **Event:** Place domain event in update method via `placeDomainEvent()`
- **Confidence:** High (applied consistently across Company, ShipmentOrder)

---

## Pattern

### Update Method Template

Real implementation from `Company.java`:

```java
public Company updateName(final String name) {
    validateCanUpdate();  // Business rule validation
    if (this.name.equals(name))
        return this;  // No-op if unchanged
    
    final Company updated = new Company(
        this.companyId,      // Same ID
        name,                // New value
        this.cnpj,           // Copy all other fields
        this.companyTypes,
        this.configurations,
        this.agreements,
        this.status,
        this.getDomainEvents(),        // CRITICAL: Copy existing events
        this.getPersistentMetadata()   // CRITICAL: Copy metadata
    );
    
    updated.placeDomainEvent(new CompanyUpdated(
        updated.companyId.value(), 
        "name", 
        this.name,  // old value
        name        // new value
    ));
    return updated;
}
```

### Key Elements

1. **No-op check:** Return `this` if value unchanged
2. **New instance:** Call private constructor with all fields
3. **Copy events and metadata:** MUST preserve from original instance
4. **Place event:** After construction, before return
5. **Return new:** Caller receives updated instance

---

## Why Immutability

| Benefit | Explanation |
|---------|-------------|
| **Thread-safety** | No race conditions on shared state |
| **Predictable state** | No hidden mutations - explicit changes only |
| **Event sourcing compatible** | State changes are explicit events |
| **Testing clarity** | Before/after comparison is simple |
| **Debugging** | State transitions traceable through events |

---

## Common Mistakes

### ❌ WRONG #1: Mutation (setter pattern)

```java
public void setName(String name) {
    this.name = name;  // DON'T MUTATE
    placeDomainEvent(...);
}
```

**Why wrong:** Violates immutability, allows hidden state changes.

### ❌ WRONG #2: Forgot to copy events

```java
Company updated = new Company(
    this.companyId, 
    name, 
    this.cnpj,
    this.companyTypes,
    this.configurations,
    this.agreements,
    this.status,
    new HashSet<>(),  // ❌ Lost existing domain events!
    new HashMap<>()   // ❌ Lost metadata!
);
```

**Why wrong:** Existing events are lost. If another update already placed an event, it disappears.

### ❌ WRONG #3: Placing event in use case

```java
// In use case
Company company = repository.findById(input.id()).orElseThrow();
company = company.updateName(input.name());
company.placeDomainEvent(new CompanyUpdated(...));  // ❌ WRONG
```

**Why wrong:** Event placement belongs in the aggregate method, not the use case.

### ✅ CORRECT: Full pattern

```java
public Company updateCnpj(final String cnpj) {
    validateCanUpdate();
    if (this.cnpj.value().equals(cnpj))
        return this;
    
    final Company updated = new Company(
        this.companyId,
        this.name,
        new Cnpj(cnpj),  // New value
        this.companyTypes,
        this.configurations,
        this.agreements,
        this.status,
        this.getDomainEvents(),        // ✅ Copy events
        this.getPersistentMetadata()   // ✅ Copy metadata
    );
    
    updated.placeDomainEvent(new CompanyUpdated(
        updated.companyId.value(), 
        "cnpj", 
        this.cnpj.value(), 
        cnpj
    ));
    return updated;
}
```

---

## Repository Usage

```java
@Override
public Output execute(final Input input) {
    Company company = companyRepository.getCompanyById(new CompanyId(input.companyId()))
            .orElseThrow(() -> new NotFoundException("Company not found"));
    
    // Update returns NEW instance
    company = company.updateName(input.name());
    
    // Save new instance (repository handles event outbox)
    company = companyRepository.update(company);
    
    return new Output(company.getCompanyId().value(), company.getName());
}
```

**Critical:** Reassign variable with returned instance. Pattern: `company = company.updateX(...)`.

---

## Multiple Updates in Sequence

```java
Company company = repository.findById(id).orElseThrow();

// Chain updates - each returns new instance
company = company.updateName(newName);
company = company.updateTypes(newTypes);
company = company.updateConfigurations(newConfig);

// Single save at end - repository sees all events
company = repository.update(company);
```

Each update:
1. Creates new instance
2. Copies events from previous instance
3. Adds its own event

Final instance has ALL events from the chain.

---

## Metadata

**Confidence:** `high`  
**Applies To:** `[company, shipmentorder, all aggregates]`  
**Replaces:** `[ARCHITECTURE.md § Pattern 3, examples/complete-aggregate.md duplication]`  
**Token Cost:** `~120 lines`  
**Related Skills:** `[]`  
**Created:** `2025-02-24`  
**Last Updated:** `2025-02-24`
