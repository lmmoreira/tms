# Repository Implementation Example

This document shows complete repository implementation patterns in TMS.

---

## Repository Interface (Application Layer)

**Location:** `company/application/repositories/CompanyRepository.java`

```java
package br.com.logistics.tms.company.application.repositories;

import br.com.logistics.tms.company.domain.Cnpj;
import br.com.logistics.tms.company.domain.Company;
import br.com.logistics.tms.company.domain.CompanyId;

import java.util.Optional;

/**
 * Repository interface for Company aggregate.
 * Defined in application layer, implemented in infrastructure.
 */
public interface CompanyRepository {

    /**
     * Creates a new company.
     * Persists entity and domain events.
     */
    Company create(Company company);

    /**
     * Updates an existing company.
     * Persists entity and domain events.
     */
    Company update(Company company);

    /**
     * Finds company by ID.
     */
    Optional<Company> getCompanyById(CompanyId companyId);

    /**
     * Finds company by CNPJ.
     */
    Optional<Company> getCompanyByCnpj(Cnpj cnpj);

    /**
     * Deletes a company by ID.
     */
    void delete(CompanyId companyId);
}
```

---

## JPA Entity (Infrastructure Layer)

**Location:** `company/infrastructure/jpa/CompanyJpaEntity.java`

```java
package br.com.logistics.tms.company.infrastructure.jpa;

import br.com.logistics.tms.company.domain.*;
import jakarta.persistence.*;

import java.util.*;

/**
 * JPA entity for Company aggregate.
 * Maps between database and domain model.
 */
@Entity
@Table(name = "companies")
public class CompanyJpaEntity {

    @Id
    @Column(name = "company_id")
    private UUID companyId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "cnpj", unique = true, nullable = false, length = 14)
    private String cnpj;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "company_types", joinColumns = @JoinColumn(name = "company_id"))
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private Set<CompanyType> types = new HashSet<>();

    @Column(name = "configuration", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> configuration = new HashMap<>();

    @Column(name = "metadata", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> metadata = new HashMap<>();

    @Column(name = "created_at", nullable = false)
    private java.time.Instant createdAt;

    @Column(name = "updated_at")
    private java.time.Instant updatedAt;

    // ========== CONSTRUCTORS ==========

    protected CompanyJpaEntity() {
        // JPA requires no-arg constructor
    }

    public CompanyJpaEntity(UUID companyId, 
                           String name, 
                           String cnpj,
                           Set<CompanyType> types,
                           Map<String, Object> configuration,
                           Map<String, Object> metadata) {
        this.companyId = companyId;
        this.name = name;
        this.cnpj = cnpj;
        this.types = types != null ? new HashSet<>(types) : new HashSet<>();
        this.configuration = configuration != null ? new HashMap<>(configuration) : new HashMap<>();
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        this.createdAt = java.time.Instant.now();
    }

    // ========== DOMAIN CONVERSION ==========

    /**
     * Converts JPA entity to domain aggregate.
     */
    public Company toDomain() {
        return Company.reconstruct(
            new CompanyId(this.companyId),
            this.name,
            new Cnpj(this.cnpj),
            Set.copyOf(this.types),
            Map.copyOf(this.configuration),
            Set.of(), // Agreements would be loaded separately if needed
            Map.copyOf(this.metadata)
        );
    }

    /**
     * Creates JPA entity from domain aggregate.
     */
    public static CompanyJpaEntity from(Company company) {
        return new CompanyJpaEntity(
            company.getCompanyId().value(),
            company.getName(),
            company.getCnpj().value(),
            company.getTypes(),
            company.getConfiguration(),
            company.getPersistentMetadata()
        );
    }

    /**
     * Updates this entity from domain aggregate.
     */
    public void updateFrom(Company company) {
        this.name = company.getName();
        this.cnpj = company.getCnpj().value();
        this.types = new HashSet<>(company.getTypes());
        this.configuration = new HashMap<>(company.getConfiguration());
        this.metadata = new HashMap<>(company.getPersistentMetadata());
        this.updatedAt = java.time.Instant.now();
    }

    // ========== LIFECYCLE CALLBACKS ==========

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = java.time.Instant.now();
    }

    // ========== GETTERS/SETTERS ==========

    public UUID getCompanyId() {
        return companyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCnpj() {
        return cnpj;
    }

    public Set<CompanyType> getTypes() {
        return types;
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public java.time.Instant getCreatedAt() {
        return createdAt;
    }

    public java.time.Instant getUpdatedAt() {
        return updatedAt;
    }
}
```

---

## Spring Data JPA Repository

**Location:** `company/infrastructure/jpa/CompanyJpaRepository.java`

```java
package br.com.logistics.tms.company.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Company.
 */
@Repository
public interface CompanyJpaRepository extends JpaRepository<CompanyJpaEntity, UUID> {

    /**
     * Finds company by CNPJ.
     */
    Optional<CompanyJpaEntity> findByCnpj(String cnpj);

    /**
     * Checks if company exists by CNPJ.
     */
    boolean existsByCnpj(String cnpj);

    /**
     * Custom query example.
     */
    @Query("SELECT c FROM CompanyJpaEntity c WHERE c.name LIKE %:name%")
    List<CompanyJpaEntity> findByNameContaining(@Param("name") String name);
}
```

---

## Repository Implementation (Infrastructure Layer)

**Location:** `company/infrastructure/repositories/CompanyRepositoryImpl.java`

```java
package br.com.logistics.tms.company.infrastructure.repositories;

import br.com.logistics.tms.commons.infrastructure.outbox.OutboxService;
import br.com.logistics.tms.company.application.repositories.CompanyRepository;
import br.com.logistics.tms.company.domain.Cnpj;
import br.com.logistics.tms.company.domain.Company;
import br.com.logistics.tms.company.domain.CompanyId;
import br.com.logistics.tms.company.infrastructure.jpa.CompanyJpaEntity;
import br.com.logistics.tms.company.infrastructure.jpa.CompanyJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of CompanyRepository.
 * Handles persistence and event outbox.
 */
@Repository
public class CompanyRepositoryImpl implements CompanyRepository {

    private final CompanyJpaRepository jpaRepository;
    private final OutboxService outboxService;

    public CompanyRepositoryImpl(CompanyJpaRepository jpaRepository,
                                OutboxService outboxService) {
        this.jpaRepository = jpaRepository;
        this.outboxService = outboxService;
    }

    @Override
    @Transactional
    public Company create(Company company) {
        // 1. Convert domain to JPA entity
        CompanyJpaEntity entity = CompanyJpaEntity.from(company);
        
        // 2. Save entity
        entity = jpaRepository.save(entity);
        
        // 3. Save domain events to outbox (same transaction)
        outboxService.saveEvents(company.getDomainEvents());
        
        // 4. Return updated domain object
        return entity.toDomain();
    }

    @Override
    @Transactional
    public Company update(Company company) {
        // 1. Find existing entity
        CompanyJpaEntity entity = jpaRepository.findById(company.getCompanyId().value())
            .orElseThrow(() -> new RuntimeException("Company not found"));
        
        // 2. Update entity from domain
        entity.updateFrom(company);
        
        // 3. Save updated entity
        entity = jpaRepository.save(entity);
        
        // 4. Save domain events to outbox
        outboxService.saveEvents(company.getDomainEvents());
        
        // 5. Return updated domain object
        return entity.toDomain();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Company> getCompanyById(CompanyId companyId) {
        return jpaRepository.findById(companyId.value())
            .map(CompanyJpaEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Company> getCompanyByCnpj(Cnpj cnpj) {
        return jpaRepository.findByCnpj(cnpj.value())
            .map(CompanyJpaEntity::toDomain);
    }

    @Override
    @Transactional
    public void delete(CompanyId companyId) {
        jpaRepository.deleteById(companyId.value());
    }
}
```

---

## Outbox Service

**Location:** `commons/infrastructure/outbox/OutboxService.java`

```java
package br.com.logistics.tms.commons.infrastructure.outbox;

import br.com.logistics.tms.commons.domain.AbstractDomainEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Service to save domain events to outbox table.
 * Events are published asynchronously by OutboxPublisher.
 */
@Service
public class OutboxService {

    private final OutboxJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxJpaRepository outboxRepository,
                        ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Saves domain events to outbox table.
     * Uses MANDATORY propagation to ensure it runs in existing transaction.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void saveEvents(Set<AbstractDomainEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        for (AbstractDomainEvent event : events) {
            try {
                OutboxEntity outboxEntity = new OutboxEntity(
                    event.getClass().getSimpleName(),
                    event.getAggregateId(),
                    objectMapper.writeValueAsString(event)
                );
                outboxRepository.save(outboxEntity);
            } catch (Exception e) {
                throw new RuntimeException("Failed to save event to outbox", e);
            }
        }
    }
}
```

---

## Key Patterns

### Repository Interface
✅ **Location:** Application layer
✅ **Dependencies:** Only domain types
✅ **Methods:** CRUD operations using domain objects

### JPA Entity
✅ **Location:** Infrastructure layer
✅ **Conversion:** `toDomain()` and `from()` methods
✅ **Mutable:** JPA requires setters
✅ **Annotations:** Standard JPA annotations

### Repository Implementation
✅ **Transaction Management:** `@Transactional` on write operations
✅ **Event Handling:** Save events to outbox in same transaction
✅ **Domain Conversion:** Convert between JPA and domain objects
✅ **Read-Only:** Use `@Transactional(readOnly = true)` for queries

### Event Outbox
✅ **Transactional:** Events saved in same transaction as entity
✅ **Propagation:** Use `MANDATORY` to enforce existing transaction
✅ **Serialization:** JSON serialization of events
✅ **Async Publishing:** Separate background process publishes events

---

## Testing Repository

```java
@SpringBootTest
@Testcontainers
class CompanyRepositoryImplTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    private CompanyRepository companyRepository;

    @Test
    void shouldCreateCompanyAndSaveEvents() {
        // Given
        Company company = Company.createCompany("Test", "12345678901234", types, config);

        // When
        company = companyRepository.create(company);

        // Then
        assertNotNull(company.getCompanyId());
        
        // Verify company is persisted
        Optional<Company> found = companyRepository.getCompanyById(company.getCompanyId());
        assertTrue(found.isPresent());
        assertEquals("Test", found.get().getName());
    }

    @Test
    void shouldUpdateCompanyAndSaveEvents() {
        // Given
        Company company = createAndPersistCompany();
        Company updated = company.updateName("Updated Name");

        // When
        updated = companyRepository.update(updated);

        // Then
        assertEquals("Updated Name", updated.getName());
    }
}
```
