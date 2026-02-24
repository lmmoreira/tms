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

public interface CompanyRepository {
    Company create(Company company);
    Company update(Company company);
    Optional<Company> getCompanyById(CompanyId companyId);
    Optional<Company> getCompanyByCnpj(Cnpj cnpj);
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
    // ... (metadata, createdAt, updatedAt fields)

    protected CompanyJpaEntity() {} // JPA requires no-arg constructor

    public CompanyJpaEntity(final UUID companyId, final String name, final String cnpj,
                           final Set<CompanyType> types, final Map<String, Object> configuration,
                           final Map<String, Object> metadata) {
        this.companyId = companyId;
        this.name = name;
        this.cnpj = cnpj;
        this.types = types != null ? new HashSet<>(types) : new HashSet<>();
        // ... (configuration, metadata initialization, createdAt)
    }

    public Company toDomain() {
        return Company.reconstruct(new CompanyId(this.companyId), this.name, new Cnpj(this.cnpj),
                                  Set.copyOf(this.types), Map.copyOf(this.configuration),
                                  Set.of(), Map.copyOf(this.metadata));
    }

    public static CompanyJpaEntity from(final Company company) {
        return new CompanyJpaEntity(company.getCompanyId().value(), company.getName(),
                                   company.getCnpj().value(), company.getTypes(),
                                   company.getConfiguration(), company.getPersistentMetadata());
    }

    public void updateFrom(final Company company) {
        this.name = company.getName();
        this.cnpj = company.getCnpj().value();
        // ... (types, configuration, metadata updates, updatedAt)
        this.updatedAt = java.time.Instant.now();
    }

    @PreUpdate
    protected void onUpdate() { this.updatedAt = java.time.Instant.now(); }

    // Getters/setters omitted for brevity
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
import java.util.*;

@Repository
public interface CompanyJpaRepository extends JpaRepository<CompanyJpaEntity, UUID> {
    Optional<CompanyJpaEntity> findByCnpj(String cnpj);
    boolean existsByCnpj(String cnpj);
    
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
// ... (domain imports, JPA entity/repo imports, Spring annotations)

@Repository
public class CompanyRepositoryImpl implements CompanyRepository {

    private final CompanyJpaRepository jpaRepository;
    private final OutboxService outboxService;

    public CompanyRepositoryImpl(final CompanyJpaRepository jpaRepository,
                                final OutboxService outboxService) {
        this.jpaRepository = jpaRepository;
        this.outboxService = outboxService;
    }

    @Override
    @Transactional
    public Company create(final Company company) {
        CompanyJpaEntity entity = CompanyJpaEntity.from(company);
        entity = jpaRepository.save(entity);
        outboxService.saveEvents(company.getDomainEvents());
        return entity.toDomain();
    }

    @Override
    @Transactional
    public Company update(final Company company) {
        CompanyJpaEntity entity = jpaRepository.findById(company.getCompanyId().value())
            .orElseThrow(() -> new RuntimeException("Company not found"));
        entity.updateFrom(company);
        entity = jpaRepository.save(entity);
        outboxService.saveEvents(company.getDomainEvents());
        return entity.toDomain();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Company> getCompanyById(final CompanyId companyId) {
        return jpaRepository.findById(companyId.value()).map(CompanyJpaEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Company> getCompanyByCnpj(final Cnpj cnpj) {
        return jpaRepository.findByCnpj(cnpj.value()).map(CompanyJpaEntity::toDomain);
    }

    @Override
    @Transactional
    public void delete(final CompanyId companyId) {
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
// ... (ObjectMapper, Spring annotations)

@Service
public class OutboxService {

    private final OutboxJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OutboxService(final OutboxJpaRepository outboxRepository,
                        final ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void saveEvents(final Set<AbstractDomainEvent> events) {
        if (events == null || events.isEmpty()) return;

        for (final AbstractDomainEvent event : events) {
            try {
                final OutboxEntity outboxEntity = new OutboxEntity(
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
        final Company company = Company.createCompany("Test", "12345678901234", types, config);
        final Company saved = companyRepository.create(company);
        assertNotNull(saved.getCompanyId());
        
        final Optional<Company> found = companyRepository.getCompanyById(saved.getCompanyId());
        assertTrue(found.isPresent());
        assertEquals("Test", found.get().getName());
    }

    @Test
    void shouldUpdateCompanyAndSaveEvents() {
        final Company company = createAndPersistCompany();
        final Company updated = companyRepository.update(company.updateName("Updated Name"));
        assertEquals("Updated Name", updated.getName());
    }
}
```
