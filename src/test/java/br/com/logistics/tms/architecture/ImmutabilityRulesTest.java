package br.com.logistics.tms.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static br.com.logistics.tms.architecture.ArchUnitConditions.haveSetters;
import static br.com.logistics.tms.architecture.ArchUnitConditions.returnTheSameClassAsDeclaring;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

class ImmutabilityRulesTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setup() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("br.com.logistics.tms");
    }

    @Test
    void aggregatesShouldNotHaveSetters() {
        final ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .and().areAssignableTo("br.com.logistics.tms.commons.domain.AbstractAggregateRoot")
                .should(haveSetters())
                .because("Aggregates must be immutable - no setters allowed");

        rule.check(classes);
    }

    @Test
    void aggregateFieldsShouldBeFinal() {
        final ArchRule rule = fields()
                .that().areDeclaredInClassesThat().resideInAPackage("..domain..")
                .and().areDeclaredInClassesThat().areAssignableTo("br.com.logistics.tms.commons.domain.AbstractAggregateRoot")
                .and().areNotStatic()
                .should().beFinal()
                .because("Aggregate fields must be final for immutability");

        rule.check(classes);
    }

    @Test
    void valueObjectsShouldNotHaveSetters() {
        final ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .and().areRecords()
                .should(haveSetters())
                .because("Value objects (records) must be immutable - no setters allowed");

        rule.check(classes);
    }

    @Test
    void valueObjectFieldsShouldBeFinal() {
        final ArchRule rule = fields()
                .that().areDeclaredInClassesThat().resideInAPackage("..domain..")
                .and().areDeclaredInClassesThat().areRecords()
                .and().areNotStatic()
                .should().beFinal()
                .because("Value object fields must be final");

        rule.check(classes);
    }

    @Test
    void domainClassesShouldNotHaveSetters() {
        final ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .and().areNotInterfaces()
                .and().areNotEnums()
                .should(haveSetters())
                .because("Domain classes must be immutable - no setters allowed");

        rule.check(classes);
    }

    @Test
    void updateMethodsShouldReturnSameType() {
        final ArchRule rule = com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods()
                .that().areDeclaredInClassesThat().resideInAPackage("..domain..")
                .and().areDeclaredInClassesThat().areAssignableTo("br.com.logistics.tms.commons.domain.AbstractAggregateRoot")
                .and().haveNameStartingWith("update")
                .and().arePublic()
                .should(returnTheSameClassAsDeclaring())
                .because("Update methods must return new instance of same type (immutability)");

        rule.check(classes);
    }

    @Test
    void idValueObjectsShouldBeRecords() {
        final ArchRule rule = classes()
                .that().resideInAPackage("..domain..")
                .and().haveSimpleNameEndingWith("Id")
                .and().areNotInterfaces()
                .and().areNotEnums()
                .should().beRecords()
                .because("ID value objects must be records for immutability");

        rule.check(classes);
    }

    @Test
    void domainEventsShouldBeImmutable() {
        final ArchRule rule = fields()
                .that().areDeclaredInClassesThat().areAssignableTo("br.com.logistics.tms.commons.domain.AbstractDomainEvent")
                .and().areNotStatic()
                .should().beFinal()
                .because("Domain event fields must be final (immutable events)");

        rule.check(classes);
    }

    @Test
    void domainEventsShouldNotHaveSetters() {
        final ArchRule rule = noClasses()
                .that().areAssignableTo("br.com.logistics.tms.commons.domain.AbstractDomainEvent")
                .should(haveSetters())
                .because("Domain events must be immutable - no setters allowed");

        rule.check(classes);
    }

}
