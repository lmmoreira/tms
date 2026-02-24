# Complete Aggregate Example

This example shows a complete aggregate implementation following TMS patterns.

## Company Aggregate

**Location:** `company/domain/Company.java`

```java
package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.AbstractAggregateRoot;
// ... (other imports: AbstractDomainEvent, Id, ValidationException, events)

/**
 * Company aggregate root representing a logistics company.
 * IMMUTABLE: All update methods return new instances.
 */
public class Company extends AbstractAggregateRoot {

    private final CompanyId companyId;
    private final String name;
    // ... (remaining fields: cnpj, types, configuration, agreements)

    // ========== PRIVATE CONSTRUCTOR ==========
    private Company(CompanyId companyId, String name, Cnpj cnpj,
                    Set<CompanyType> types, Map<String, Object> configuration,
                    Set<Agreement> agreements, Set<AbstractDomainEvent> domainEvents,
                    Map<String, Object> persistentMetadata) {
        super(new HashSet<>(domainEvents), new HashMap<>(persistentMetadata));
        
        // Validation: companyId, name, cnpj null checks; types not empty
        if (companyId == null) throw new ValidationException("CompanyId cannot be null");
        if (name == null || name.isBlank()) throw new ValidationException("Company name cannot be null or blank");
        // ... (cnpj, types validation)
        
        this.companyId = companyId;
        this.name = name;
        // ... (remaining field assignments with defensive copies)
    }

    // ========== FACTORY METHOD (CREATE) ==========
    public static Company createCompany(String name, String cnpj, Set<CompanyType> types,
                                       Map<String, Object> configuration) {
        Company company = new Company(CompanyId.unique(), name, new Cnpj(cnpj), types,
                                      configuration, new HashSet<>(), new HashSet<>(), new HashMap<>());
        company.placeDomainEvent(new CompanyCreated(company.getCompanyId().value(),
                                                     company.getName(), company.getCnpj().value()));
        return company;
    }

    // ========== RECONSTRUCTION (FROM DATABASE) ==========
    public static Company reconstruct(CompanyId companyId, String name, Cnpj cnpj,
                                     Set<CompanyType> types, Map<String, Object> configuration,
                                     Set<Agreement> agreements, Map<String, Object> persistentMetadata) {
        return new Company(companyId, name, cnpj, types, configuration, agreements,
                          new HashSet<>(), persistentMetadata);
    }

    // ========== UPDATE METHODS (IMMUTABLE) ==========
    public Company updateName(String newName) {
        if (this.name.equals(newName)) return this;
        
        Company updated = new Company(this.companyId, newName, this.cnpj, this.types,
                                     this.configuration, this.agreements, this.getDomainEvents(),
                                     this.getPersistentMetadata());
        updated.placeDomainEvent(new CompanyUpdated(updated.getCompanyId().value(),
                                                     "name", this.name, newName));
        return updated;
    }

    public Company addConfiguration(String key, Object value) {
        if (this.configuration.containsKey(key)) {
            throw new ValidationException("Configuration key already exists: " + key);
        }
        Map<String, Object> newConfig = new HashMap<>(this.configuration);
        newConfig.put(key, value);
        
        Company updated = new Company(this.companyId, this.name, this.cnpj, this.types,
                                     newConfig, this.agreements, this.getDomainEvents(),
                                     this.getPersistentMetadata());
        updated.placeDomainEvent(new ConfigurationAdded(updated.getCompanyId().value(), key, value));
        return updated;
    }

    public Company addAgreement(Agreement agreement) {
        if (this.agreements.contains(agreement)) return this;
        Set<Agreement> newAgreements = new HashSet<>(this.agreements);
        newAgreements.add(agreement);
        return new Company(this.companyId, this.name, this.cnpj, this.types,
                          this.configuration, newAgreements, this.getDomainEvents(),
                          this.getPersistentMetadata());
    }

    // ========== BUSINESS LOGIC ==========
    public boolean hasType(CompanyType type) { return this.types.contains(type); }
    public boolean canCreateShipmentOrders() {
        return hasType(CompanyType.MARKETPLACE) || hasType(CompanyType.SHIPPER);
    }

    // ========== GETTERS ONLY ==========
    public CompanyId getCompanyId() { return companyId; }
    public String getName() { return name; }
    public Cnpj getCnpj() { return cnpj; }
    public Set<CompanyType> getTypes() { return types; }
    public Map<String, Object> getConfiguration() { return configuration; }
    public Set<Agreement> getAgreements() { return agreements; }

    @Override
    public String toString() {
        return "Company{companyId=" + companyId.value() + ", name='" + name + "', cnpj=" + cnpj.value() + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Company company)) return false;
        return Objects.equals(companyId, company.companyId);
    }

    @Override
    public int hashCode() { return Objects.hash(companyId); }
}
```

**Full pattern details:** `.squad/skills/immutable-aggregate-update/SKILL.md`

## Key Points

✅ **Immutability:** All fields are `final`, update methods return new instances
✅ **Private Constructor:** Only factory methods can create instances
✅ **Factory Method:** `createCompany()` places domain event
✅ **Reconstruction:** Separate method for loading from database
✅ **Domain Events:** Placed in aggregate methods, not use cases
✅ **Validation:** In constructor and business methods
✅ **Getters Only:** No setters
✅ **Business Logic:** Methods like `canCreateShipmentOrders()`
