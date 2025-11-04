# Complete Aggregate Example

This example shows a complete aggregate implementation following TMS patterns.

## Company Aggregate

**Location:** `company/domain/Company.java`

```java
package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.AbstractAggregateRoot;
import br.com.logistics.tms.commons.domain.AbstractDomainEvent;
import br.com.logistics.tms.commons.domain.Id;
import br.com.logistics.tms.commons.exception.ValidationException;
import br.com.logistics.tms.company.domain.events.CompanyCreated;
import br.com.logistics.tms.company.domain.events.CompanyUpdated;
import br.com.logistics.tms.company.domain.events.ConfigurationAdded;

import java.util.*;

/**
 * Company aggregate root representing a logistics company.
 * IMMUTABLE: All update methods return new instances.
 */
public class Company extends AbstractAggregateRoot {

    private final CompanyId companyId;
    private final String name;
    private final Cnpj cnpj;
    private final Set<CompanyType> types;
    private final Map<String, Object> configuration;
    private final Set<Agreement> agreements;

    // ========== PRIVATE CONSTRUCTOR ==========
    private Company(CompanyId companyId, 
                    String name, 
                    Cnpj cnpj,
                    Set<CompanyType> types,
                    Map<String, Object> configuration,
                    Set<Agreement> agreements,
                    Set<AbstractDomainEvent> domainEvents,
                    Map<String, Object> persistentMetadata) {
        super(new HashSet<>(domainEvents), new HashMap<>(persistentMetadata));
        
        // Validation
        if (companyId == null) {
            throw new ValidationException("CompanyId cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new ValidationException("Company name cannot be null or blank");
        }
        if (cnpj == null) {
            throw new ValidationException("CNPJ cannot be null");
        }
        if (types == null || types.isEmpty()) {
            throw new ValidationException("Company must have at least one type");
        }
        
        this.companyId = companyId;
        this.name = name;
        this.cnpj = cnpj;
        this.types = Set.copyOf(types);
        this.configuration = configuration != null ? Map.copyOf(configuration) : Map.of();
        this.agreements = agreements != null ? Set.copyOf(agreements) : Set.of();
    }

    // ========== FACTORY METHOD (CREATE) ==========
    
    /**
     * Factory method to create a new Company.
     * Places CompanyCreated event.
     */
    public static Company createCompany(String name, 
                                       String cnpj, 
                                       Set<CompanyType> types,
                                       Map<String, Object> configuration) {
        Company company = new Company(
            CompanyId.unique(),
            name,
            new Cnpj(cnpj),
            types,
            configuration,
            new HashSet<>(),
            new HashSet<>(),
            new HashMap<>()
        );
        
        // Place domain event
        company.placeDomainEvent(new CompanyCreated(
            company.getCompanyId().value(),
            company.getName(),
            company.getCnpj().value()
        ));
        
        return company;
    }

    // ========== RECONSTRUCTION (FROM DATABASE) ==========
    
    /**
     * Reconstructs Company from persistence without placing events.
     */
    public static Company reconstruct(CompanyId companyId,
                                     String name,
                                     Cnpj cnpj,
                                     Set<CompanyType> types,
                                     Map<String, Object> configuration,
                                     Set<Agreement> agreements,
                                     Map<String, Object> persistentMetadata) {
        return new Company(
            companyId,
            name,
            cnpj,
            types,
            configuration,
            agreements,
            new HashSet<>(), // No events on reconstruction
            persistentMetadata
        );
    }

    // ========== UPDATE METHODS (IMMUTABLE) ==========
    
    /**
     * Updates company name.
     * Returns NEW instance with event.
     */
    public Company updateName(String newName) {
        if (this.name.equals(newName)) {
            return this; // No change needed
        }
        
        Company updated = new Company(
            this.companyId,
            newName,
            this.cnpj,
            this.types,
            this.configuration,
            this.agreements,
            this.getDomainEvents(),
            this.getPersistentMetadata()
        );
        
        updated.placeDomainEvent(new CompanyUpdated(
            updated.getCompanyId().value(),
            "name",
            this.name,
            newName
        ));
        
        return updated;
    }

    /**
     * Adds configuration entry.
     * Returns NEW instance with event.
     */
    public Company addConfiguration(String key, Object value) {
        if (this.configuration.containsKey(key)) {
            throw new ValidationException("Configuration key already exists: " + key);
        }
        
        Map<String, Object> newConfig = new HashMap<>(this.configuration);
        newConfig.put(key, value);
        
        Company updated = new Company(
            this.companyId,
            this.name,
            this.cnpj,
            this.types,
            newConfig,
            this.agreements,
            this.getDomainEvents(),
            this.getPersistentMetadata()
        );
        
        updated.placeDomainEvent(new ConfigurationAdded(
            updated.getCompanyId().value(),
            key,
            value
        ));
        
        return updated;
    }

    /**
     * Adds an agreement.
     * Returns NEW instance (no event in this example).
     */
    public Company addAgreement(Agreement agreement) {
        if (this.agreements.contains(agreement)) {
            return this; // Already exists
        }
        
        Set<Agreement> newAgreements = new HashSet<>(this.agreements);
        newAgreements.add(agreement);
        
        return new Company(
            this.companyId,
            this.name,
            this.cnpj,
            this.types,
            this.configuration,
            newAgreements,
            this.getDomainEvents(),
            this.getPersistentMetadata()
        );
    }

    // ========== BUSINESS LOGIC ==========
    
    /**
     * Checks if company has a specific type.
     */
    public boolean hasType(CompanyType type) {
        return this.types.contains(type);
    }

    /**
     * Validates if company can create shipment orders.
     */
    public boolean canCreateShipmentOrders() {
        return hasType(CompanyType.MARKETPLACE) || hasType(CompanyType.SHIPPER);
    }

    // ========== GETTERS ONLY ==========
    
    public CompanyId getCompanyId() {
        return companyId;
    }

    public String getName() {
        return name;
    }

    public Cnpj getCnpj() {
        return cnpj;
    }

    public Set<CompanyType> getTypes() {
        return types; // Already immutable from constructor
    }

    public Map<String, Object> getConfiguration() {
        return configuration; // Already immutable from constructor
    }

    public Set<Agreement> getAgreements() {
        return agreements; // Already immutable from constructor
    }

    @Override
    public String toString() {
        return "Company{" +
                "companyId=" + companyId.value() +
                ", name='" + name + '\'' +
                ", cnpj=" + cnpj.value() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Company company)) return false;
        return Objects.equals(companyId, company.companyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyId);
    }
}
```

## Key Points

✅ **Immutability:** All fields are `final`, update methods return new instances
✅ **Private Constructor:** Only factory methods can create instances
✅ **Factory Method:** `createCompany()` places domain event
✅ **Reconstruction:** Separate method for loading from database
✅ **Domain Events:** Placed in aggregate methods, not use cases
✅ **Validation:** In constructor and business methods
✅ **Getters Only:** No setters
✅ **Business Logic:** Methods like `canCreateShipmentOrders()`
