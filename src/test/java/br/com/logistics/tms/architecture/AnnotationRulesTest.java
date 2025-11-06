package br.com.logistics.tms.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

/**
 * ArchUnit tests to enforce annotation rules in TMS.
 * <p>
 * Annotation Rules:
 * - Use cases: @DomainService + @Cqrs
 * - Controllers: @RestController + @Cqrs
 * - Listeners: @Component + @Lazy(false) + @Cqrs(WRITE)
 * - Repository implementations: @Repository
 * - Domain classes: NO Spring/JPA annotations
 */
class AnnotationRulesTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setup() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("br.com.logistics.tms");
    }

    @Test
    void useCasesShouldBeAnnotatedWithDomainService() {
        final ArchRule rule = classes()
                .that().resideInAPackage("..application.usecases..")
                .and().haveSimpleNameEndingWith("UseCase")
                .and().areNotInterfaces()
                .should().beAnnotatedWith("br.com.logistics.tms.commons.application.annotation.DomainService")
                .because("Use cases must be annotated with @DomainService");

        rule.check(classes);
    }

    @Test
    void useCasesShouldBeAnnotatedWithCqrs() {
        final ArchRule rule = classes()
                .that().resideInAPackage("..application.usecases..")
                .and().haveSimpleNameEndingWith("UseCase")
                .and().areNotInterfaces()
                .should().beAnnotatedWith("br.com.logistics.tms.commons.application.annotation.Cqrs")
                .because("Use cases must be annotated with @Cqrs");

        rule.check(classes);
    }

    @Test
    void controllersShouldBeAnnotatedWithRestController() {
        final ArchRule rule = classes()
                .that().resideInAPackage("..infrastructure.rest..")
                .and().haveSimpleNameEndingWith("Controller")
                .should().beAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .because("Controllers must be annotated with @RestController");

        rule.check(classes);
    }

    @Test
    void controllersShouldBeAnnotatedWithCqrs() {
        final ArchRule rule = classes()
                .that().resideInAPackage("..infrastructure.rest..")
                .and().haveSimpleNameEndingWith("Controller")
                .should().beAnnotatedWith("br.com.logistics.tms.commons.application.annotation.Cqrs")
                .because("Controllers must be annotated with @Cqrs");

        rule.check(classes);
    }

    @Test
    void listenersShouldBeAnnotatedWithComponent() {
        final ArchRule rule = classes()
                .that().resideInAPackage("..infrastructure.listener..")
                .and().haveSimpleNameEndingWith("Listener")
                .should().beAnnotatedWith("org.springframework.stereotype.Component")
                .because("Listeners must be annotated with @Component");

        rule.check(classes);
    }

    @Test
    void listenersShouldBeAnnotatedWithLazyFalse() {
        final ArchRule rule = classes()
                .that().resideInAPackage("..infrastructure.listener..")
                .and().haveSimpleNameEndingWith("Listener")
                .should().beAnnotatedWith("org.springframework.context.annotation.Lazy")
                .because("Listeners must be annotated with @Lazy(false)");

        rule.check(classes);
    }

    @Test
    void listenersShouldBeAnnotatedWithCqrs() {
        final ArchRule rule = classes()
                .that().resideInAPackage("..infrastructure.listener..")
                .and().haveSimpleNameEndingWith("Listener")
                .should().beAnnotatedWith("br.com.logistics.tms.commons.application.annotation.Cqrs")
                .because("Listeners must be annotated with @Cqrs");

        rule.check(classes);
    }

    @Test
    void repositoryImplementationsShouldBeAnnotatedWithComponent() {
        final ArchRule rule = classes()
                .that().resideInAPackage("..infrastructure.repositories..")
                .and().haveSimpleNameEndingWith("RepositoryImpl")
                .should().beAnnotatedWith("org.springframework.stereotype.Component")
                .because("Repository implementations must be annotated with @Component");

        rule.check(classes);
    }

    @Test
    void domainClassesShouldNotHaveSpringAnnotations() {
        final ArchRule rule = classes()
                .that().resideInAPackage("..domain..")
                .should().notBeAnnotatedWith("org.springframework.stereotype.Component")
                .andShould().notBeAnnotatedWith("org.springframework.stereotype.Service")
                .andShould().notBeAnnotatedWith("org.springframework.stereotype.Repository")
                .andShould().notBeAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .because("Domain classes must not have Spring annotations");

        rule.check(classes);
    }

    @Test
    void domainClassesShouldNotHaveJpaAnnotations() {
        final ArchRule rule = classes()
                .that().resideInAPackage("..domain..")
                .should().notBeAnnotatedWith("jakarta.persistence.Entity")
                .andShould().notBeAnnotatedWith("jakarta.persistence.Table")
                .andShould().notBeAnnotatedWith("jakarta.persistence.Id")
                .andShould().notBeAnnotatedWith("jakarta.persistence.Column")
                .because("Domain classes must not have JPA annotations");

        rule.check(classes);
    }

    @Test
    void applicationClassesShouldNotHaveJpaAnnotations() {
        final ArchRule rule = classes()
                .that().resideInAPackage("..application..")
                .should().notBeAnnotatedWith("jakarta.persistence.Entity")
                .andShould().notBeAnnotatedWith("jakarta.persistence.Table")
                .andShould().notBeAnnotatedWith("jakarta.persistence.Id")
                .because("Application classes must not have JPA annotations");

        rule.check(classes);
    }

    @Test
    void repositoryCreateMethodsShouldBeTransactional() {
        final ArchRule rule = methods()
                .that().areDeclaredInClassesThat().resideInAPackage("..infrastructure.repositories..")
                .and().haveNameMatching("create|update|save|delete")
                .and().arePublic()
                .should().notBeAnnotatedWith("org.springframework.transaction.annotation.Transactional")
                .because("Repository create/update/delete methods cannot be declared as @Transactional since transactions are managed by Use Cases");

        rule.check(classes);
    }

    @Test
    void notClassesAreAllowedToHaveSl4fAnnotation() {
        final ArchRule rule = noClasses()
                .should().beAnnotatedWith("lombok.extern.slf4j.Slf4j")
                .because("Because logs are handled by telemetry Slf4jLoggerAdapter");

        rule.check(classes);
    }

}
