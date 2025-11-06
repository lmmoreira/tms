package br.com.logistics.tms.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static br.com.logistics.tms.architecture.ArchUnitConditions.haveSimpleNameEndingWithAny;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * ArchUnit tests to enforce naming conventions in TMS.
 * 
 * Naming Rules:
 * - Use cases: *UseCase in *.application.usecases
 * - Controllers: *Controller in *.infrastructure.rest
 * - Repositories: *Repository in application, *RepositoryImpl in infrastructure
 * - Events: Extend AbstractDomainEvent, in *.domain
 * - Aggregates: Extend AbstractAggregateRoot
 * - Listeners: *Listener in *.infrastructure.listener
 * - Entities: In *.infrastructure.jpa.entities
 */
class NamingConventionTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setup() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("br.com.logistics.tms");
    }

    @Test
    void useCasesShouldBeNamedCorrectly() {
        final ArchRule rule = classes()
                .that().resideOutsideOfPackage("..commons.application.usecases..")
                .and().resideInAPackage("..application.usecases..")
                .and().areNotInterfaces()
                .and().areNotMemberClasses()
                .should().haveSimpleNameEndingWith("UseCase")
                .because("Use cases must be named *UseCase");

        rule.check(classes);
    }

    @Test
    void useCasesShouldBeInCorrectPackage() {
        final ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("UseCase")
                .and().areNotInterfaces()
                .should().resideInAPackage("..application.usecases..")
                .because("Use cases must be in *.application.usecases package");

        rule.check(classes);
    }

    @Test
    void controllersShouldBeNamedCorrectly() {
        final ArchRule rule = classes()
                .that().resideInAPackage("..infrastructure.rest..")
                .and().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .should().haveSimpleNameEndingWith("Controller")
                .because("REST controllers must be named *Controller");

        rule.check(classes);
    }

    @Test
    void controllersShouldBeInCorrectPackage() {
        final ArchRule rule = classes()
                .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .should().resideInAPackage("..infrastructure.rest..")
                .because("Controllers must be in *.infrastructure.rest package");

        rule.check(classes);
    }

    @Test
    void repositoryInterfacesShouldBeInApplicationLayer() {
        final ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Repository")
                .and().resideOutsideOfPackage("..infrastructure.jpa.repositories..")
                .and().areInterfaces()
                .should().resideInAPackage("..application.repositories..")
                .because("Repository interfaces must be in *.application.repositories package");

        rule.check(classes);
    }

    @Test
    void repositoryImplementationsShouldBeInInfrastructureLayer() {
        final ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("RepositoryImpl")
                .should().resideInAPackage("..infrastructure.repositories..")
                .because("Repository implementations must be in *.infrastructure.repositories package");

        rule.check(classes);
    }

    @Test
    void domainEventsShouldExtendAbstractDomainEvent() {
        final ArchRule rule = classes()
                .that().resideInAPackage("..domain..")
                .and().haveSimpleNameEndingWith("Created")
                .or().haveSimpleNameEndingWith("Updated")
                .or().haveSimpleNameEndingWith("Deleted")
                .should().beAssignableTo("br.com.logistics.tms.commons.domain.AbstractDomainEvent")
                .because("Domain events must extend AbstractDomainEvent");

        rule.check(classes);
    }

    @Test
    void domainEventsShouldBeInDomainPackage() {
        final ArchRule rule = classes()
                .that().areAssignableTo("br.com.logistics.tms.commons.domain.AbstractDomainEvent")
                .and().resideOutsideOfPackage("..commons..")
                .should().resideInAPackage("..domain..")
                .because("Domain events must be in *.domain package");

        rule.check(classes);
    }

    @Test
    void aggregatesShouldExtendAbstractAggregateRoot() {
        final ArchRule rule = classes()
                .that().resideInAPackage("..domain..")
                .and().areNotInterfaces()
                .and().areNotEnums()
                .and().doNotHaveSimpleName("AbstractAggregateRoot")
                .and().areAssignableTo("br.com.logistics.tms.commons.domain.AbstractAggregateRoot")
                .should().haveSimpleNameNotEndingWith("Event")
                .andShould().haveSimpleNameNotEndingWith("Id")
                .because("Aggregates must extend AbstractAggregateRoot");

        rule.check(classes);
    }

    @Test
    void jpaEntitiesShouldBeInCorrectPackage() {
        final ArchRule rule = classes()
                .that().areAnnotatedWith("jakarta.persistence.Entity")
                .should().resideInAPackage("..infrastructure.jpa.entities..")
                .because("JPA entities must be in *.infrastructure.jpa.entities package");

        rule.check(classes);
    }

    @Test
    void jpaEntitiesShouldBeNamedWithEntitySuffix() {
        final ArchRule rule = classes()
                .that().resideInAPackage("..infrastructure.jpa.entities..")
                .and().areNotInterfaces()
                .and().areNotMemberClasses()
                .should().haveSimpleNameEndingWith("Entity")
                .because("JPA entities must be named *Entity");

        rule.check(classes);
    }

    @Test
    void listenersShouldBeNamedCorrectly() {
        final ArchRule rule = classes()
                .that().resideInAPackage("..infrastructure.listener..")
                .and().areNotInterfaces()
                .should().haveSimpleNameEndingWith("Listener")
                .because("Event listeners must be named *Listener");

        rule.check(classes);
    }

    @Test
    void listenersShouldBeInCorrectPackage() {
        final ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Listener")
                .and().resideOutsideOfPackage("..commons..")
                .should().resideInAPackage("..infrastructure.listener..")
                .because("Event listeners must be in *.infrastructure.listener package");

        rule.check(classes);
    }

    @Test
    void dtosShouldBeInInfrastructureDto() {
        final ArchRule rule = classes()
                .that().resideOutsideOfPackage("..infrastructure.spi.dto..")
                .and().haveSimpleNameEndingWith("DTO")
                .should().resideInAPackage("..infrastructure.dto..")
                .because("DTOs must be in *.infrastructure.dto package");

        rule.check(classes);
    }

    @Test
    void repositoryImplementationsShouldBeNamedCorrectly() {
        final ArchRule rule = classes()
                .that().resideInAPackage("..infrastructure.repositories..")
                .and().areNotInterfaces()
                .should(haveSimpleNameEndingWithAny("RepositoryImpl", "JpaRepository"))
                .because("Repository implementations must be named *RepositoryImpl or *JpaRepository");

        rule.check(classes);
    }
}
