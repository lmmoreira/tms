package br.com.logistics.tms.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * ArchUnit tests to enforce strict layer boundaries in TMS architecture.
 * 
 * Layer Rules:
 * - Domain Layer: Pure Java only, NO framework dependencies
 * - Application Layer: Depends ONLY on domain + commons, NO frameworks
 * - Infrastructure Layer: All frameworks allowed
 */
class LayerArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setup() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("br.com.logistics.tms");
    }

    @Test
    void domainLayerShouldNotDependOnSpring() {
        final ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
                .because("Domain layer must be pure Java with no framework dependencies");

        rule.check(classes);
    }

    @Test
    void domainLayerShouldNotDependOnJPA() {
        final ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..")
                .because("Domain layer must not depend on JPA");

        rule.check(classes);
    }

    @Test
    void domainLayerShouldNotDependOnJackson() {
        final ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("com.fasterxml.jackson..")
                .because("Domain layer must not depend on Jackson");

        rule.check(classes);
    }

    @Test
    void domainLayerShouldNotDependOnRabbitMQ() {
        final ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework.amqp..")
                .because("Domain layer must not depend on RabbitMQ");

        rule.check(classes);
    }

    @Test
    void domainLayerShouldNotDependOnHibernate() {
        final ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.hibernate..")
                .because("Domain layer must not depend on Hibernate");

        rule.check(classes);
    }

    @Test
    void applicationLayerShouldNotDependOnSpring() {
        final ArchRule rule = noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
                .because("Application layer must not depend on Spring (except commons annotations)");

        rule.check(classes);
    }

    @Test
    void applicationLayerShouldNotDependOnJPA() {
        final ArchRule rule = noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..")
                .because("Application layer must not depend on JPA");

        rule.check(classes);
    }

    @Test
    void applicationLayerShouldNotDependOnJackson() {
        final ArchRule rule = noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage("com.fasterxml.jackson..")
                .because("Application layer must not depend on Jackson");

        rule.check(classes);
    }

    @Test
    void applicationLayerShouldNotDependOnHttp() {
        final ArchRule rule = noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework.web..", "jakarta.servlet..")
                .because("Application layer must not depend on HTTP");

        rule.check(classes);
    }

    @Test
    void domainLayerShouldNotDependOnApplicationLayer() {
        final ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..application..")
                .because("Domain must not depend on application layer");

        rule.check(classes);
    }

    @Test
    void domainLayerShouldNotDependOnInfrastructureLayer() {
        final ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .because("Domain must not depend on infrastructure layer");

        rule.check(classes);
    }

    @Test
    void applicationLayerShouldNotDependOnInfrastructureLayer() {
        final ArchRule rule = noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .because("Application must not depend on infrastructure layer");

        rule.check(classes);
    }

    @Test
    void layeredArchitectureShouldBeRespected() {
        final ArchRule rule = layeredArchitecture()
                .consideringAllDependencies()
                
                .layer("Domain").definedBy("..domain..")
                .layer("Application").definedBy("..application..")
                .layer("Infrastructure").definedBy("..infrastructure..")
                
                .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure")
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure")
                
                .because("Layered architecture must be respected: Infrastructure -> Application -> Domain");

        rule.check(classes);
    }
}
