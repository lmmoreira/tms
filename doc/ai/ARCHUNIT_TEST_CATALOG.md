# ArchUnit Test Catalog

**Complete reference of all ArchUnit tests in the TMS project**

---

## 📖 About This Document

This catalog documents all existing ArchUnit tests organized by category. Use this as a reference when:
- Understanding what's already tested
- Avoiding duplicate tests
- Finding examples for new tests
- Debugging test failures

---

## 🗂️ Test Files Overview

| File | Purpose | Tests Count |
|------|---------|-------------|
| `LayerArchitectureTest.java` | Layer boundaries & dependencies | 13 |
| `AnnotationRulesTest.java` | Required annotations | 12 |
| `ImmutabilityRulesTest.java` | Immutability enforcement | 8 |
| `ValueObjectRulesTest.java` | Value object patterns | 7 |
| `EventRulesTest.java` | Domain event rules | 11 |
| `RepositoryRulesTest.java` | Repository patterns | 8 |
| `ModuleIsolationTest.java` | Module independence | 4 |
| `DependencyRulesTest.java` | Dependency management | 13 |
| `NamingConventionTest.java` | Naming standards | 13 |
| `InfrastructureRulesTest.java` | Infrastructure patterns | 13 |

**Utility Classes:**
- `ArchUnitConditions.java` - Reusable custom conditions
- `ArchUnitPredicates.java` - Reusable predicates

---

## 📊 Tests by Category

### 1. Layer Architecture Tests (`LayerArchitectureTest.java`)

**Purpose:** Enforce strict separation between domain, application, and infrastructure layers.

| Test | What It Checks |
|------|----------------|
| `domainLayerShouldNotDependOnSpring()` | Domain has no Spring dependencies |
| `domainLayerShouldNotDependOnJPA()` | Domain has no JPA dependencies |
| `domainLayerShouldNotDependOnJackson()` | Domain has no Jackson dependencies |
| `domainLayerShouldNotDependOnRabbitMQ()` | Domain has no RabbitMQ dependencies |
| `domainLayerShouldNotDependOnHibernate()` | Domain has no Hibernate dependencies |
| `applicationLayerShouldNotDependOnSpring()` | Application has no Spring dependencies |
| `applicationLayerShouldNotDependOnJPA()` | Application has no JPA dependencies |
| `applicationLayerShouldNotDependOnJackson()` | Application has no Jackson dependencies |
| `applicationLayerShouldNotDependOnHttp()` | Application has no HTTP dependencies |
| `domainLayerShouldNotDependOnApplicationLayer()` | Domain doesn't depend on application |
| `domainLayerShouldNotDependOnInfrastructureLayer()` | Domain doesn't depend on infrastructure |
| `applicationLayerShouldNotDependOnInfrastructureLayer()` | Application doesn't depend on infrastructure |
| `layeredArchitectureShouldBeRespected()` | Overall layered architecture integrity |

**Key Pattern:** Pure hexagonal architecture with strict boundaries.

---

### 2. Annotation Rules Tests (`AnnotationRulesTest.java`)

**Purpose:** Ensure required annotations are present on components.

| Test | What It Checks |
|------|----------------|
| `useCasesShouldBeAnnotatedWithDomainService()` | Use cases have `@DomainService` |
| `useCasesShouldBeAnnotatedWithCqrs()` | Use cases have `@Cqrs` |
| `controllersShouldBeAnnotatedWithRestController()` | Controllers have `@RestController` |
| `controllersShouldBeAnnotatedWithCqrs()` | Controllers have `@Cqrs` |
| `listenersShouldBeAnnotatedWithComponent()` | Listeners have `@Component` |
| `listenersShouldBeAnnotatedWithLazyFalse()` | Listeners have `@Lazy(false)` |
| `listenersShouldBeAnnotatedWithCqrs()` | Listeners have `@Cqrs` |
| `repositoryImplementationsShouldBeAnnotatedWithComponent()` | Repository impls have `@Component` |
| `domainClassesShouldNotHaveSpringAnnotations()` | Domain classes are Spring-free |
| `domainClassesShouldNotHaveJpaAnnotations()` | Domain classes are JPA-free |
| `applicationClassesShouldNotHaveJpaAnnotations()` | Application classes are JPA-free |
| `repositoryCreateMethodsShouldBeTransactional()` | Repository methods NOT `@Transactional` (managed by use cases) |
| `notClassesAreAllowedToHaveSl4fAnnotation()` | No `@Slf4j` annotation (telemetry handles logs) |

**Key Pattern:** Annotations enforce architectural boundaries.

---

### 3. Immutability Rules Tests (`ImmutabilityRulesTest.java`)

**Purpose:** Enforce immutability in domain layer.

| Test | What It Checks | Uses Condition |
|------|----------------|----------------|
| `aggregatesShouldNotHaveSetters()` | Aggregates have no setters | `haveSetters()` |
| `aggregateFieldsShouldBeFinal()` | Aggregate fields are final | - |
| `valueObjectsShouldNotHaveSetters()` | Value objects (records) have no setters | `haveSetters()` |
| `valueObjectFieldsShouldBeFinal()` | Value object fields are final | - |
| `domainClassesShouldNotHaveSetters()` | All domain classes have no setters | `haveSetters()` |
| `updateMethodsShouldReturnSameType()` | Update methods return new instance | `returnTheSameClassAsDeclaring()` |
| `idValueObjectsShouldBeRecords()` | ID value objects are records | - |
| `domainEventsShouldBeImmutable()` | Event fields are final | - |
| `domainEventsShouldNotHaveSetters()` | Events have no setters | `haveSetters()` |

**Key Pattern:** Immutability via final fields, no setters, update methods return new instances.

---

### 4. Value Object Rules Tests (`ValueObjectRulesTest.java`)

**Purpose:** Enforce value object patterns and conventions.

| Test | What It Checks | Uses Condition/Predicate |
|------|----------------|--------------------------|
| `valueObjectsShouldBeRecords()` | VOs are Java records | `matchSimpleNamePattern()` |
| `idValueObjectsShouldBeNamedWithIdSuffix()` | ID VOs end with "Id" | - |
| `valueObjectRecordsShouldHaveCompactConstructor()` | Records use compact constructor | - |
| `idValueObjectsShouldHaveUniqueFactoryMethod()` | IDs have `unique()` method | `haveStaticMethodNamed()` |
| `idValueObjectsShouldHaveWithFactoryMethod()` | IDs have `with()` method | Custom condition |
| `cnpjValueObjectShouldBeRecord()` | Cnpj is a record | - |
| `configurationsValueObjectShouldBeRecord()` | Configurations is a record | - |

**Key Pattern:** Value objects as immutable records with factory methods.

---

### 5. Event Rules Tests (`EventRulesTest.java`)

**Purpose:** Enforce domain event patterns and naming conventions.

| Test | What It Checks | Uses Condition |
|------|----------------|----------------|
| `eventsShouldBeNamedInPastTense()` | Events end with Created/Updated/Deleted/Event | - |
| `eventsShouldExtendAbstractDomainEvent()` | Events extend `AbstractDomainEvent` | - |
| `eventsShouldBeInDomainPackage()` | Events in `*.domain` package | - |
| `eventFieldsShouldBeFinal()` | Event fields are final | - |
| `eventsShouldNotHaveSetters()` | Events have no setters | Custom condition |
| `eventsShouldHaveGetters()` | Events have getters | Custom condition |
| `createdEventsShouldFollowNamingPattern()` | Created events follow `{Entity}Created` | `matchSimpleNamePattern()` |
| `updatedEventsShouldFollowNamingPattern()` | Updated events follow `{Entity}Updated` | `matchSimpleNamePattern()` |
| `deletedEventsShouldFollowNamingPattern()` | Deleted events follow `{Entity}Deleted` | `matchSimpleNamePattern()` |

**Key Pattern:** Events are immutable, past-tense named, with clear patterns.

---

### 6. Repository Rules Tests (`RepositoryRulesTest.java`)

**Purpose:** Enforce repository patterns and boundaries.

| Test | What It Checks |
|------|----------------|
| `repositoryInterfacesShouldBeInApplicationLayer()` | Interfaces in `*.application.repositories` |
| `repositoryImplementationsShouldBeInInfrastructureLayer()` | Implementations in `*.infrastructure.repositories` |
| `repositoryImplementationsShouldImplementInterface()` | Implementations implement interfaces |
| `repositoryMethodsShouldNotBeTransactional()` | Methods NOT `@Transactional` (use case manages) |
| `repositoryInterfacesShouldBePublic()` | Interfaces are public |
| `repositoryImplementationsShouldHaveFinalFields()` | Implementation fields are final |
| `repositoryImplementationsShouldNotAccessOtherRepositories()` | No cross-module repository calls |

**Key Pattern:** Repository interface in application, implementation in infrastructure.

---

### 7. Module Isolation Tests (`ModuleIsolationTest.java`)

**Purpose:** Enforce module independence and event-driven communication.

| Test | What It Checks |
|------|----------------|
| `companyModuleShouldNotDependOnShipmentOrderModule()` | Company doesn't depend on ShipmentOrder |
| `shipmentOrderModuleShouldNotDependOnCompanyModule()` | ShipmentOrder doesn't depend on Company |
| `noModuleShouldAccessAnotherModules()` | Modules communicate only via events |
| `commonsModuleMayBeAccessedByAllModulesAndCantDependeOnIt()` | Commons is shared, depends on nothing |

**Key Pattern:** Modules communicate via events, not direct calls.

---

### 8. Dependency Rules Tests (`DependencyRulesTest.java`)

**Purpose:** Enforce dependency management and injection patterns.

| Test | What It Checks | Uses Condition |
|------|----------------|----------------|
| `domainPackageShouldBeFreeOfCycles()` | No cyclic dependencies in domain | - |
| `applicationPackageShouldBeFreeOfCycles()` | No cyclic dependencies in application | - |
| `modulesShouldBeFreeOfCycles()` | No cyclic dependencies between modules | - |
| `noFieldInjection()` | No field injection (constructor only) | Custom condition |
| `injectedFieldsMustBeFinal()` | Injected fields are final | - |
| `useCaseFieldsMustBeFinal()` | Use case fields are final | - |
| `repositoryFieldsMustBeFinal()` | Repository fields are final | - |
| `listenerFieldsMustBeFinal()` | Listener fields are final | - |
| `controllerFieldsMustBeFinal()` | Controller fields are final | - |
| `useCasesShouldImplementUseCaseInterface()` | Use cases implement UseCase interface | - |
| `noClassesShouldInjectObjectMapper()` | No `ObjectMapper` injection (use JsonSingleton) | - |
| `repositoryImplementationsShouldImplementRepositoryInterface()` | Impls match interfaces | Custom condition |
| `applicationLayerShouldOnlyDependOnDomainAndCommons()` | Application depends on domain/commons only | - |
| `domainLayerShouldOnlyDependOnDomainAndCommons()` | Domain depends on commons domain only | - |

**Key Pattern:** Constructor injection, final fields, no cycles, explicit dependencies.

---

### 9. Naming Convention Tests (`NamingConventionTest.java`)

**Purpose:** Enforce consistent naming across the codebase.

| Test | What It Checks | Uses Condition |
|------|----------------|----------------|
| `useCasesShouldBeNamedCorrectly()` | Use cases end with "UseCase" | - |
| `useCasesShouldBeInCorrectPackage()` | Use cases in `*.application.usecases` | - |
| `controllersShouldBeNamedCorrectly()` | Controllers end with "Controller" | - |
| `controllersShouldBeInCorrectPackage()` | Controllers in `*.infrastructure.rest` | - |
| `repositoryInterfacesShouldBeInApplicationLayer()` | Repository interfaces in application | - |
| `repositoryImplementationsShouldBeInInfrastructureLayer()` | Repository impls in infrastructure | - |
| `domainEventsShouldExtendAbstractDomainEvent()` | Events extend `AbstractDomainEvent` | - |
| `domainEventsShouldBeInDomainPackage()` | Events in `*.domain` | - |
| `aggregatesShouldExtendAbstractAggregateRoot()` | Aggregates extend `AbstractAggregateRoot` | - |
| `jpaEntitiesShouldBeInCorrectPackage()` | JPA entities in `*.infrastructure.jpa.entities` | - |
| `jpaEntitiesShouldBeNamedWithEntitySuffix()` | JPA entities end with "Entity" | - |
| `listenersShouldBeNamedCorrectly()` | Listeners end with "Listener" | - |
| `listenersShouldBeInCorrectPackage()` | Listeners in `*.infrastructure.listener` | - |
| `dtosShouldBeInInfrastructureDto()` | DTOs in `*.infrastructure.dto` | - |
| `repositoryImplementationsShouldBeNamedCorrectly()` | Impls end with "RepositoryImpl" or "JpaRepository" | `haveSimpleNameEndingWithAny()` |

**Key Pattern:** Consistent, predictable naming for all components.

---

### 10. Infrastructure Rules Tests (`InfrastructureRulesTest.java`)

**Purpose:** Enforce infrastructure layer patterns.

| Test | What It Checks | Uses Condition |
|------|----------------|----------------|
| `jpaEntitiesShouldBeInCorrectPackage()` | JPA entities in `*.jpa.entities` | - |
| `jpaEntitiesShouldHaveEntityAnnotation()` | Entities have `@Entity` | - |
| `dtosShouldBeInInfrastructureDto()` | DTOs in `*.infrastructure.dto` | - |
| `dtosShouldBeRecords()` | DTOs are records | - |
| `controllersShouldNotContainBusinessLogic()` | Controllers return Object/ResponseEntity | - |
| `jpaEntitiesShouldHaveTableAnnotation()` | Entities have `@Table` | - |
| `listenerMethodsShouldHaveRabbitListenerAnnotation()` | Listener methods have `@RabbitListener` | - |
| `controllersShouldUsePostMappingOrGetMapping()` | Controller methods have HTTP mappings | - |
| `jpaRepositoriesShouldExtendJpaRepository()` | JPA repos extend Spring Data JpaRepository | - |
| `infrastructureClassesShouldNotAccessDomainDirectly()` | Infrastructure doesn't manipulate aggregates | - |
| `controllersShouldHaveRestUseCaseExecutor()` | Controllers use RestUseCaseExecutor | `haveFieldOfTypeContaining()` |
| `listenersShouldHaveVoidUseCaseExecutor()` | Listeners use VoidUseCaseExecutor | `haveFieldOfTypeExactly()` |
| `jpaEntitiesShouldHaveIdAnnotation()` | Entities have field with `@Id` | `haveFieldAnnotatedWith()` |

**Key Pattern:** Infrastructure delegates to use cases, no business logic in controllers/listeners.

---

## 🛠️ Utility Classes

### ArchUnitConditions.java

Reusable custom conditions for common checks:

| Method | Purpose | Used In |
|--------|---------|---------|
| `matchSimpleNamePattern(regex)` | Check class name matches regex | EventRulesTest, ValueObjectRulesTest |
| `haveSetters()` | Check for setter methods | ImmutabilityRulesTest |
| `returnTheSameClassAsDeclaring()` | Check method returns same type | ImmutabilityRulesTest |
| `haveStaticMethodNamed(name)` | Check for static method | ValueObjectRulesTest |
| `haveMethodNamed(name)` | Check for instance method | - |
| `haveFieldOfTypeContaining(fragments...)` | Check field type (partial) | InfrastructureRulesTest |
| `haveFieldOfTypeExactly(type)` | Check field type (exact) | InfrastructureRulesTest |
| `haveFieldAnnotatedWith(annotation)` | Check field annotation | InfrastructureRulesTest |
| `haveSimpleNameEndingWithAny(suffixes...)` | Check multiple suffixes | NamingConventionTest |

### ArchUnitPredicates.java

Reusable predicates for filtering:

| Method | Purpose | Used In |
|--------|---------|---------|
| `matchSimpleNamePattern(regex)` | Filter by name pattern | ValueObjectRulesTest, EventRulesTest |

---

## 📈 Test Statistics

- **Total Test Classes:** 10
- **Total Tests:** ~102
- **Utility Classes:** 2
- **Reusable Conditions:** 9
- **Reusable Predicates:** 1

---

## 🔍 Finding Tests

### By Architectural Concern

| Concern | Test File |
|---------|-----------|
| Layer boundaries | `LayerArchitectureTest` |
| Immutability | `ImmutabilityRulesTest` |
| Module isolation | `ModuleIsolationTest` |
| Naming standards | `NamingConventionTest` |
| Dependency injection | `DependencyRulesTest` |
| Domain events | `EventRulesTest` |
| Value objects | `ValueObjectRulesTest` |
| Repositories | `RepositoryRulesTest` |
| Annotations | `AnnotationRulesTest` |
| Infrastructure | `InfrastructureRulesTest` |

### By Component Type

| Component | Test Files |
|-----------|------------|
| Aggregates | `ImmutabilityRulesTest`, `NamingConventionTest` |
| Use Cases | `AnnotationRulesTest`, `DependencyRulesTest`, `NamingConventionTest` |
| Controllers | `AnnotationRulesTest`, `InfrastructureRulesTest`, `NamingConventionTest` |
| Repositories | `RepositoryRulesTest`, `NamingConventionTest`, `AnnotationRulesTest` |
| Events | `EventRulesTest`, `ImmutabilityRulesTest`, `NamingConventionTest` |
| Value Objects | `ValueObjectRulesTest`, `ImmutabilityRulesTest` |
| Listeners | `AnnotationRulesTest`, `InfrastructureRulesTest`, `NamingConventionTest` |
| DTOs | `InfrastructureRulesTest`, `NamingConventionTest` |
| JPA Entities | `InfrastructureRulesTest`, `NamingConventionTest` |

---

## 🎓 Best Practices from Existing Tests

### 1. Clear Test Names
✅ Good: `aggregatesShouldNotHaveSetters()`  
❌ Bad: `testAggregates()`

### 2. Use Utility Classes
✅ Reuse conditions from `ArchUnitConditions`  
❌ Duplicate condition logic

### 3. Descriptive "because" Clauses
✅ `.because("Aggregates must be immutable - no setters allowed")`  
❌ `.because("rule")`

### 4. Test One Concern
✅ Separate tests for different rules  
❌ One giant test checking everything

### 5. Use Final Variables
✅ `final ArchRule rule = ...`  
❌ `ArchRule rule = ...`

### 6. Document Test Purpose
✅ JavaDoc with rules list  
❌ No documentation

---

## 🚀 Running Tests

```bash
# Run all architecture tests
mvn test -Dtest="br.com.logistics.tms.architecture.*"

# Run specific test class
mvn test -Dtest=LayerArchitectureTest

# Run specific test method
mvn test -Dtest=LayerArchitectureTest#domainLayerShouldNotDependOnSpring

# Run with verbose output
mvn test -Dtest=LayerArchitectureTest -X
```

---

## 📝 Adding New Tests

**Before creating a new test:**

1. ✅ Check if similar test already exists in this catalog
2. ✅ Check if needed condition exists in `ArchUnitConditions`
3. ✅ Choose appropriate test file based on category
4. ✅ Follow naming conventions from existing tests
5. ✅ Add JavaDoc with clear rules description
6. ✅ Test compilation: `mvn test-compile`
7. ✅ Run test: `mvn test -Dtest=YourTest`

**If creating a reusable condition:**

1. Add to `ArchUnitConditions.java`
2. Add JavaDoc explaining purpose
3. Update this catalog

---

## 🔗 Related Documentation

- [ARCHUNIT_GUIDELINES.md](./ARCHUNIT_GUIDELINES.md) - How to write ArchUnit tests
- [ARCHITECTURE.md](./ARCHITECTURE.md) - Overall architecture documentation
- [QUICK_REFERENCE.md](./QUICK_REFERENCE.md) - Quick patterns reference

---

**Last Updated:** 2025-11-06  
**Maintained By:** Development Team
