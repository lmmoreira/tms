# Prompt: Create New Use Case

## Purpose
Template for creating a new use case operation in a TMS module.

---

## Instructions for AI Assistant

Create a complete use case implementation following TMS patterns.

### Required Information

**Operation Details:**
- **Operation Name:** `[Verb + Entity]` (e.g., GetCompanyByCnpj, UpdateCompanyName)
- **Module:** `[company | shipmentorder | other]`
- **Type:** `[WRITE | READ]`
- **Description:** `[Brief description of what this operation does]`

**Input Fields:**
```
- field1: Type (description)
- field2: Type (description)
```

**Output Fields:**
```
- field1: Type (description)
- field2: Type (description)
```

**Business Rules:**
```
- Rule 1: [Description]
- Rule 2: [Description]
```

---

## Implementation Checklist

Generate the following files:

### 1. Use Case Class
**Location:** `src/main/java/br/com/logistics/tms/{module}/application/usecases/{Verb}{Entity}UseCase.java`

**Pattern:**
```java
@DomainService
@Cqrs(DatabaseRole.WRITE)  // or READ
public class {Verb}{Entity}UseCase implements UseCase<{UseCase}.Input, {UseCase}.Output> {

    private final {Entity}Repository repository;

    public {UseCase}({Entity}Repository repository) {
        this.repository = repository;
    }

    @Override
    public Output execute(Input input) {
        // 1. Validation (business rules)
        
        // 2. Domain logic (via aggregate or repository)
        
        // 3. Persistence (if WRITE operation)
        
        // 4. Return output
    }

    public record Input(
        // Input fields with validation annotations if needed
    ) {}

    public record Output(
        // Output fields
    ) {}
}
```

### 2. REST Controller
**Location:** `src/main/java/br/com/logistics/tms/{module}/infrastructure/rest/{Verb}Controller.java`

**Pattern:**
```java
@RestController
@RequestMapping("{entities}")
@Cqrs(DatabaseRole.WRITE)  // or READ
public class {Verb}Controller {

    private final {Verb}{Entity}UseCase useCase;
    private final DefaultRestPresenter defaultRestPresenter;
    private final RestUseCaseExecutor restUseCaseExecutor;

    public {Verb}Controller({Verb}{Entity}UseCase useCase,
                           DefaultRestPresenter defaultRestPresenter,
                           RestUseCaseExecutor restUseCaseExecutor) {
        this.useCase = useCase;
        this.defaultRestPresenter = defaultRestPresenter;
        this.restUseCaseExecutor = restUseCaseExecutor;
    }

    @{HttpMethod}("{path}")
    public Object {methodName}(@RequestBody {Operation}DTO dto) {
        return restUseCaseExecutor
                .from(useCase)
                .withInput(dto)
                .mapOutputTo({Operation}ResponseDTO.class)
                .presentWith(output -> defaultRestPresenter.present(output, HttpStatus.{STATUS}.value()))
                .execute();
    }
}
```

**HTTP Method Mapping:**
- CREATE → `@PostMapping`, `HttpStatus.CREATED` (201)
- READ → `@GetMapping`, `HttpStatus.OK` (200)
- UPDATE → `@PutMapping`, `HttpStatus.OK` (200)
- DELETE → `@DeleteMapping`, `HttpStatus.NO_CONTENT` (204)

### 3. DTOs
**Location:** `src/main/java/br/com/logistics/tms/{module}/infrastructure/dto/`

**Request DTO:**
```java
public record {Operation}DTO(
    // Input fields matching use case Input
) {}
```

**Response DTO:**
```java
public record {Operation}ResponseDTO(
    // Output fields matching use case Output
) {}
```

### 4. Unit Test (if applicable)
**Location:** `src/test/java/br/com/logistics/tms/{module}/application/usecases/{Verb}{Entity}UseCaseTest.java`

**Pattern:**
```java
class {Verb}{Entity}UseCaseTest {

    private {Entity}Repository repository;
    private {Verb}{Entity}UseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock({Entity}Repository.class);
        useCase = new {Verb}{Entity}UseCase(repository);
    }

    @Test
    void shouldExecuteSuccessfully() {
        // Given
        var input = new {UseCase}.Input(...);
        
        // Mock repository if needed
        when(repository.someMethod(...)).thenReturn(...);
        
        // When
        var output = useCase.execute(input);
        
        // Then
        assertNotNull(output);
        // More assertions
    }

    @Test
    void shouldThrowExceptionWhenInvalid() {
        // Test validation scenarios
    }
}
```

### 5. Integration Test
**Location:** `src/test/java/br/com/logistics/tms/{module}/infrastructure/rest/{Operation}IntegrationTest.java`

**Pattern:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class {Operation}IntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:latest");

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCompleteFullFlow() {
        // Given
        var requestDto = new {Operation}DTO(...);
        
        // When
        ResponseEntity<{Operation}ResponseDTO> response = restTemplate
            .postForEntity("/{entities}", requestDto, {Operation}ResponseDTO.class);
        
        // Then
        assertEquals(HttpStatus.{STATUS}, response.getStatusCode());
        assertNotNull(response.getBody());
        // More assertions
    }
}
```

---

## Special Considerations

### For WRITE Operations
- Update aggregate via domain method (returns new instance)
- Domain events should be placed in aggregate methods
- Repository saves aggregate + events to outbox
- Test that events are created

### For READ Operations
- No domain events
- Use read database (`DatabaseRole.READ`)
- Return view model, not full aggregate
- Consider pagination for list operations

### For Operations Involving Multiple Aggregates
- Consider if this should trigger domain events
- Each aggregate must be independently consistent
- Use eventual consistency via events if needed

---

## Example Usage

**Request:**
```
Operation: Get Company by CNPJ
Module: company
Type: READ

Input:
- cnpj: String (Brazilian business ID, 14 digits)

Output:
- companyId: UUID
- name: String
- cnpj: String
- types: Set<CompanyType>

Business Rules:
- CNPJ must be valid format (14 digits)
- Company must exist
```

**Generated Files:**
1. `GetCompanyByCnpjUseCase.java`
2. `GetByCnpjController.java`
3. `GetCompanyByCnpjDTO.java` (just wraps cnpj)
4. `GetCompanyByCnpjResponseDTO.java`
5. `GetCompanyByCnpjUseCaseTest.java`
6. `GetCompanyByCnpjIntegrationTest.java`

---

## Validation Checklist

Before submitting, verify:

- [ ] Use case has `@DomainService` and `@Cqrs` annotations
- [ ] Controller uses `RestUseCaseExecutor` pattern
- [ ] DTOs are Java records
- [ ] Proper HTTP method and status code
- [ ] Tests cover happy path and validation failures
- [ ] If WRITE operation, domain events are created in aggregate
- [ ] Naming follows conventions: `{Verb}{Entity}UseCase`

---

## References

- **Pattern Examples:** `/doc/ai/examples/complete-use-case.md`
- **Architecture Guide:** `/doc/ai/ARCHITECTURE.md`
- **Existing Use Cases:** `src/main/java/br/com/logistics/tms/{module}/application/usecases/`
