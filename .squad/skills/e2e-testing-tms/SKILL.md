# E2E Testing in TMS

## ⚡ TL;DR

- **Environment:** `make start-tms` runs PostgreSQL + RabbitMQ + app server (port 8080)
- **HTTP Files:** IntelliJ HTTP Client format in `src/main/resources/{module}/`
- **Pattern:** Create dependencies → test CRUD → negative cases
- **Common Gotchas:** JPA transient entities, circular hashCode, enum mismatches, value object formatting
- **Confidence:** Medium (validated through agreement E2E, bugs found and fixed)

---

## Environment Setup

### Start TMS Stack

```bash
make start-tms
```

**What runs:**
- PostgreSQL (schemas: company, shipmentorder, etc.)
- RabbitMQ (event bus between modules)
- Spring Boot app on `http://localhost:8080`

**When to use full vs minimal:**
- **Minimal (`make start-tms`):** Most E2E tests — single module CRUD flows
- **Full stack:** Multi-module event-driven flows requiring cross-module synchronization

### Verify Readiness

```bash
# Check app health
curl http://localhost:8080/actuator/health

# Check database
docker ps | grep postgres

# Check message bus
docker ps | grep rabbitmq
```

---

## HTTP Test File Structure

**Location:** `src/main/resources/{module}/{feature}-e2e-{variant}.http`

**Example:** `src/main/resources/company/agreement-e2e-simple.http`

### Template Structure

```http
@server = http://localhost:8080

### ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
### 🧪 {ENTITY} E2E TEST ({VARIANT})
### ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
###
### QUICK TEST: {brief description}
###
### This file tests:
###   ✓ {operation 1}
###   ✓ {operation 2}
###   ✓ {operation 3}
###
### ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━


### ━━━ STEP 1: CREATE DEPENDENCY ENTITIES ━━━
POST {{server}}/{parent-resource}
Content-Type: application/json

{
  "field": "value"
}

> {% client.global.set("parentId", response.body.{idField}) %}


### ━━━ STEP 2: CREATE TARGET ENTITY ━━━
POST {{server}}/{parent-resource}/{{parentId}}/{child-resource}
Content-Type: application/json

{
  "field": "value"
}

> {% client.global.set("targetId", response.body.{idField}) %}


### ━━━ STEP 3: LIST ENTITIES ━━━
GET {{server}}/{parent-resource}/{{parentId}}/{child-resource}


### ━━━ STEP 4: GET BY ID ━━━
GET {{server}}/{parent-resource}/{{parentId}}/{child-resource}/{{targetId}}


### ━━━ STEP 5: UPDATE ━━━
PUT {{server}}/{parent-resource}/{{parentId}}/{child-resource}/{{targetId}}
Content-Type: application/json

{
  "field": "new-value"
}


### ━━━ STEP 6: VERIFY UPDATE ━━━
GET {{server}}/{parent-resource}/{{parentId}}/{child-resource}/{{targetId}}


### ━━━ STEP 7: DELETE ━━━
DELETE {{server}}/{parent-resource}/{{parentId}}/{child-resource}/{{targetId}}


### ━━━ STEP 8: VERIFY DELETION ━━━
GET {{server}}/{parent-resource}/{{parentId}}/{child-resource}


### ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
### NEGATIVE TEST SCENARIOS
### ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━


### ━━━ NEGATIVE 1: DUPLICATE (SHOULD FAIL) ━━━
POST {{server}}/{parent-resource}/{{parentId}}/{child-resource}
Content-Type: application/json

{
  "field": "existing-value"
}


### ━━━ NEGATIVE 2: INVALID REFERENCE (SHOULD FAIL) ━━━
POST {{server}}/{parent-resource}/{{parentId}}/{child-resource}
Content-Type: application/json

{
  "referenceId": "00000000-0000-0000-0000-000000000000"
}


### ━━━ NEGATIVE 3: MISSING REQUIRED FIELD (SHOULD FAIL) ━━━
POST {{server}}/{parent-resource}/{{parentId}}/{child-resource}
Content-Type: application/json

{
  "optionalField": "value"
}
```

---

## Value Object Formatting

TMS uses value objects for validated primitives. Format matters.

### Common Value Objects

| Type | Format | Example | Validation |
|------|--------|---------|------------|
| **CNPJ** | `"##.###.###/####-##"` | `"11.222.333/0001-44"` | 14 digits with formatting |
| **UUID** | `"xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"` | `"123e4567-e89b-12d3-a456-426614174000"` | UUID v4/v7 format |
| **Instant** | `"yyyy-MM-ddTHH:mm:ssZ"` | `"2026-02-24T00:00:00Z"` | ISO 8601 UTC |

### DTO Field Example

```json
{
  "name": "Test Company",
  "cnpj": "11.222.333/0001-44",
  "types": ["MARKETPLACE"],
  "configuration": {
    "webhook": "https://api.example.com/webhooks"
  }
}
```

**Gotcha:** Omit formatting (e.g., `"11222333000144"` instead of `"11.222.333/0001-44"`) → validation fails silently or throws exception.

---

## DTO Field Validation Patterns

### Required vs Optional

```java
// Required fields — throw ValidationException if null/empty
if (input.name() == null || input.name().isBlank()) {
    throw new ValidationException("Name is required");
}

// Optional fields — skip if null
if (input.configuration() != null) {
    // process configuration
}
```

### Enum Validation

```java
// DTO accepts string, converts to enum
public record CreateAgreementDTO(
    String type  // "DELIVERS_WITH", "WORKS_WITH", etc.
) {}

// Use case validates enum match
try {
    AgreementType.valueOf(dto.type());
} catch (IllegalArgumentException e) {
    throw new ValidationException("Invalid agreement type: " + dto.type());
}
```

**HTTP test:**
```http
POST {{server}}/companies/{{companyId}}/agreements
Content-Type: application/json

{
  "type": "INVALID_TYPE",  # Should fail
  "destinationCompanyId": "{{otherId}}"
}
```

### Configuration Maps

```java
// Map<String, Object> for flexible JSON configuration
private Map<String, Object> configuration;

// Validation example
if (configuration.isEmpty()) {
    throw new ValidationException("Configuration cannot be empty");
}
```

**HTTP test:**
```http
{
  "configuration": {
    "contractNumber": "AGR-2026-001",
    "maxWeight": 1000,
    "regions": ["SP", "RJ"]
  }
}
```

**Gotcha:** Avoid nested complex objects — DTOs should map cleanly to `Map<String, Object>`.

---

## JPA Bidirectional Relationship Patterns

### Problem 1: Transient Entity Bug

**Symptom:** `org.hibernate.TransientObjectException` when persisting parent with new child.

**Cause:** JPA repository saves parent, but child entity is transient (not yet persisted).

**❌ WRONG:**

```java
@Entity
public class Company {
    @OneToMany(mappedBy = "sourceCompany", cascade = CascadeType.ALL)
    private Set<Agreement> agreements;
    
    public Company addAgreement(Agreement agreement) {
        agreements.add(agreement);  // Transient!
        return this;
    }
}
```

**✅ CORRECT — Resolver Function Pattern:**

```java
// Repository implementation
@Override
public Company create(final Company company) {
    final CompanyJpaEntity entity = CompanyJpaEntity.from(company);
    
    // Resolver: map domain agreements → JPA entities BEFORE save
    final Set<AgreementJpaEntity> agreementEntities = company.getAgreements().stream()
        .map(agreement -> {
            final AgreementJpaEntity ae = AgreementJpaEntity.from(agreement);
            ae.setSourceCompany(entity);  // Set FK reference
            ae.setDestinationCompany(resolveCompany(agreement.getDestinationCompanyId()));
            return ae;
        })
        .collect(Collectors.toSet());
    
    entity.setAgreements(agreementEntities);
    
    final CompanyJpaEntity saved = repository.save(entity);
    return saved.toDomain();
}

private CompanyJpaEntity resolveCompany(final CompanyId companyId) {
    return repository.findById(companyId.value())
        .orElseThrow(() -> new NotFoundException("Company not found"));
}
```

**Key points:**
- Resolver runs BEFORE save
- Maps domain IDs → JPA entities via repository lookups
- Sets FK references explicitly (`setSourceCompany`, `setDestinationCompany`)
- Works for ANY ManyToOne FK scenario

### Problem 2: Circular hashCode

**Symptom:** `StackOverflowError` when persisting bidirectional relationship.

**Cause:** Lombok `@Data` generates `hashCode()` and `equals()` using ALL fields, including bidirectional references.

**❌ WRONG:**

```java
@Entity
@Data  // ← Danger: generates hashCode on ALL fields
public class Agreement {
    @ManyToOne
    private Company sourceCompany;  // Circular reference
}
```

**✅ CORRECT — ID-Only equals/hashCode:**

```java
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Agreement {
    @Id
    private UUID id;
    
    @ManyToOne
    private Company sourceCompany;
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Agreement)) return false;
        final Agreement that = (Agreement) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
```

**Applies to ALL JPA entities with bidirectional relationships.**

### Lombok Annotation Safety

| Annotation | Safe? | Notes |
|------------|-------|-------|
| `@Getter` | ✅ | Safe — only generates getters |
| `@Setter` | ✅ | Safe — only generates setters |
| `@NoArgsConstructor` | ✅ | Safe — JPA requires it |
| `@Data` | ❌ | **Danger** — generates `equals`/`hashCode` on ALL fields |
| `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` | ✅ | Safe — use with `@EqualsAndHashCode.Include` on ID field |

**Recommended pattern for JPA entities:**

```java
@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Agreement {
    @Id
    @EqualsAndHashCode.Include
    private UUID id;
    
    @ManyToOne
    private Company sourceCompany;
}
```

---

## REST Controller Routing

### Nested Resource Paths

TMS uses nested paths for child resources:

```
POST   /companies/{companyId}/agreements
GET    /companies/{companyId}/agreements
GET    /companies/{companyId}/agreements/{agreementId}
PUT    /companies/{companyId}/agreements/{agreementId}
DELETE /companies/{companyId}/agreements/{agreementId}
```

**Pattern:** `/{parent-resource}/{parentId}/{child-resource}/{childId?}`

### Controller Example

```java
@RestController
@RequestMapping("companies/{companyId}/agreements")
@Cqrs(DatabaseRole.WRITE)
public class CreateAgreementController {
    
    @PostMapping
    public Object create(@PathVariable final UUID companyId,
                        @RequestBody final CreateAgreementDTO dto) {
        return restUseCaseExecutor
                .from(createAgreementUseCase)
                .withInput(new CreateAgreementUseCase.Input(companyId, dto))
                .mapOutputTo(AgreementResponseDTO.class)
                .presentWith(output -> defaultRestPresenter.present(output, HttpStatus.CREATED.value()))
                .execute();
    }
}
```

**Key points:**
- `@PathVariable` extracts parent ID from URL
- Controller passes parent ID into use case input
- Zero business logic — only delegation

### Path Variable Validation

**Use case validates parent exists:**

```java
@Override
public Output execute(final Input input) {
    // 1. Validate parent exists
    final Company company = companyRepository.getCompanyById(new CompanyId(input.companyId()))
            .orElseThrow(() -> new NotFoundException("Company not found"));
    
    // 2. Validate destination exists
    final Company destination = companyRepository.getCompanyById(new CompanyId(input.destinationCompanyId()))
            .orElseThrow(() -> new NotFoundException("Destination company not found"));
    
    // 3. Create child entity
    final Agreement agreement = Agreement.createAgreement(...);
    
    // 4. Add to parent
    final Company updated = company.addAgreement(agreement);
    
    // 5. Persist
    final Company saved = companyRepository.create(updated);
    
    return new Output(saved.getAgreements().iterator().next().getAgreementId().value());
}
```

---

## E2E Test Flow Template

### Phase 1: Dependency Setup

```http
### ━━━ CREATE DEPENDENCY 1 ━━━
POST {{server}}/companies
Content-Type: application/json

{
  "name": "Parent Company",
  "cnpj": "11.222.333/0001-44",
  "types": ["MARKETPLACE"],
  "configuration": {}
}

> {% client.global.set("companyId", response.body.companyId) %}


### ━━━ CREATE DEPENDENCY 2 ━━━
POST {{server}}/companies
Content-Type: application/json

{
  "name": "Related Company",
  "cnpj": "22.333.444/0001-55",
  "types": ["LOGISTICS_PROVIDER"],
  "configuration": {}
}

> {% client.global.set("relatedId", response.body.companyId) %}
```

**Rule:** Create ALL dependencies BEFORE testing target entity.

### Phase 2: CRUD Operations

```http
### ━━━ CREATE ━━━
POST {{server}}/companies/{{companyId}}/agreements
Content-Type: application/json

{
  "destinationCompanyId": "{{relatedId}}",
  "type": "DELIVERS_WITH",
  "configuration": {},
  "conditions": [],
  "validFrom": "2026-02-24T00:00:00Z",
  "validTo": "2026-12-31T23:59:59Z"
}

> {% client.global.set("agreementId", response.body.agreementId) %}


### ━━━ LIST ━━━
GET {{server}}/companies/{{companyId}}/agreements


### ━━━ GET BY ID ━━━
GET {{server}}/companies/{{companyId}}/agreements/{{agreementId}}


### ━━━ UPDATE ━━━
PUT {{server}}/companies/{{companyId}}/agreements/{{agreementId}}
Content-Type: application/json

{
  "validTo": "2027-12-31T23:59:59Z"
}


### ━━━ VERIFY UPDATE ━━━
GET {{server}}/companies/{{companyId}}/agreements/{{agreementId}}


### ━━━ DELETE ━━━
DELETE {{server}}/companies/{{companyId}}/agreements/{{agreementId}}


### ━━━ VERIFY DELETION ━━━
GET {{server}}/companies/{{companyId}}/agreements
```

### Phase 3: Negative Cases

```http
### ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
### NEGATIVE TEST SCENARIOS
### ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━


### ━━━ DUPLICATE (SHOULD FAIL) ━━━
POST {{server}}/companies/{{companyId}}/agreements
Content-Type: application/json

{
  "destinationCompanyId": "{{relatedId}}",
  "type": "DELIVERS_WITH",
  "configuration": {},
  "conditions": [],
  "validFrom": "2026-02-24T00:00:00Z",
  "validTo": "2026-12-31T23:59:59Z"
}


### ━━━ SELF-REFERENCE (SHOULD FAIL) ━━━
POST {{server}}/companies/{{companyId}}/agreements
Content-Type: application/json

{
  "destinationCompanyId": "{{companyId}}",
  "type": "DELIVERS_WITH",
  "configuration": {},
  "conditions": [],
  "validFrom": "2026-02-24T00:00:00Z",
  "validTo": "2026-12-31T23:59:59Z"
}


### ━━━ MISSING TARGET (SHOULD FAIL) ━━━
POST {{server}}/companies/{{companyId}}/agreements
Content-Type: application/json

{
  "destinationCompanyId": "00000000-0000-0000-0000-000000000000",
  "type": "DELIVERS_WITH",
  "configuration": {},
  "conditions": [],
  "validFrom": "2026-02-24T00:00:00Z",
  "validTo": "2026-12-31T23:59:59Z"
}


### ━━━ INVALID DATE RANGE (SHOULD FAIL) ━━━
POST {{server}}/companies/{{companyId}}/agreements
Content-Type: application/json

{
  "destinationCompanyId": "{{relatedId}}",
  "type": "DELIVERS_WITH",
  "configuration": {},
  "conditions": [],
  "validFrom": "2026-12-31T00:00:00Z",
  "validTo": "2026-01-01T23:59:59Z"
}
```

**Common negative cases:**
- Duplicate detection
- Self-reference validation
- Missing foreign key
- Invalid date/time ranges
- Enum mismatch
- Missing required fields
- Configuration validation

---

## Common Gotchas

### 1. Configuration Validation

**Problem:** Empty configuration maps silently accepted.

**Fix:** Add domain-level validation.

```java
public static Agreement createAgreement(
    final CompanyId sourceCompanyId,
    final CompanyId destinationCompanyId,
    final AgreementType type,
    final Map<String, Object> configuration,
    final Set<AgreementCondition> conditions,
    final Instant validFrom,
    final Instant validTo
) {
    if (configuration.isEmpty()) {
        throw new ValidationException("Agreement configuration cannot be empty");
    }
    // ...
}
```

### 2. Enum Mismatch

**Problem:** DTO accepts string, but enum validation happens late.

**Fix:** Validate early in use case.

```java
try {
    AgreementType.valueOf(dto.type());
} catch (IllegalArgumentException e) {
    throw new ValidationException("Invalid agreement type: " + dto.type() + 
                                 ". Valid types: " + Arrays.toString(AgreementType.values()));
}
```

### 3. Path Variable Extraction

**Problem:** Nested path variables not extracted.

**Fix:** Ensure `@PathVariable` matches route.

```java
@RequestMapping("companies/{companyId}/agreements")
public class CreateAgreementController {
    
    @PostMapping
    public Object create(
        @PathVariable final UUID companyId,  // ← Must match {companyId}
        @RequestBody final CreateAgreementDTO dto
    ) {
        // ...
    }
}
```

### 4. Transient Entity Exception

**Fix:** Use resolver function pattern (see JPA section above).

### 5. Circular hashCode

**Fix:** ID-only equals/hashCode (see JPA section above).

---

## Debugging Failed Tests

### Check HTTP Response

```http
### Test request
POST {{server}}/companies/{{companyId}}/agreements
Content-Type: application/json

{
  "destinationCompanyId": "{{relatedId}}",
  "type": "DELIVERS_WITH"
}

### Check response body
> {%
    client.test("Status is 201", function() {
        client.assert(response.status === 201, "Expected 201");
    });
    
    client.test("Body has agreementId", function() {
        client.assert(response.body.agreementId !== undefined);
    });
%}
```

### Check Application Logs

```bash
# Follow TMS logs
docker logs -f tms-app

# Filter for errors
docker logs tms-app 2>&1 | grep ERROR

# Filter for specific request
docker logs tms-app 2>&1 | grep "POST /companies"
```

### Check Database State

```bash
# Connect to PostgreSQL
docker exec -it tms-postgres psql -U postgres -d tms

# Query company schema
\c tms
SET search_path TO company;
SELECT * FROM companies;
SELECT * FROM agreements;
```

### Common Error Patterns

| Error | Cause | Fix |
|-------|-------|-----|
| `TransientObjectException` | Child entity not persisted before parent | Use resolver pattern |
| `StackOverflowError` | Circular hashCode in bidirectional relationship | ID-only equals/hashCode |
| `ValidationException: Invalid CNPJ` | Missing formatting in request | Add dots/slashes: `"11.222.333/0001-44"` |
| `NotFoundException: Company not found` | Path variable mismatch or missing entity | Verify `@PathVariable` name, check entity exists |
| `IllegalArgumentException` | Enum mismatch | Check enum value string matches exactly |

---

## When to Write E2E Tests

**Always:**
- New entity CRUD flows
- Multi-entity relationships (agreements, conditions, etc.)
- Complex validation rules

**Before:**
- Deploying to staging/production
- Merging feature branches

**After fixing bugs:**
- Add negative test case that reproduces the bug
- Verify fix passes new test

---

## Related Patterns

- **Immutable Aggregate Update** — `.squad/skills/immutable-aggregate-update/SKILL.md`
- **Fake Repository Pattern** — `.squad/skills/fake-repository-pattern/SKILL.md` (for unit tests)
- **Test Data Builder Pattern** — `.squad/skills/test-data-builder-pattern/SKILL.md` (for complex test data)

---

## Examples in Codebase

- `src/main/resources/company/agreement-e2e-simple.http` — Agreement CRUD flow
- `src/main/java/br/com/logistics/tms/company/infrastructure/jpa/repositories/CompanyRepositoryImpl.java` — Resolver pattern
- `src/main/java/br/com/logistics/tms/company/infrastructure/jpa/entities/AgreementJpaEntity.java` — ID-only equals/hashCode
