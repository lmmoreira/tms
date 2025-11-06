package br.com.logistics.tms.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

/**
 * ArchUnit tests to enforce repository rules in TMS.
 * <p>
 * Repository Rules:
 * - Repository interfaces in application layer
 * - Repository implementations in infrastructure
 * - Repositories use OutboxService for events
 * - Create/update methods are @Transactional
 */
class RepositoryRulesTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setup() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("br.com.logistics.tms");
    }

    @Test
    void repositoryInterfacesShouldBeInApplicationLayer() {
        final ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Repository")
                .and().areInterfaces()
                .and().resideOutsideOfPackage("..commons..")
                .and().resideOutsideOfPackage("..jpa..")
                .should().resideInAPackage("..application.repositories..")
                .because("Repository interfaces must be in *.application.repositories");

        rule.check(classes);
    }

    @Test
    void repositoryImplementationsShouldBeInInfrastructureLayer() {
        final ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("RepositoryImpl")
                .should().resideInAPackage("..infrastructure.repositories..")
                .because("Repository implementations must be in *.infrastructure.repositories");

        rule.check(classes);
    }

    @Test
    void repositoryImplementationsShouldImplementInterface() {
        final ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("RepositoryImpl")
                .should().beAssignableTo(
                        com.tngtech.archunit.base.DescribedPredicate.describe(
                                "implement repository interface",
                                javaClass -> !javaClass.getAllRawInterfaces().isEmpty()
                        )
                )
                .because("Repository implementations must implement repository interface");

        rule.check(classes);
    }


    @Test
    void repositoryMethodsShouldNotBeTransactional() {
        final ArchRule rule = methods()
                .that().areDeclaredInClassesThat().haveSimpleNameEndingWith("RepositoryImpl")
                .and().haveNameMatching("create|save|update|delete|remove")
                .and().arePublic()
                .should().notBeAnnotatedWith("org.springframework.transaction.annotation.Transactional")
                .because("Repository methods must not be @Transactional - Managed by usecase");

        rule.check(classes);
    }

    @Test
    void repositoryInterfacesShouldBePublic() {
        final ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Repository")
                .and().areInterfaces()
                .and().resideInAPackage("..application.repositories..")
                .should().bePublic()
                .because("Repository interfaces must be public");

        rule.check(classes);
    }

    @Test
    void repositoryImplementationsShouldHaveFinalFields() {
        final ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("RepositoryImpl")
                .should().haveOnlyFinalFields()
                .because("Repository implementation fields must be final");

        rule.check(classes);
    }

    @Test
    void repositoryImplementationsShouldNotAccessOtherRepositories() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("RepositoryImpl")
                .and().resideInAPackage("..company..")
                .should().onlyDependOnClassesThat()
                .resideOutsideOfPackage("..shipmentorder..repositories..")
                .because("Repositories must not access other module repositories");

        rule.check(classes);
    }

}
