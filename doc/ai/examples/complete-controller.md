# Complete REST Controller Examples

This document shows complete REST controller implementations following TMS patterns.

---

## POST Controller: CreateController

**Location:** `company/infrastructure/rest/CreateController.java`

```java
package br.com.logistics.tms.company.infrastructure.rest;

import br.com.logistics.tms.commons.application.annotations.Cqrs;
import br.com.logistics.tms.commons.application.annotations.DatabaseRole;
// ... (rest infrastructure imports, DTOs)

@RestController
@RequestMapping("companies")
@Cqrs(DatabaseRole.WRITE)
public class CreateController {

    private final CreateCompanyUseCase createCompanyUseCase;
    private final DefaultRestPresenter defaultRestPresenter;
    private final RestUseCaseExecutor restUseCaseExecutor;

    public CreateController(final CreateCompanyUseCase createCompanyUseCase,
                           final DefaultRestPresenter defaultRestPresenter,
                           final RestUseCaseExecutor restUseCaseExecutor) {
        this.createCompanyUseCase = createCompanyUseCase;
        this.defaultRestPresenter = defaultRestPresenter;
        this.restUseCaseExecutor = restUseCaseExecutor;
    }

    @PostMapping
    public Object create(@RequestBody final CreateCompanyDTO dto) {
        return restUseCaseExecutor
            .from(createCompanyUseCase)
            .withInput(dto)
            .mapOutputTo(CreateCompanyResponseDTO.class)
            .presentWith(output -> defaultRestPresenter.present(output, HttpStatus.CREATED.value()))
            .execute();
    }
}
```

### Request DTO

**Location:** `company/infrastructure/dto/CreateCompanyDTO.java`

```java
package br.com.logistics.tms.company.infrastructure.dto;

import br.com.logistics.tms.commons.application.UseCaseInputMapper;
// ... (CreateCompanyUseCase, CompanyType, JsonProperty)

public record CreateCompanyDTO(
    @JsonProperty("name") String name,
    @JsonProperty("cnpj") String cnpj,
    @JsonProperty("types") Set<CompanyType> types,
    @JsonProperty("configuration") Map<String, Object> configuration
) implements UseCaseInputMapper<CreateCompanyUseCase.Input> {

    @Override
    public CreateCompanyUseCase.Input toInput() {
        return new CreateCompanyUseCase.Input(name, cnpj, types, configuration);
    }
}
```

### Response DTO

**Location:** `company/infrastructure/dto/CreateCompanyResponseDTO.java`

```java
package br.com.logistics.tms.company.infrastructure.dto;

import br.com.logistics.tms.commons.application.UseCaseOutputMapper;
// ... (CreateCompanyUseCase, JsonProperty, UUID)

public record CreateCompanyResponseDTO(
    @JsonProperty("company_id") UUID companyId,
    @JsonProperty("name") String name,
    @JsonProperty("cnpj") String cnpj
) implements UseCaseOutputMapper<CreateCompanyUseCase.Output> {

    public static CreateCompanyResponseDTO from(final CreateCompanyUseCase.Output output) {
        return new CreateCompanyResponseDTO(output.companyId(), output.name(), output.cnpj());
    }
}
```

---

## GET Controller: GetByIdController

**Location:** `company/infrastructure/rest/GetByIdController.java`

```java
package br.com.logistics.tms.company.infrastructure.rest;

import br.com.logistics.tms.commons.application.annotations.Cqrs;
import br.com.logistics.tms.commons.application.annotations.DatabaseRole;
// ... (rest infrastructure imports, GetCompanyByIdUseCase, CompanyResponseDTO)

@RestController
@RequestMapping("companies")
@Cqrs(DatabaseRole.READ)
public class GetByIdController {

    private final GetCompanyByIdUseCase getCompanyByIdUseCase;
    private final DefaultRestPresenter defaultRestPresenter;
    private final RestUseCaseExecutor restUseCaseExecutor;

    public GetByIdController(final GetCompanyByIdUseCase getCompanyByIdUseCase,
                            final DefaultRestPresenter defaultRestPresenter,
                            final RestUseCaseExecutor restUseCaseExecutor) {
        this.getCompanyByIdUseCase = getCompanyByIdUseCase;
        this.defaultRestPresenter = defaultRestPresenter;
        this.restUseCaseExecutor = restUseCaseExecutor;
    }

    @GetMapping("{id}")
    public Object getById(@PathVariable("id") final UUID id) {
        return restUseCaseExecutor
            .from(getCompanyByIdUseCase)
            .withInput(new GetCompanyByIdUseCase.Input(id))
            .mapOutputTo(CompanyResponseDTO.class)
            .presentWith(output -> defaultRestPresenter.present(output, HttpStatus.OK.value()))
            .execute();
    }
}
```

### Response DTO

```java
package br.com.logistics.tms.company.infrastructure.dto;

import br.com.logistics.tms.commons.application.UseCaseOutputMapper;
// ... (GetCompanyByIdUseCase, CompanyType, JsonProperty, UUID)

public record CompanyResponseDTO(
    @JsonProperty("company_id") UUID companyId, @JsonProperty("name") String name,
    @JsonProperty("cnpj") String cnpj, @JsonProperty("types") Set<CompanyType> types,
    @JsonProperty("configuration") Map<String, Object> configuration
) implements UseCaseOutputMapper<GetCompanyByIdUseCase.Output> {

    public static CompanyResponseDTO from(final GetCompanyByIdUseCase.Output output) {
        return new CompanyResponseDTO(output.companyId(), output.name(), output.cnpj(),
                                     output.types(), output.configuration());
    }
}
```

---

## PUT Controller: UpdateController

**Location:** `company/infrastructure/rest/UpdateController.java`

```java
package br.com.logistics.tms.company.infrastructure.rest;

import br.com.logistics.tms.commons.application.annotations.Cqrs;
import br.com.logistics.tms.commons.application.annotations.DatabaseRole;
// ... (rest infrastructure imports, UpdateCompanyUseCase, DTOs)

@RestController
@RequestMapping("companies")
@Cqrs(DatabaseRole.WRITE)
public class UpdateController {

    private final UpdateCompanyUseCase updateCompanyUseCase;
    private final DefaultRestPresenter defaultRestPresenter;
    private final RestUseCaseExecutor restUseCaseExecutor;

    public UpdateController(final UpdateCompanyUseCase updateCompanyUseCase,
                           final DefaultRestPresenter defaultRestPresenter,
                           final RestUseCaseExecutor restUseCaseExecutor) {
        this.updateCompanyUseCase = updateCompanyUseCase;
        this.defaultRestPresenter = defaultRestPresenter;
        this.restUseCaseExecutor = restUseCaseExecutor;
    }

    @PutMapping("{id}")
    public Object update(@PathVariable("id") final UUID id, @RequestBody final UpdateCompanyDTO dto) {
        return restUseCaseExecutor
            .from(updateCompanyUseCase)
            .withInput(dto.toInput(id))
            .mapOutputTo(UpdateCompanyResponseDTO.class)
            .presentWith(output -> defaultRestPresenter.present(output, HttpStatus.OK.value()))
            .execute();
    }
}
```

---

## Controller Pattern Summary

### Key Components

1. **Controller Class**
   - `@RestController` + `@RequestMapping`
   - `@Cqrs(DatabaseRole.WRITE or READ)`
   - Constructor injection: Use case, presenter, executor

2. **Endpoint Method**
   - HTTP method annotation (`@PostMapping`, `@GetMapping`, etc.)
   - Parameters: `@RequestBody`, `@PathVariable`, `@RequestParam`
   - Returns `Object` (executor handles response)

3. **RestUseCaseExecutor Chain**
   ```java
   return restUseCaseExecutor
       .from(useCase)              // Specify use case
       .withInput(dto)             // Provide input
       .mapOutputTo(ResponseDTO)   // Map output
       .presentWith(presenter)     // Present result
       .execute();                 // Execute
   ```

4. **DTOs**
   - Request DTO: Implements `UseCaseInputMapper`
   - Response DTO: Implements `UseCaseOutputMapper`
   - Static `from()` method for mapping

### Rules

✅ **Zero Business Logic:** Controllers only delegate
✅ **Annotations Required:** `@Cqrs` on all controllers
✅ **Use Executor:** Always use `RestUseCaseExecutor`
✅ **Constructor Injection:** No field injection
✅ **DTOs:** Separate from domain objects
