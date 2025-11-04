# Complete Use Case Examples

This document shows complete use case implementations for both WRITE and READ operations.

---

## WRITE Use Case: CreateCompanyUseCase

**Location:** `company/application/usecases/CreateCompanyUseCase.java`

```java
package br.com.logistics.tms.company.application.usecases;

import br.com.logistics.tms.commons.application.UseCase;
import br.com.logistics.tms.commons.application.annotations.Cqrs;
import br.com.logistics.tms.commons.application.annotations.DatabaseRole;
import br.com.logistics.tms.commons.application.annotations.DomainService;
import br.com.logistics.tms.commons.exception.ValidationException;
import br.com.logistics.tms.company.application.repositories.CompanyRepository;
import br.com.logistics.tms.company.domain.Cnpj;
import br.com.logistics.tms.company.domain.Company;
import br.com.logistics.tms.company.domain.CompanyType;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Use case to create a new company.
 * WRITE operation - uses write database.
 */
@DomainService
@Cqrs(DatabaseRole.WRITE)
public class CreateCompanyUseCase implements UseCase<CreateCompanyUseCase.Input, CreateCompanyUseCase.Output> {

    private final CompanyRepository companyRepository;

    public CreateCompanyUseCase(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public Output execute(Input input) {
        // 1. Validate business rules
        validateInput(input);
        
        // 2. Check if company already exists
        if (companyRepository.getCompanyByCnpj(new Cnpj(input.cnpj())).isPresent()) {
            throw new ValidationException("Company with CNPJ " + input.cnpj() + " already exists");
        }
        
        // 3. Create aggregate via factory method (event is placed here)
        Company company = Company.createCompany(
            input.name(),
            input.cnpj(),
            input.types(),
            input.configuration()
        );
        
        // 4. Persist (repository saves entity AND events to outbox)
        company = companyRepository.create(company);
        
        // 5. Return output
        return new Output(
            company.getCompanyId().value(),
            company.getName(),
            company.getCnpj().value()
        );
    }

    private void validateInput(Input input) {
        if (input.name() == null || input.name().isBlank()) {
            throw new ValidationException("Company name is required");
        }
        if (input.cnpj() == null || input.cnpj().isBlank()) {
            throw new ValidationException("CNPJ is required");
        }
        if (input.types() == null || input.types().isEmpty()) {
            throw new ValidationException("At least one company type is required");
        }
    }

    /**
     * Input record for creating a company.
     */
    public record Input(
        String name,
        String cnpj,
        Set<CompanyType> types,
        Map<String, Object> configuration
    ) {}

    /**
     * Output record with created company details.
     */
    public record Output(
        UUID companyId,
        String name,
        String cnpj
    ) {}
}
```

---

## READ Use Case: GetCompanyByIdUseCase

**Location:** `company/application/usecases/GetCompanyByIdUseCase.java`

```java
package br.com.logistics.tms.company.application.usecases;

import br.com.logistics.tms.commons.application.UseCase;
import br.com.logistics.tms.commons.application.annotations.Cqrs;
import br.com.logistics.tms.commons.application.annotations.DatabaseRole;
import br.com.logistics.tms.commons.application.annotations.DomainService;
import br.com.logistics.tms.commons.exception.NotFoundException;
import br.com.logistics.tms.company.application.repositories.CompanyRepository;
import br.com.logistics.tms.company.domain.Company;
import br.com.logistics.tms.company.domain.CompanyId;
import br.com.logistics.tms.company.domain.CompanyType;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Use case to retrieve a company by ID.
 * READ operation - uses read replica database.
 */
@DomainService
@Cqrs(DatabaseRole.READ)
public class GetCompanyByIdUseCase implements UseCase<GetCompanyByIdUseCase.Input, GetCompanyByIdUseCase.Output> {

    private final CompanyRepository companyRepository;

    public GetCompanyByIdUseCase(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public Output execute(Input input) {
        // 1. Retrieve company
        Company company = companyRepository.getCompanyById(new CompanyId(input.companyId()))
            .orElseThrow(() -> new NotFoundException("Company not found with ID: " + input.companyId()));

        // 2. Map to output
        return new Output(
            company.getCompanyId().value(),
            company.getName(),
            company.getCnpj().value(),
            company.getTypes(),
            company.getConfiguration()
        );
    }

    /**
     * Input record with company ID to retrieve.
     */
    public record Input(UUID companyId) {}

    /**
     * Output record with company details.
     */
    public record Output(
        UUID companyId,
        String name,
        String cnpj,
        Set<CompanyType> types,
        Map<String, Object> configuration
    ) {}
}
```

---

## UPDATE Use Case: UpdateCompanyUseCase

**Location:** `company/application/usecases/UpdateCompanyUseCase.java`

```java
package br.com.logistics.tms.company.application.usecases;

import br.com.logistics.tms.commons.application.UseCase;
import br.com.logistics.tms.commons.application.annotations.Cqrs;
import br.com.logistics.tms.commons.application.annotations.DatabaseRole;
import br.com.logistics.tms.commons.application.annotations.DomainService;
import br.com.logistics.tms.commons.exception.NotFoundException;
import br.com.logistics.tms.commons.exception.ValidationException;
import br.com.logistics.tms.company.application.repositories.CompanyRepository;
import br.com.logistics.tms.company.domain.Company;
import br.com.logistics.tms.company.domain.CompanyId;

import java.util.UUID;

/**
 * Use case to update a company's name.
 * WRITE operation - demonstrates immutable update pattern.
 */
@DomainService
@Cqrs(DatabaseRole.WRITE)
public class UpdateCompanyUseCase implements UseCase<UpdateCompanyUseCase.Input, UpdateCompanyUseCase.Output> {

    private final CompanyRepository companyRepository;

    public UpdateCompanyUseCase(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public Output execute(Input input) {
        // 1. Validate input
        if (input.name() == null || input.name().isBlank()) {
            throw new ValidationException("Company name cannot be blank");
        }
        
        // 2. Retrieve existing company
        Company company = companyRepository.getCompanyById(new CompanyId(input.companyId()))
            .orElseThrow(() -> new NotFoundException("Company not found"));

        // 3. Update via immutable method (returns NEW instance with event)
        Company updatedCompany = company.updateName(input.name());

        // 4. Persist updated company (repository saves entity AND events)
        updatedCompany = companyRepository.update(updatedCompany);

        // 5. Return output
        return new Output(
            updatedCompany.getCompanyId().value(),
            updatedCompany.getName()
        );
    }

    public record Input(UUID companyId, String name) {}
    
    public record Output(UUID companyId, String name) {}
}
```

---

## Key Patterns

### WRITE Use Cases
✅ **Annotation:** `@DomainService` + `@Cqrs(DatabaseRole.WRITE)`
✅ **Validation:** Input validation at use case level
✅ **Immutability:** Always get new instance from aggregate methods
✅ **Events:** Placed by aggregate, saved by repository
✅ **Return:** Output record with relevant data

### READ Use Cases
✅ **Annotation:** `@DomainService` + `@Cqrs(DatabaseRole.READ)`
✅ **Read-Only:** Uses read replica database
✅ **No Events:** Read operations don't generate events
✅ **Mapping:** Map aggregate to output DTO

### Common Rules
✅ **Constructor Injection:** Only repositories injected
✅ **Single Responsibility:** One operation per use case
✅ **No Business Logic Leak:** Keep in domain layer
✅ **Nested Records:** Input/Output as public static records
