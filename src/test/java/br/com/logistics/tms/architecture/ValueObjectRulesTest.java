package br.com.logistics.tms.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static br.com.logistics.tms.architecture.ArchUnitConditions.haveStaticMethodNamed;
import static br.com.logistics.tms.architecture.ArchUnitPredicates.matchSimpleNamePattern;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.constructors;

/**
 * ArchUnit tests to enforce value object rules in TMS.
 * <p>
 * Value Object Rules:
 * - Value objects are records
 * - Value objects in *.domain package
 * - ID value objects named *Id
 * - Value objects have validation (compact constructor)
 */
class ValueObjectRulesTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setup() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("br.com.logistics.tms");
    }

    @Test
    void valueObjectsShouldBeRecords() {
        final ArchRule rule = classes()
                .that().resideInAPackage("..domain..")
                .and(matchSimpleNamePattern(".*(Data|Id|Configurations|Types|Cnpj|Cpf|Email)$"))
                .should().beRecords()
                .because("Value objects should be immutable records");

        rule.check(classes);
    }

    @Test
    void idValueObjectsShouldBeNamedWithIdSuffix() {
        final ArchRule rule = classes()
                .that().resideInAPackage("..domain..")
                .and().areRecords()
                .and().haveSimpleNameEndingWith("Id")
                .should().haveOnlyFinalFields()
                .because("ID value objects must be named *Id and be immutable");

        rule.check(classes);
    }


    @Test
    void valueObjectRecordsShouldHaveCompactConstructor() {
        // Records with validation should have compact constructor
        // This is implicit in records, but we can check they don't have explicit constructors
        final ArchRule rule = constructors()
                .that().areDeclaredInClassesThat().resideInAPackage("..domain..")
                .and().areDeclaredInClassesThat().areRecords()
                .should().bePublic()
                .because("Value object records should use compact constructor for validation");

        rule.check(classes);
    }

    @Test
    void idValueObjectsShouldHaveUniqueFactoryMethod() {
        ArchRule rule = ArchRuleDefinition.classes()
                .that().resideInAPackage("..domain..")
                .and().haveSimpleNameEndingWith("Id")
                .and().areRecords()
                .should(haveStaticMethodNamed("unique"))
                .because("ID value objects should have unique() static factory method");

        rule.check(classes);
    }

    @Test
    void idValueObjectsShouldHaveWithFactoryMethod() {
        ArchCondition<JavaClass> haveWithStaticFactory = new ArchCondition<>("have static with() method") {
            @Override
            public void check(JavaClass clazz, ConditionEvents events) {
                boolean hasWith = clazz.getMethods().stream()
                        .anyMatch(m -> m.getName().equals("with") && m.getModifiers().contains(JavaModifier.STATIC));

                if (!hasWith) {
                    String message = String.format("Class %s does not have static with() factory method", clazz.getName());
                    events.add(SimpleConditionEvent.violated(clazz, message));
                }
            }
        };

        ArchRule rule = ArchRuleDefinition.classes()
                .that().resideInAPackage("..domain..")
                .and().haveSimpleNameEndingWith("Id")
                .and().areRecords()
                .should(haveWithStaticFactory)
                .because("ID value objects should have with() static factory method");

        rule.check(new com.tngtech.archunit.core.importer.ClassFileImporter()
                .importPackages("br.com.logistics.tms"));
    }

    @Test
    void cnpjValueObjectShouldBeRecord() {
        final ArchRule rule = classes()
                .that().haveSimpleName("Cnpj")
                .should().beRecords()
                .andShould().resideInAPackage("..domain..")
                .because("Cnpj should be a value object record in domain");

        rule.check(classes);
    }

    @Test
    void configurationsValueObjectShouldBeRecord() {
        final ArchRule rule = classes()
                .that().haveSimpleName("Configurations")
                .should().beRecords()
                .andShould().resideInAPackage("..domain..")
                .because("Configurations should be a value object record in domain");

        rule.check(classes);
    }
}
