# Complete REST Controller Examples

This document shows complete REST controller implementations following TMS patterns.

---

## POST Controller: CreateController

**Location:** `company/infrastructure/rest/CreateController.java`

```java
package br.com.logistics.tms.company.infrastructure.rest;

import br.com.logistics.tms.commons.application.annotations.Cqrs;
import br.com.logistics.tms.commons.application.annotations.DatabaseRole;
import br.com.logistics.tms.commons.infrastructure.rest.DefaultRestPresenter;
import br.com.logistics.tms.commons.infrastructure.rest.RestUseCaseExecutor;
import br.com.logistics.tms.company.application.usecases.CreateCompanyUseCase;
import br.com.logistics.tms.company.infrastructure.dto.CreateCompanyDTO;
import br.com.logistics.tms.company.infrastructure.dto.CreateCompanyResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for creating companies.
 * Zero business logic - only delegation to use case.
 */
@RestController
@RequestMapping("companies")
@Cqrs(DatabaseRole.WRITE)
public class CreateController {

    private final CreateCompanyUseCase createCompanyUseCase;
    private final DefaultRestPresenter defaultRestPresenter;
    private final RestUseCaseExecutor restUseCaseExecutor;

    public CreateController(CreateCompanyUseCase createCompanyUseCase,
                           DefaultRestPresenter defaultRestPresenter,
                           RestUseCaseExecutor restUseCaseExecutor) {
        this.createCompanyUseCase = createCompanyUseCase;
        this.defaultRestPresenter = defaultRestPresenter;
        this.restUseCaseExecutor = restUseCaseExecutor;
    }

    /**
     * POST /companies
     * Creates a new company.
     */
    @PostMapping
    public Object create(@RequestBody CreateCompanyDTO dto) {
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
import br.com.logistics.tms.company.application.usecases.CreateCompanyUseCase;
import br.com.logistics.tms.company.domain.CompanyType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Set;

/**
 * DTO for creating a company.
 * Implements UseCaseInputMapper to convert to use case input.
 */
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
import br.com.logistics.tms.company.application.usecases.CreateCompanyUseCase;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Response DTO for created company.
 * Implements UseCaseOutputMapper to convert from use case output.
 */
public record CreateCompanyResponseDTO(
    @JsonProperty("company_id") UUID companyId,
    @JsonProperty("name") String name,
    @JsonProperty("cnpj") String cnpj
) implements UseCaseOutputMapper<CreateCompanyUseCase.Output> {

    public static CreateCompanyResponseDTO from(CreateCompanyUseCase.Output output) {
        return new CreateCompanyResponseDTO(
            output.companyId(),
            output.name(),
            output.cnpj()
        );
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
import br.com.logistics.tms.commons.infrastructure.rest.DefaultRestPresenter;
import br.com.logistics.tms.commons.infrastructure.rest.RestUseCaseExecutor;
import br.com.logistics.tms.company.application.usecases.GetCompanyByIdUseCase;
import br.com.logistics.tms.company.infrastructure.dto.CompanyResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for retrieving company by ID.
 * READ operation - uses read replica.
 */
@RestController
@RequestMapping("companies")
@Cqrs(DatabaseRole.READ)
public class GetByIdController {

    private final GetCompanyByIdUseCase getCompanyByIdUseCase;
    private final DefaultRestPresenter defaultRestPresenter;
    private final RestUseCaseExecutor restUseCaseExecutor;

    public GetByIdController(GetCompanyByIdUseCase getCompanyByIdUseCase,
                            DefaultRestPresenter defaultRestPresenter,
                            RestUseCaseExecutor restUseCaseExecutor) {
        this.getCompanyByIdUseCase = getCompanyByIdUseCase;
        this.defaultRestPresenter = defaultRestPresenter;
        this.restUseCaseExecutor = restUseCaseExecutor;
    }

    /**
     * GET /companies/{id}
     * Retrieves company details by ID.
     */
    @GetMapping("{id}")
    public Object getById(@PathVariable("id") UUID id) {
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
import br.com.logistics.tms.company.application.usecases.GetCompanyByIdUseCase;
import br.com.logistics.tms.company.domain.CompanyType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record CompanyResponseDTO(
    @JsonProperty("company_id") UUID companyId,
    @JsonProperty("name") String name,
    @JsonProperty("cnpj") String cnpj,
    @JsonProperty("types") Set<CompanyType> types,
    @JsonProperty("configuration") Map<String, Object> configuration
) implements UseCaseOutputMapper<GetCompanyByIdUseCase.Output> {

    public static CompanyResponseDTO from(GetCompanyByIdUseCase.Output output) {
        return new CompanyResponseDTO(
            output.companyId(),
            output.name(),
            output.cnpj(),
            output.types(),
            output.configuration()
        );
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
import br.com.logistics.tms.commons.infrastructure.rest.DefaultRestPresenter;
import br.com.logistics.tms.commons.infrastructure.rest.RestUseCaseExecutor;
import br.com.logistics.tms.company.application.usecases.UpdateCompanyUseCase;
import br.com.logistics.tms.company.infrastructure.dto.UpdateCompanyDTO;
import br.com.logistics.tms.company.infrastructure.dto.UpdateCompanyResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for updating companies.
 */
@RestController
@RequestMapping("companies")
@Cqrs(DatabaseRole.WRITE)
public class UpdateController {

    private final UpdateCompanyUseCase updateCompanyUseCase;
    private final DefaultRestPresenter defaultRestPresenter;
    private final RestUseCaseExecutor restUseCaseExecutor;

    public UpdateController(UpdateCompanyUseCase updateCompanyUseCase,
                           DefaultRestPresenter defaultRestPresenter,
                           RestUseCaseExecutor restUseCaseExecutor) {
        this.updateCompanyUseCase = updateCompanyUseCase;
        this.defaultRestPresenter = defaultRestPresenter;
        this.restUseCaseExecutor = restUseCaseExecutor;
    }

    /**
     * PUT /companies/{id}
     * Updates company name.
     */
    @PutMapping("{id}")
    public Object update(@PathVariable("id") UUID id,
                        @RequestBody UpdateCompanyDTO dto) {
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
