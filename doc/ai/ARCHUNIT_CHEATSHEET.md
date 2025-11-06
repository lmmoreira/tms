# ArchUnit Utilities Cheat Sheet

**Quick reference for ArchUnitConditions and ArchUnitPredicates**

---

## 📦 Import Statements

```java
// Import conditions (for .should() clauses)
import static br.com.logistics.tms.architecture.ArchUnitConditions.*;

// Import predicates (for .that() clauses)
import static br.com.logistics.tms.architecture.ArchUnitPredicates.*;
```

---

## 🛠️ ArchUnitConditions

**Use in `.should()` clauses to define what to check**

### Check for Setters

```java
// Check if classes have setter methods
noClasses()
    .that().resideInAPackage("..domain..")
    .should(haveSetters())
    .because("Domain must be immutable")
    .check(classes);
```

### Check for Static Method

```java
// Check if classes have specific static method
classes()
    .that().haveSimpleNameEndingWith("Id")
    .should(haveStaticMethodNamed("unique"))
    .because("ID value objects need unique() factory")
    .check(classes);
```

### Check for Instance Method

```java
// Check if classes have specific instance method
classes()
    .that().areAnnotatedWith(Entity.class)
    .should(haveMethodNamed("toDomain"))
    .because("JPA entities need toDomain() converter")
    .check(classes);
```

### Check Field Type (Partial Match)

```java
// Check if classes have field of type containing fragment
classes()
    .that().haveSimpleNameEndingWith("Controller")
    .should(haveFieldOfTypeContaining("RestUseCaseExecutor", "RestPresenter"))
    .because("Controllers delegate to executors")
    .check(classes);
```

### Check Field Type (Exact Match)

```java
// Check if classes have field of exact type
classes()
    .that().haveSimpleNameEndingWith("Listener")
    .should(haveFieldOfTypeExactly("VoidUseCaseExecutor"))
    .because("Listeners use VoidUseCaseExecutor")
    .check(classes);
```

### Check Field Annotation

```java
// Check if classes have field with specific annotation
classes()
    .that().areAnnotatedWith("jakarta.persistence.Entity")
    .should(haveFieldAnnotatedWith("jakarta.persistence.Id"))
    .because("JPA entities need @Id field")
    .check(classes);
```

### Check Multiple Name Endings

```java
// Check if classes end with one of multiple suffixes
classes()
    .that().resideInAPackage("..infrastructure.repositories..")
    .should(haveSimpleNameEndingWithAny("RepositoryImpl", "JpaRepository"))
    .because("Repository implementations follow naming")
    .check(classes);
```

### Check Name Pattern (Regex)

```java
// Check if class name matches regex pattern
classes()
    .that().resideInAPackage("..domain..")
    .and().haveSimpleNameEndingWith("Created")
    .should(matchSimpleNamePattern("^[A-Z][a-zA-Z]*Created$"))
    .because("Events follow {Entity}Created pattern")
    .check(classes);
```

### Check Method Return Type

```java
// Check if methods return same type as declaring class
methods()
    .that().haveNameStartingWith("update")
    .and().areDeclaredInClassesThat().areAssignableTo(AbstractAggregateRoot.class)
    .should(returnTheSameClassAsDeclaring())
    .because("Update methods return new instance (immutability)")
    .check(classes);
```

---

## 🎯 ArchUnitPredicates

**Use in `.that()` clauses to filter classes**

### Filter by Name Pattern (Regex)

```java
// Filter classes by regex pattern before applying rule
classes()
    .that(matchSimpleNamePattern("^[A-Z][a-zA-Z]*Created$"))
    .should().resideInAPackage("..domain..")
    .because("Created events must be in domain")
    .check(classes);
```

---

## 📋 Common Combinations

### Immutability Check

```java
// Check aggregate has no setters and all fields final
@Test
void aggregatesShouldBeImmutable() {
    // No setters
    noClasses()
        .that().areAssignableTo(AbstractAggregateRoot.class)
        .should(haveSetters())
        .check(classes);
    
    // Final fields
    fields()
        .that().areDeclaredInClassesThat().areAssignableTo(AbstractAggregateRoot.class)
        .and().areNotStatic()
        .should().beFinal()
        .check(classes);
}
```

### Event Naming Convention

```java
// Check events follow naming pattern and are in correct package
@Test
void eventsShouldFollowConvention() {
    classes()
        .that(matchSimpleNamePattern("^[A-Z][a-zA-Z]*Created$"))
        .should().resideInAPackage("..domain..")
        .andShould().beAssignableTo(AbstractDomainEvent.class)
        .check(classes);
}
```

### Value Object Pattern

```java
// Check ID value objects have factory methods
@Test
void idValueObjectsShouldHaveFactories() {
    classes()
        .that().haveSimpleNameEndingWith("Id")
        .and().areRecords()
        .should(haveStaticMethodNamed("unique"))
        .andShould(haveStaticMethodNamed("with"))
        .check(classes);
}
```

### Controller Pattern

```java
// Check controllers use executors
@Test
void controllersShouldUseExecutors() {
    classes()
        .that().haveSimpleNameEndingWith("Controller")
        .should(haveFieldOfTypeContaining("RestUseCaseExecutor", "RestPresenter"))
        .because("Controllers delegate to executors")
        .check(classes);
}
```

---

## 💡 Tips

### DO ✅

```java
// Use utility conditions
import static br.com.logistics.tms.architecture.ArchUnitConditions.*;
classes().should(haveSetters())

// Combine with standard ArchUnit methods
classes()
    .that().resideInAPackage("..domain..")
    .should(haveSetters())
    .andShould().haveOnlyFinalFields()
```

### DON'T ❌

```java
// Don't duplicate condition code
private static ArchCondition<JavaClass> haveSetters() {
    // Same code as in ArchUnitConditions!
}

// Don't forget to import
classes().should(haveSetters())  // Won't compile without import!
```

---

## 🔍 Finding the Right Utility

### Need to Check...

| What | Use This | Type |
|------|----------|------|
| Setter methods exist | `haveSetters()` | Condition |
| Static method exists | `haveStaticMethodNamed(name)` | Condition |
| Instance method exists | `haveMethodNamed(name)` | Condition |
| Field type (partial) | `haveFieldOfTypeContaining(fragments...)` | Condition |
| Field type (exact) | `haveFieldOfTypeExactly(type)` | Condition |
| Field has annotation | `haveFieldAnnotatedWith(annotation)` | Condition |
| Name ends with options | `haveSimpleNameEndingWithAny(suffixes...)` | Condition |
| Name matches regex | `matchSimpleNamePattern(regex)` | Condition or Predicate |
| Method returns self | `returnTheSameClassAsDeclaring()` | Condition |

### Need to Filter...

| What | Use This |
|------|----------|
| By name pattern | `matchSimpleNamePattern(regex)` (Predicate) |

---

## 📚 Full Documentation

- **Complete Guide:** [ARCHUNIT_GUIDELINES.md](./ARCHUNIT_GUIDELINES.md)
- **All Tests Catalog:** [ARCHUNIT_TEST_CATALOG.md](./ARCHUNIT_TEST_CATALOG.md)
- **Implementation:** `src/test/java/.../architecture/ArchUnitConditions.java`
- **Implementation:** `src/test/java/.../architecture/ArchUnitPredicates.java`

---

## 🚀 Quick Start Template

```java
package br.com.logistics.tms.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
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
    void myTest() {
        final ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should(haveSetters())  // ✅ Use utility
                .because("Clear reason here");

        rule.check(classes);
    }
}
```

---

**Last Updated:** 2025-11-06
