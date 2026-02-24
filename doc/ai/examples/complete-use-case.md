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
// ... (other imports: ValidationException, CompanyRepository, domain types)

@DomainService
@Cqrs(DatabaseRole.WRITE)
public class CreateCompanyUseCase implements UseCase<CreateCompanyUseCase.Input, CreateCompanyUseCase.Output> {

    private final CompanyRepository companyRepository;

    public CreateCompanyUseCase(final CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public Output execute(final Input input) {
        validateInput(input);
        
        if (companyRepository.getCompanyByCnpj(new Cnpj(input.cnpj())).isPresent()) {
            throw new ValidationException("Company with CNPJ " + input.cnpj() + " already exists");
        }
        
        Company company = Company.createCompany(input.name(), input.cnpj(),
                                               input.types(), input.configuration());
        company = companyRepository.create(company);
        
        return new Output(company.getCompanyId().value(), company.getName(), company.getCnpj().value());
    }

    private void validateInput(final Input input) {
        if (input.name() == null || input.name().isBlank()) {
            throw new ValidationException("Company name is required");
        }
        // ... (cnpj, types validation)
    }

    public record Input(String name, String cnpj, Set<CompanyType> types,
                       Map<String, Object> configuration) {}
    public record Output(UUID companyId, String name, String cnpj) {}
}
```

---

## READ Use Case: GetCompanyByIdUseCase

**Location:** `company/application/usecases/GetCompanyByIdUseCase.java`

```java
package br.com.logistics.tms.company.application.usecases;

import br.com.logistics.tms.commons.application.UseCase;
// ... (annotations, NotFoundException, CompanyRepository, domain types)

@DomainService
@Cqrs(DatabaseRole.READ)
public class GetCompanyByIdUseCase implements UseCase<GetCompanyByIdUseCase.Input, GetCompanyByIdUseCase.Output> {

    private final CompanyRepository companyRepository;

    public GetCompanyByIdUseCase(final CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public Output execute(final Input input) {
        final Company company = companyRepository.getCompanyById(new CompanyId(input.companyId()))
            .orElseThrow(() -> new NotFoundException("Company not found with ID: " + input.companyId()));

        return new Output(company.getCompanyId().value(), company.getName(), company.getCnpj().value(),
                         company.getTypes(), company.getConfiguration());
    }

    public record Input(UUID companyId) {}
    public record Output(UUID companyId, String name, String cnpj, Set<CompanyType> types,
                        Map<String, Object> configuration) {}
}
```

---

## UPDATE Use Case: UpdateCompanyUseCase

**Location:** `company/application/usecases/UpdateCompanyUseCase.java`

```java
package br.com.logistics.tms.company.application.usecases;

import br.com.logistics.tms.commons.application.UseCase;
// ... (annotations, exceptions, CompanyRepository, domain types)

@DomainService
@Cqrs(DatabaseRole.WRITE)
public class UpdateCompanyUseCase implements UseCase<UpdateCompanyUseCase.Input, UpdateCompanyUseCase.Output> {

    private final CompanyRepository companyRepository;

    public UpdateCompanyUseCase(final CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public Output execute(final Input input) {
        if (input.name() == null || input.name().isBlank()) {
            throw new ValidationException("Company name cannot be blank");
        }
        
        Company company = companyRepository.getCompanyById(new CompanyId(input.companyId()))
            .orElseThrow(() -> new NotFoundException("Company not found"));
        Company updatedCompany = company.updateName(input.name());
        updatedCompany = companyRepository.update(updatedCompany);

        return new Output(updatedCompany.getCompanyId().value(), updatedCompany.getName());
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
