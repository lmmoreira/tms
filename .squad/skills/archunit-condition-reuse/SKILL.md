# ArchUnit Condition Reuse Pattern

## ⚡ TL;DR

- **When:** Writing ArchUnit architecture tests
- **Why:** Reuse conditions, avoid duplicating DescribedPredicate implementations
- **Pattern:** `import static br.com.logistics.tms.architecture.ArchUnitConditions.*;`
- **Confidence:** Medium (utilities exist, usage sparse - only 6 imports found across 5 test files)

---

## Available Utilities

### ArchUnitConditions

**Location:** `src/test/java/br/com/logistics/tms/architecture/ArchUnitConditions.java`

Custom ArchUnit conditions for `.should()` clauses. All methods return `ArchCondition<JavaClass>` unless noted.

#### `haveSetters()`

Checks if a class has setter methods (methods starting with "set" and taking 1 parameter).

```java
import static br.com.logistics.tms.architecture.ArchUnitConditions.*;

@Test
void aggregatesShouldNotHaveSetters() {
    noClasses()
        .that().resideInAPackage("..domain..")
        .should(haveSetters())  // ✅ Reused condition
        .because("aggregates must be immutable")
        .check(classes);
}
```

#### `haveStaticMethodNamed(String methodName)`

Checks if a class has a static method with the specified name.

```java
@Test
void idValueObjectsShouldHaveUniqueMethod() {
    classes()
        .that().haveSimpleNameEndingWith("Id")
        .should(haveStaticMethodNamed("unique"))  // ✅ Reused
        .because("ID value objects need unique() factory")
        .check(classes);
}
```

#### `haveMethodNamed(String methodName)`

Checks if a class has an instance method with the specified name (not necessarily static).

```java
@Test
void jpaEntitiesShouldHaveToDomain() {
    classes()
        .that().areAnnotatedWith("jakarta.persistence.Entity")
        .should(haveMethodNamed("toDomain"))  // ✅ Reused
        .because("JPA entities need toDomain() converter")
        .check(classes);
}
```

#### `haveFieldOfTypeContaining(String... typeFragments)`

Checks if a class has a field whose type name contains any of the specified fragments (partial match).

```java
@Test
void controllersShouldUseExecutors() {
    classes()
        .that().haveSimpleNameEndingWith("Controller")
        .should(haveFieldOfTypeContaining("RestUseCaseExecutor", "RestPresenter"))  // ✅ Reused
        .because("Controllers delegate to executors")
        .check(classes);
}
```

#### `haveFieldOfTypeExactly(String typeName)`

Checks if a class has a field of the exact type specified (fully qualified or simple name).

```java
@Test
void listenersShouldUseVoidExecutor() {
    classes()
        .that().haveSimpleNameEndingWith("Listener")
        .should(haveFieldOfTypeExactly("VoidUseCaseExecutor"))  // ✅ Reused
        .because("Listeners use VoidUseCaseExecutor")
        .check(classes);
}
```

#### `haveFieldAnnotatedWith(String annotationName)`

Checks if a class has at least one field annotated with the specified annotation.

```java
@Test
void jpaEntitiesShouldHaveIdField() {
    classes()
        .that().areAnnotatedWith("jakarta.persistence.Entity")
        .should(haveFieldAnnotatedWith("jakarta.persistence.Id"))  // ✅ Reused
        .because("JPA entities need @Id field")
        .check(classes);
}
```

#### `haveSimpleNameEndingWithAny(String... suffixes)`

Checks if a class name ends with any of the specified suffixes.

```java
@Test
void repositoryImplementationsShouldFollowNaming() {
    classes()
        .that().resideInAPackage("..infrastructure.repositories..")
        .should(haveSimpleNameEndingWithAny("RepositoryImpl", "JpaRepository"))  // ✅ Reused
        .because("Repository implementations follow naming conventions")
        .check(classes);
}
```

#### `matchSimpleNamePattern(String regex)`

Checks if a class name matches the specified regex pattern. Can be used as **both** condition and predicate.

```java
@Test
void eventsShouldFollowNamingPattern() {
    classes()
        .that().resideInAPackage("..domain..")
        .and().haveSimpleNameEndingWith("Created")
        .should(matchSimpleNamePattern("^[A-Z][a-zA-Z]*Created$"))  // ✅ Reused as condition
        .because("Events follow {Entity}Created pattern")
        .check(classes);
}
```

#### `returnTheSameClassAsDeclaring()` → Returns `ArchCondition<JavaMethod>`

Checks if methods return the same type as their declaring class (for immutability pattern).

```java
@Test
void updateMethodsShouldReturnNewInstance() {
    methods()
        .that().haveNameStartingWith("update")
        .and().areDeclaredInClassesThat().areAssignableTo(AbstractAggregateRoot.class)
        .should(returnTheSameClassAsDeclaring())  // ✅ Reused
        .because("Update methods return new instance (immutability)")
        .check(classes);
}
```

---

### ArchUnitPredicates

**Location:** `src/test/java/br/com/logistics/tms/architecture/ArchUnitPredicates.java`

Custom predicates for `.that()` clauses to filter classes before applying rules.

#### `matchSimpleNamePattern(String regex)` → Returns `DescribedPredicate<JavaClass>`

Filters classes whose simple name matches the specified regex pattern.

```java
import static br.com.logistics.tms.architecture.ArchUnitPredicates.*;

@Test
void createdEventsShouldBeInDomain() {
    classes()
        .that(matchSimpleNamePattern("^[A-Z][a-zA-Z]*Created$"))  // ✅ Reused as predicate
        .should().resideInAPackage("..domain..")
        .andShould().beAssignableTo(AbstractDomainEvent.class)
        .because("Created events must be in domain package")
        .check(classes);
}
```

---

## When to Add New Conditions

### ✅ Add to ArchUnitConditions when:

- Same check needed in **3+ tests**
- Complex logic worth centralizing
- Improves readability significantly
- Pattern is reusable across modules

### ❌ Keep inline when:

- One-off check
- Test-specific logic
- Simple predicate (1-2 lines)

---

## Pattern: Using Imported Conditions

```java
package br.com.logistics.tms.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static br.com.logistics.tms.architecture.ArchUnitConditions.*;
import static br.com.logistics.tms.architecture.ArchUnitPredicates.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

class MyArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setup() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("br.com.logistics.tms");
    }

    @Test
    void idValueObjectsShouldHaveFactories() {
        classes()
            .that().haveSimpleNameEndingWith("Id")
            .should(haveStaticMethodNamed("unique"))  // ✅ Reused
            .andShould(haveStaticMethodNamed("with"))  // ✅ Reused
            .because("ID value objects need factory methods")
            .check(classes);
    }
}
```

---

## Real Usage in TMS Codebase

Based on grep results, utilities are used in:

1. **NamingConventionTest.java**
   - Uses: `haveSimpleNameEndingWithAny`

2. **EventRulesTest.java**
   - Uses: `matchSimpleNamePattern`

3. **InfrastructureRulesTest.java**
   - Uses: `haveFieldOfTypeContaining`, `haveStaticMethodNamed`, `haveMethodNamed`, `haveFieldOfTypeExactly`

4. **ValueObjectRulesTest.java**
   - Uses: `haveStaticMethodNamed`, `matchSimpleNamePattern` (predicate)

5. **ImmutabilityRulesTest.java**
   - Uses: `haveSetters`, `returnTheSameClassAsDeclaring`

---

## Combining Utilities with Standard ArchUnit

```java
// ✅ Combine utility conditions with standard ArchUnit methods
classes()
    .that().resideInAPackage("..domain..")
    .should(haveSetters())  // ✅ Utility
    .andShould().haveOnlyFinalFields()  // Standard ArchUnit

// ✅ Chain multiple utility conditions
classes()
    .that().haveSimpleNameEndingWith("Id")
    .should(haveStaticMethodNamed("unique"))  // ✅ Utility
    .andShould(haveStaticMethodNamed("with"))  // ✅ Utility
```

---

## Anti-Patterns

### ❌ DON'T duplicate condition code

```java
// ❌ WRONG - duplicating what's already in ArchUnitConditions
private static ArchCondition<JavaClass> haveSetters() {
    return new ArchCondition<>("have setters") {
        @Override
        public void check(JavaClass javaClass, ConditionEvents conditionEvents) {
            // Duplicated implementation!
        }
    };
}
```

### ❌ DON'T forget to import

```java
// ❌ WRONG - won't compile without import
@Test
void test() {
    classes().should(haveSetters())  // Compilation error!
}

// ✅ CORRECT - import first
import static br.com.logistics.tms.architecture.ArchUnitConditions.*;

@Test
void test() {
    classes().should(haveSetters())  // Works!
}
```

---

## Finding the Right Utility

| Need to Check... | Use This | Type |
|------------------|----------|------|
| Setter methods exist | `haveSetters()` | Condition |
| Static method exists | `haveStaticMethodNamed(name)` | Condition |
| Instance method exists | `haveMethodNamed(name)` | Condition |
| Field type (partial match) | `haveFieldOfTypeContaining(fragments...)` | Condition |
| Field type (exact match) | `haveFieldOfTypeExactly(type)` | Condition |
| Field has annotation | `haveFieldAnnotatedWith(annotation)` | Condition |
| Name ends with options | `haveSimpleNameEndingWithAny(suffixes...)` | Condition |
| Name matches regex | `matchSimpleNamePattern(regex)` | Condition or Predicate |
| Method returns self | `returnTheSameClassAsDeclaring()` | Condition |

---

## Metadata

**Confidence:** `medium`  
**Applies To:** `[architecture tests, ArchUnit, test utilities]`  
**Replaces:** `[ARCHUNIT_CHEATSHEET.md § ArchUnit Utilities section]`  
**Token Cost:** `~120 lines`  
**Related Skills:** `[]`  
**Created:** `2026-02-24`  
**Last Updated:** `2026-02-24`
