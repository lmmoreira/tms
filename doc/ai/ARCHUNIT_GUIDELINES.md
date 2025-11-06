# ArchUnit Guidelines for TMS Project

**Comprehensive guide for creating ArchUnit architecture tests**

---

## 🎯 Quick Navigation

| Need | Go To |
|------|-------|
| First time? | [Quick Start Template](#quick-start-template) |
| What can I check? | [Decision Tree](#decision-tree) |
| Copy-paste patterns | [Common Patterns](#common-patterns) |
| Custom conditions | [Custom Conditions Library](#custom-conditions-library) |
| What to avoid | [Anti-Patterns](#anti-patterns) |
| Not working? | [Troubleshooting](#troubleshooting) |

---

## ⚡ Quick Start Template

```java
package br.com.logistics.tms.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.base.DescribedPredicate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

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
    void myFirstTest() {
        final ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
                .because("domain layer must be framework-free");

        rule.check(classes);
    }
}
```

---

## 🎯 Golden Rules

### ✅ **DO**

1. **Use custom `ArchCondition` for complex checks**
   - Most checks beyond simple package/annotation rules need custom conditions
   - Don't assume fluent API methods exist - create custom conditions

2. **Use comma-separated packages for multiple dependencies**
   ```java
   .resideInAnyPackage("org.springframework..", "jakarta.persistence..")
   ```

3. **Use streams for checking fields/methods**
   ```java
   javaClass.getMethods().stream().anyMatch(m -> /* condition */)
   ```

4. **Test compilation immediately**
   ```bash
   mvn clean compile test-compile
   ```

5. **Mark all fields as `final` in custom conditions**
   ```java
   private static final Pattern PATTERN = Pattern.compile(".*");
   ```

6. **Provide clear violation messages**
   ```java
   events.add(SimpleConditionEvent.violated(item, "Clear message explaining what's wrong"))
   ```

### ❌ **DON'T**

1. **Don't assume these methods exist (they don't!):**
   - `.haveMethodThatStartsWith("set")`
   - `.haveMethodThatIsStatic("unique")`
   - `.haveFieldMatching("pattern")`
   - `.haveFieldThat(predicate)`
   - `.haveSimpleNameMatching("regex")`
   - `.haveMethod("methodName")`

2. **Don't chain with `.or()` for packages:**
   ```java
   // ❌ WRONG
   .resideInAnyPackage("pkg1..").or().resideInAnyPackage("pkg2..")
   
   // ✅ CORRECT
   .resideInAnyPackage("pkg1..", "pkg2..")
   ```

3. **Don't skip compilation tests** - ArchUnit tests that don't compile are useless

4. **Don't write vague violation messages** - help developers understand what failed

---

## 🌲 Decision Tree

```
What do you need to check?
│
├─ Package dependencies?
│  └─ Use: noClasses().that().resideInAPackage("..domain..")
│           .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
│
├─ Annotations present/absent?
│  └─ Use: classes().that().resideInAPackage("..application..")
│           .should().beAnnotatedWith("DomainService")
│
├─ Fields are final?
│  └─ Use: fields().that().areDeclaredInClassesThat().resideInAPackage("..domain..")
│           .should().beFinal()
│
├─ Methods exist with specific name?
│  └─ Use: Custom ArchCondition (see Library below)
│
├─ Methods have setters?
│  └─ Use: Custom ArchCondition (see Library below)
│
├─ Methods return specific type?
│  └─ Use: Custom ArchCondition (see Library below)
│
├─ Fields have specific type?
│  └─ Use: Custom ArchCondition (see Library below)
│
├─ Classes match naming pattern?
│  └─ Use: Custom ArchCondition with regex (see Library below)
│
├─ Classes implement interface?
│  └─ Use: classes().should().implement("InterfaceName")
│
└─ Something complex/custom?
   └─ Create custom ArchCondition (template below)
```

---

## 📦 Common Patterns (Copy-Paste)

### Pattern 1: Package Dependencies

```java
// Domain should not depend on frameworks
@Test
void domainShouldNotDependOnFrameworks() {
    noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..",
                    "com.fasterxml.jackson.."
            )
            .because("domain layer must be framework-free")
            .check(classes);
}
```

### Pattern 2: Required Annotations

```java
// Use cases must have @DomainService
@Test
void useCasesShouldHaveDomainServiceAnnotation() {
    classes()
            .that().resideInAPackage("..application.usecases..")
            .and().haveSimpleNameEndingWith("UseCase")
            .should().beAnnotatedWith("br.com.logistics.tms.commons.application.annotation.DomainService")
            .because("all use cases must be annotated with @DomainService")
            .check(classes);
}
```

### Pattern 3: Fields Must Be Final

```java
// Domain fields must be final
@Test
void domainFieldsShouldBeFinal() {
    fields()
            .that().areDeclaredInClassesThat().resideInAPackage("..domain..")
            .and().areNotStatic()
            .should().beFinal()
            .because("domain objects must be immutable")
            .check(classes);
}
```

### Pattern 4: Implement Interface

```java
// Use cases must implement UseCase interface
@Test
void useCasesShouldImplementUseCaseInterface() {
    classes()
            .that().resideInAPackage("..application.usecases..")
            .and().haveSimpleNameEndingWith("UseCase")
            .should().implement("br.com.logistics.tms.commons.application.usecases.UseCase")
            .because("all use cases must implement UseCase interface")
            .check(classes);
}
```

### Pattern 5: No Field Injection

```java
// No field injection allowed
@Test
void noFieldInjection() {
    noClasses()
            .that().resideInAnyPackage("br.com.logistics.tms..")
            .should(haveFieldInjection())
            .because("use constructor injection only")
            .check(classes);
}

// Helper condition (add to bottom of class)
private static ArchCondition<JavaClass> haveFieldInjection() {
    return new ArchCondition<>("have field injection") {
        @Override
        public void check(final JavaClass clazz, final ConditionEvents events) {
            for (final JavaField field : clazz.getAllFields()) {
                final boolean hasAutowired = field.isAnnotatedWith("org.springframework.beans.factory.annotation.Autowired");
                final boolean hasInject = field.isAnnotatedWith("jakarta.inject.Inject");

                if (hasAutowired || hasInject) {
                    final String annotation = hasAutowired ? "@Autowired" : "@Inject";
                    final String message = String.format(
                            "Class %s has field %s annotated with %s - use constructor injection",
                            clazz.getName(),
                            field.getName(),
                            annotation
                    );
                    events.add(SimpleConditionEvent.violated(field, message));
                }
            }
        }
    };
}
```

---

## 🛠️ Custom Conditions Library

### Template for Custom Conditions

```java
private static ArchCondition<JavaClass> myCustomCondition(String parameter) {
    return new ArchCondition<>("description of what this checks") {
        @Override
        public void check(final JavaClass javaClass, final ConditionEvents events) {
            // Your checking logic
            final boolean isValid = /* your condition */;
            
            if (!isValid) {
                final String message = "Clear message: " + javaClass.getName() + " violates rule";
                events.add(SimpleConditionEvent.violated(javaClass, message));
            }
        }
    };
}

// Use it:
classes().that()...should(myCustomCondition("param"))
```

### 1. Check for Setters

```java
private static ArchCondition<JavaClass> haveSetters() {
    return new ArchCondition<>("have setter methods") {
        @Override
        public void check(final JavaClass javaClass, final ConditionEvents events) {
            for (final JavaMethod method : javaClass.getMethods()) {
                if (method.getName().startsWith("set") && method.getRawParameterTypes().size() == 1) {
                    final String message = String.format(
                            "Class %s has setter method %s - domain objects must be immutable",
                            javaClass.getSimpleName(),
                            method.getName()
                    );
                    events.add(SimpleConditionEvent.violated(method, message));
                }
            }
        }
    };
}

// Use: noClasses().that().resideInAPackage("..domain..").should(haveSetters())
```

### 2. Check for Static Method by Name

```java
private static ArchCondition<JavaClass> haveStaticMethodNamed(final String methodName) {
    return new ArchCondition<>("have static method named '" + methodName + "'") {
        @Override
        public void check(final JavaClass javaClass, final ConditionEvents events) {
            final boolean hasMethod = javaClass.getMethods().stream()
                    .anyMatch(m -> m.getName().equals(methodName) && m.getModifiers().contains(JavaModifier.STATIC));

            if (!hasMethod) {
                final String message = String.format(
                        "Class %s should have static method '%s'",
                        javaClass.getSimpleName(),
                        methodName
                );
                events.add(SimpleConditionEvent.violated(javaClass, message));
            }
        }
    };
}

// Use: classes().that()...should(haveStaticMethodNamed("unique"))
```

### 3. Check for Instance Method by Name

```java
private static ArchCondition<JavaClass> haveMethodNamed(final String methodName) {
    return new ArchCondition<>("have method named '" + methodName + "'") {
        @Override
        public void check(final JavaClass javaClass, final ConditionEvents events) {
            final boolean hasMethod = javaClass.getMethods().stream()
                    .anyMatch(m -> m.getName().equals(methodName));

            if (!hasMethod) {
                final String message = String.format(
                        "Class %s should have method '%s'",
                        javaClass.getSimpleName(),
                        methodName
                );
                events.add(SimpleConditionEvent.violated(javaClass, message));
            }
        }
    };
}

// Use: classes().that()...should(haveMethodNamed("execute"))
```

### 4. Check for Field of Type (Contains)

```java
private static ArchCondition<JavaClass> haveFieldOfTypeContaining(final String... typeFragments) {
    return new ArchCondition<>("have field of type containing " + String.join(" or ", typeFragments)) {
        @Override
        public void check(final JavaClass javaClass, final ConditionEvents events) {
            final boolean hasField = javaClass.getAllFields().stream()
                    .anyMatch(f -> {
                        final String typeName = f.getRawType().getName();
                        for (final String fragment : typeFragments) {
                            if (typeName.contains(fragment)) return true;
                        }
                        return false;
                    });

            if (!hasField) {
                final String message = String.format(
                        "Class %s should have field of type containing one of: %s",
                        javaClass.getSimpleName(),
                        String.join(", ", typeFragments)
                );
                events.add(SimpleConditionEvent.violated(javaClass, message));
            }
        }
    };
}

// Use: classes().that()...should(haveFieldOfTypeContaining("Repository", "OutboxService"))
```

### 5. Check for Field of Exact Type

```java
private static ArchCondition<JavaClass> haveFieldOfTypeExactly(final String typeName) {
    return new ArchCondition<>("have field of type exactly '" + typeName + "'") {
        @Override
        public void check(final JavaClass javaClass, final ConditionEvents events) {
            final boolean hasField = javaClass.getAllFields().stream()
                    .anyMatch(f -> f.getRawType().getName().equals(typeName));

            if (!hasField) {
                final String message = String.format(
                        "Class %s should have field of type %s",
                        javaClass.getSimpleName(),
                        typeName
                );
                events.add(SimpleConditionEvent.violated(javaClass, message));
            }
        }
    };
}

// Use: classes().that()...should(haveFieldOfTypeExactly("br.com.logistics.tms.commons.VoidUseCaseExecutor"))
```

### 6. Check for Field with Annotation

```java
private static ArchCondition<JavaClass> haveFieldAnnotatedWith(final String annotationName) {
    return new ArchCondition<>("have field annotated with @" + annotationName.substring(annotationName.lastIndexOf('.') + 1)) {
        @Override
        public void check(final JavaClass javaClass, final ConditionEvents events) {
            final boolean hasAnnotatedField = javaClass.getAllFields().stream()
                    .anyMatch(f -> f.isAnnotatedWith(annotationName));

            if (!hasAnnotatedField) {
                final String message = String.format(
                        "Class %s should have a field annotated with @%s",
                        javaClass.getSimpleName(),
                        annotationName.substring(annotationName.lastIndexOf('.') + 1)
                );
                events.add(SimpleConditionEvent.violated(javaClass, message));
            }
        }
    };
}

// Use: classes().that()...should(haveFieldAnnotatedWith("jakarta.persistence.Id"))
```

### 7. Check Method Return Type (Same as Declaring Class)

```java
private static ArchCondition<JavaMethod> returnSameTypeAsDeclaringClass() {
    return new ArchCondition<>("return same type as declaring class") {
        @Override
        public void check(final JavaMethod method, final ConditionEvents events) {
            final boolean ok = method.getReturnType().toErasure().equals(method.getOwner());
            final String message = ok
                    ? method.getFullName() + " returns same type as declaring class"
                    : method.getFullName() + " should return same type but returns " + method.getReturnType().toErasure().getName();
            events.add(new SimpleConditionEvent(method, ok, message));
        }
    };
}

// Use: methods().that().haveNameMatching("update.*").should(returnSameTypeAsDeclaringClass())
```

### 8. Check Simple Name Pattern (Regex)

```java
private static ArchCondition<JavaClass> matchSimpleNamePattern(final String regex) {
    return new ArchCondition<>("have simple name matching pattern " + regex) {
        private final Pattern pattern = Pattern.compile(regex);

        @Override
        public void check(final JavaClass javaClass, final ConditionEvents events) {
            if (!pattern.matcher(javaClass.getSimpleName()).matches()) {
                final String message = String.format(
                        "Class %s does not match naming pattern %s",
                        javaClass.getSimpleName(),
                        regex
                );
                events.add(SimpleConditionEvent.violated(javaClass, message));
            }
        }
    };
}

// Use: classes().that()...should(matchSimpleNamePattern("^[A-Z][a-zA-Z]*Created$"))
```

### 9. Check for Specific Field Name

```java
private static ArchCondition<JavaClass> haveFieldNamed(final String... fieldNames) {
    return new ArchCondition<>("have field named: " + String.join(" or ", fieldNames)) {
        @Override
        public void check(final JavaClass javaClass, final ConditionEvents events) {
            final boolean hasField = javaClass.getAllFields().stream()
                    .anyMatch(f -> {
                        for (final String name : fieldNames) {
                            if (f.getName().equals(name) || f.getName().endsWith(name)) {
                                return true;
                            }
                        }
                        return false;
                    });

            if (!hasField) {
                final String message = String.format(
                        "Class %s does not have field named: %s",
                        javaClass.getName(),
                        String.join(" or ", fieldNames)
                );
                events.add(SimpleConditionEvent.violated(javaClass, message));
            }
        }
    };
}

// Use: classes().that()...should(haveFieldNamed("aggregateId", "id"))
```

### 10. Check Class Has All Fields Final

```java
private static ArchCondition<JavaClass> haveAllFieldsFinal() {
    return new ArchCondition<>("have all non-static fields final") {
        @Override
        public void check(final JavaClass javaClass, final ConditionEvents events) {
            for (final JavaField field : javaClass.getAllFields()) {
                if (!field.getModifiers().contains(JavaModifier.STATIC) && !field.getModifiers().contains(JavaModifier.FINAL)) {
                    final String message = String.format(
                            "Class %s has non-final field '%s' - should be final for immutability",
                            javaClass.getSimpleName(),
                            field.getName()
                    );
                    events.add(SimpleConditionEvent.violated(field, message));
                }
            }
        }
    };
}

// Use: classes().that().resideInAPackage("..domain..").should(haveAllFieldsFinal())
```

---

## ❌ Anti-Patterns

### Anti-Pattern 1: Using Non-Existent Methods

```java
// ❌ WRONG - These methods DO NOT EXIST
.should().haveMethodThatStartsWith("set")
.should().haveMethodThatIsStatic("unique")
.should().haveFieldMatching("value|types")
.should().haveSimpleNameMatching("regex")
.should().haveMethod("toDomain")

// ✅ CORRECT - Create custom ArchCondition
private static ArchCondition<JavaClass> haveSetters() {
    return new ArchCondition<>("have setters") {
        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            for (JavaMethod method : javaClass.getMethods()) {
                if (method.getName().startsWith("set") && method.getRawParameterTypes().size() == 1) {
                    events.add(SimpleConditionEvent.violated(method, "has setter: " + method.getName()));
                }
            }
        }
    };
}
```

### Anti-Pattern 2: Wrong Package Chaining

```java
// ❌ WRONG
.should().dependOnClassesThat().resideInAnyPackage("org.springframework.web..")
.or().dependOnClassesThat().resideInAnyPackage("jakarta.servlet..")

// ✅ CORRECT
.should().dependOnClassesThat().resideInAnyPackage(
    "org.springframework.web..",
    "jakarta.servlet.."
)
```

### Anti-Pattern 3: Wrong Predicate Usage

```java
// ❌ WRONG - haveRawReturnType doesn't accept DescribedPredicate
.should().haveRawReturnType(
    DescribedPredicate.describe("return same type", 
        method -> method.getReturnType().toErasure().equals(method.getOwner())
    )
)

// ✅ CORRECT - Create custom ArchCondition<JavaMethod>
private static ArchCondition<JavaMethod> returnSameType() {
    return new ArchCondition<>("return same type") {
        @Override
        public void check(JavaMethod method, ConditionEvents events) {
            boolean ok = method.getReturnType().toErasure().equals(method.getOwner());
            if (!ok) {
                events.add(SimpleConditionEvent.violated(method, "wrong return type"));
            }
        }
    };
}
```

### Anti-Pattern 4: Not Testing Compilation

```java
// ❌ WRONG - Claiming success without testing
// "I created 10 ArchUnit tests" (but they don't compile!)

// ✅ CORRECT - Always verify
mvn clean compile test-compile
mvn test -Dtest=MyArchitectureTest
```

### Anti-Pattern 5: Vague Violation Messages

```java
// ❌ WRONG
events.add(SimpleConditionEvent.violated(javaClass, "invalid"));

// ✅ CORRECT
String message = String.format(
    "Class %s violates immutability - field '%s' is not final",
    javaClass.getSimpleName(),
    field.getName()
);
events.add(SimpleConditionEvent.violated(field, message));
```

---

## 📚 Required Imports

Always include these imports when creating ArchUnit tests:

```java
// Core ArchUnit classes
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaModifier;

// Import configuration
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

// Rule definitions
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.base.DescribedPredicate;

// JUnit
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

// Java utilities (if needed)
import java.util.regex.Pattern;

// Static imports
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
```

---

## 🧪 Testing & Validation

### Before Committing

```bash
# 1. Compile main code
mvn clean compile

# 2. Compile tests
mvn test-compile

# 3. Run specific test
mvn test -Dtest=MyArchitectureTest

# 4. Run all architecture tests
mvn test -Dtest=br.com.logistics.tms.architecture.*

# 5. Verify build passes
mvn clean verify
```

### Checklist

- [ ] All imports present
- [ ] No use of non-existent ArchUnit API methods
- [ ] Custom `ArchCondition` for complex checks
- [ ] Helper methods at bottom of class for reusability
- [ ] Clear violation messages in `SimpleConditionEvent`
- [ ] Test descriptions are clear and specific
- [ ] Code compiles without errors
- [ ] Tests run and pass (or fail as expected)
- [ ] All fields marked as `final` where possible

---

## 🔧 Troubleshooting

### Issue: "Method not found" compilation error

**Cause:** Using non-existent ArchUnit fluent API methods

**Solution:** Create custom `ArchCondition` (see Library above)

---

### Issue: Tests pass but should fail

**Cause:** Condition logic is inverted or incorrect

**Solution:** 
1. Add print statements to verify condition is being checked
2. Test with a known violation
3. Verify boolean logic in condition

---

### Issue: Too many violations

**Cause:** Rule is too broad or checks wrong classes

**Solution:**
1. Narrow package filter: `.that().resideInAPackage("..specific.package..")`
2. Add exclusions: `.and().haveSimpleNameNotContaining("Test")`
3. Use more specific predicates

---

### Issue: Custom condition not triggering

**Cause:** Predicate in `.that()` filters out all classes

**Solution:**
1. Remove filters one by one to find culprit
2. Verify package names are correct (use `..` notation)
3. Check class actually exists in codebase

---

### Issue: "Cannot resolve symbol" for custom condition

**Cause:** Method defined after it's used, or not static

**Solution:**
1. Move helper methods to bottom of class
2. Ensure methods are `private static`
3. Check method name matches usage

---

## 📖 TMS Project Specific Rules

### Domain Layer Rules

```java
@Test
void domainShouldBeFrameworkFree() {
    noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..",
                    "com.fasterxml.jackson..",
                    "org.hibernate.."
            )
            .because("domain layer must contain only pure Java")
            .check(classes);
}

@Test
void domainObjectsShouldBeImmutable() {
    noClasses()
            .that().resideInAPackage("..domain..")
            .should(haveSetters())
            .because("domain objects must be immutable")
            .check(classes);
}

@Test
void domainFieldsShouldBeFinal() {
    fields()
            .that().areDeclaredInClassesThat().resideInAPackage("..domain..")
            .and().areNotStatic()
            .should().beFinal()
            .because("domain objects must be immutable")
            .check(classes);
}
```

### Application Layer Rules

```java
@Test
void applicationShouldBeFrameworkFree() {
    noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence.."
            )
            .because("application layer must be framework-free")
            .check(classes);
}

@Test
void useCasesShouldHaveDomainServiceAnnotation() {
    classes()
            .that().resideInAPackage("..application.usecases..")
            .and().haveSimpleNameEndingWith("UseCase")
            .should().beAnnotatedWith("br.com.logistics.tms.commons.application.annotation.DomainService")
            .because("all use cases must be annotated with @DomainService")
            .check(classes);
}

@Test
void useCasesShouldImplementUseCaseInterface() {
    classes()
            .that().resideInAPackage("..application.usecases..")
            .and().haveSimpleNameEndingWith("UseCase")
            .should().implement("br.com.logistics.tms.commons.application.usecases.UseCase")
            .because("all use cases must implement UseCase interface")
            .check(classes);
}
```

### Infrastructure Layer Rules

```java
@Test
void controllersShouldHaveCqrsAnnotation() {
    classes()
            .that().resideInAPackage("..infrastructure.rest..")
            .and().haveSimpleNameEndingWith("Controller")
            .should().beAnnotatedWith("br.com.logistics.tms.commons.application.annotation.Cqrs")
            .because("all controllers must declare CQRS role")
            .check(classes);
}

@Test
void noFieldInjectionInInfrastructure() {
    noClasses()
            .that().resideInAPackage("..infrastructure..")
            .should(haveFieldInjection())
            .because("use constructor injection only")
            .check(classes);
}
```

### Value Objects Rules

```java
@Test
void valueObjectsShouldBeRecords() {
    classes()
            .that().haveSimpleNameEndingWith("Id")
            .or().haveSimpleNameMatching(".*(Cnpj|Cpf|Email|Data|Types)$")
            .should().beRecords()
            .because("value objects should be implemented as records for immutability")
            .check(classes);
}
```

### Event Rules

```java
@Test
void domainEventsShouldHavePastTenseNames() {
    classes()
            .that().resideInAPackage("..domain..")
            .and().haveSimpleNameEndingWith("Event")
            .or().areAssignableTo("br.com.logistics.tms.commons.domain.AbstractDomainEvent")
            .should(matchSimpleNamePattern("^[A-Z][a-zA-Z]*(Created|Updated|Deleted|Cancelled|Completed)$"))
            .because("domain events should use past tense naming")
            .check(classes);
}
```

---

## 💡 Best Practices

1. **One test per rule** - Keep tests focused and easy to understand
2. **Clear test names** - Use descriptive method names: `domainShouldBeFrameworkFree()`
3. **Meaningful because clauses** - Explain WHY the rule exists
4. **Group related tests** - Use nested test classes for organization
5. **Reuse helper conditions** - Create library at bottom of class
6. **Test your tests** - Introduce violations to verify rules work
7. **Keep messages actionable** - Tell developers HOW to fix violations

---

## 📌 Summary

### ✅ **Always Do:**
- Create custom `ArchCondition` for complex checks
- Test compilation immediately
- Use streams for iteration
- Provide clear violation messages
- Mark variables as `final`

### ❌ **Never Do:**
- Assume fluent API methods exist
- Chain packages with `.or()`
- Skip compilation testing
- Write vague error messages
- Use field injection

### 🎯 **When in Doubt:**
1. Check [Decision Tree](#decision-tree)
2. Look at [Custom Conditions Library](#custom-conditions-library)
3. Copy template and adapt
4. Test compilation
5. Verify rule works with known violation

---

**Remember:** ArchUnit is powerful but requires custom conditions for most real-world checks. Don't assume fluent API methods exist - create custom conditions and test them!

---

**Last Updated:** 2025-11-05  
**Maintained by:** TMS Development Team
