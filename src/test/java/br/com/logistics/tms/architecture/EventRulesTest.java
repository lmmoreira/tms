package br.com.logistics.tms.architecture;

import br.com.logistics.tms.commons.domain.AbstractDomainEvent;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static br.com.logistics.tms.architecture.ArchUnitConditions.matchSimpleNamePattern;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;

/**
 * ArchUnit tests to enforce domain event rules in TMS.
 * 
 * Event Rules:
 * - Events named in past tense (*Created, *Updated, *Deleted)
 * - Events are immutable (final fields)
 * - Events in *.domain package
 * - Events extend AbstractDomainEvent
 * - Events have aggregateId field
 */
class EventRulesTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setup() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("br.com.logistics.tms");
    }

    @Test
    void eventsShouldBeNamedInPastTense() {
        final ArchRule rule = classes()
                .that().areAssignableTo("br.com.logistics.tms.commons.domain.AbstractDomainEvent")
                .and().resideOutsideOfPackage("..commons..")
                .should().haveSimpleNameEndingWith("Created")
                .orShould().haveSimpleNameEndingWith("Updated")
                .orShould().haveSimpleNameEndingWith("Retrieved")
                .orShould().haveSimpleNameEndingWith("Deleted")
                .orShould().haveSimpleNameEndingWith("Event")
                .because("Domain events should be named in past tense");

        rule.check(classes);
    }

    @Test
    void eventsShouldExtendAbstractDomainEvent() {
        final ArchRule rule = classes()
                .that().resideInAPackage("..domain..")
                .and().haveSimpleNameEndingWith("Created")
                .or().haveSimpleNameEndingWith("Updated")
                .or().haveSimpleNameEndingWith("Retrieved")
                .or().haveSimpleNameEndingWith("Deleted")
                .should().beAssignableTo("br.com.logistics.tms.commons.domain.AbstractDomainEvent")
                .because("Domain events must extend AbstractDomainEvent");

        rule.check(classes);
    }

    @Test
    void eventsShouldBeInDomainPackage() {
        final ArchRule rule = classes()
                .that().areAssignableTo("br.com.logistics.tms.commons.domain.AbstractDomainEvent")
                .and().resideOutsideOfPackage("..commons..")
                .should().resideInAPackage("..domain..")
                .because("Domain events must be in *.domain package");

        rule.check(classes);
    }

    @Test
    void eventFieldsShouldBeFinal() {
        final ArchRule rule = fields()
                .that().areDeclaredInClassesThat().areAssignableTo("br.com.logistics.tms.commons.domain.AbstractDomainEvent")
                .and().areNotStatic()
                .should().beFinal()
                .because("Domain event fields must be final (immutable)");

        rule.check(classes);
    }

    @Test
    void eventsShouldNotHaveSetters() {
        final ArchRule rule = classes()
                .that().areAssignableTo(AbstractDomainEvent.class)
                .should(new ArchCondition<>("not have any setters") {
                    @Override
                    public void check(JavaClass javaClass, ConditionEvents events) {
                        boolean hasSetter = javaClass.getAllMethods().stream()
                                .anyMatch(m -> m.getName().startsWith("set"));
                        if (hasSetter) {
                            events.add(SimpleConditionEvent.violated(
                                    javaClass, String.format("Class %s has setter methods", javaClass.getName())
                            ));
                        }
                    }
                })
                .because("Domain events must be immutable - no setters");
        rule.check(classes);
    }

    @Test
    void eventsShouldHaveGetters() {
        final ArchRule rule = classes()
                .that().areAssignableTo(AbstractDomainEvent.class)
                .and().resideOutsideOfPackage("..commons..")
                .should(new ArchCondition<>("have at least one getter") {
                    @Override
                    public void check(JavaClass javaClass, ConditionEvents events) {
                        boolean hasGetter = javaClass.getAllMethods().stream()
                                .anyMatch(m -> m.getName().startsWith("get"));
                        if (!hasGetter) {
                            events.add(SimpleConditionEvent.violated(
                                    javaClass, String.format("Class %s has no getter methods", javaClass.getName())
                            ));
                        }
                    }
                })
                .because("Domain events should have getters for their fields");
        rule.check(classes);
    }

    @Test
    void createdEventsShouldFollowNamingPattern() {
        ArchRule rule = classes()
                .that().resideInAPackage("..domain..")
                .and().haveSimpleNameEndingWith("Created")
                .should(matchSimpleNamePattern("^[A-Z][a-zA-Z]*Created$"))
                .because("Created events should follow pattern: {Entity}Created");

        rule.check(classes);
    }

    @Test
    void updatedEventsShouldFollowNamingPattern() {
        ArchRule rule = classes()
                .that().resideInAPackage("..domain..")
                .and().haveSimpleNameEndingWith("Updated")
                .should(matchSimpleNamePattern("^[A-Z][a-zA-Z]*Updated$"))
                .because("Updated events should follow pattern: {Entity}Updated");

        rule.check(classes);
    }

    @Test
    void deletedEventsShouldFollowNamingPattern() {
        ArchRule rule = classes()
                .that().resideInAPackage("..domain..")
                .and().haveSimpleNameEndingWith("Deleted")
                .should(matchSimpleNamePattern("^[A-Z][a-zA-Z]*Deleted$"))
                .because("Deleted events should follow pattern: {Entity}Deleted");

        rule.check(classes);
    }

}
