package br.com.logistics.tms.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static br.com.logistics.tms.architecture.ArchUnitConditions.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

/**
 * ArchUnit tests to enforce infrastructure layer rules in TMS.
 * <p>
 * Infrastructure Rules:
 * - JPA entities in *.infrastructure.jpa.entities
 * - JPA entities have @Entity
 * - JPA entities have from(domain) static method
 * - JPA entities have toDomain() method
 * - DTOs in *.infrastructure.dto
 * - DTOs are records
 * - Controllers use RestUseCaseExecutor
 * - Listeners use VoidUseCaseExecutor
 */
class InfrastructureRulesTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setup() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("br.com.logistics.tms");
    }

    @Test
    void jpaEntitiesShouldBeInCorrectPackage() {
        final ArchRule rule = classes()
                .that().areAnnotatedWith("jakarta.persistence.Entity")
                .should().resideInAPackage("..infrastructure.jpa.entities..")
                .because("JPA entities must be in *.infrastructure.jpa.entities");

        rule.check(classes);
    }

    @Test
    void jpaEntitiesShouldHaveEntityAnnotation() {
        final ArchRule rule = classes()
                .that().resideInAPackage("..infrastructure.jpa.entities..")
                .and().haveSimpleNameEndingWith("Entity")
                .should().beAnnotatedWith("jakarta.persistence.Entity")
                .because("JPA entities must have @Entity annotation");

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
    void dtosShouldBeRecords() {
        final ArchRule rule = classes()
                .that().resideInAPackage("..infrastructure.dto..")
                .and().haveSimpleNameEndingWith("DTO")
                .should().beRecords()
                .because("DTOs should be immutable records");

        rule.check(classes);
    }

    @Test
    void controllersShouldNotContainBusinessLogic() {
        final ArchRule rule = methods()
                .that().areDeclaredInClassesThat().resideInAPackage("..infrastructure.rest..")
                .and().areDeclaredInClassesThat().haveSimpleNameEndingWith("Controller")
                .and().arePublic()
                .should().haveRawReturnType("java.lang.Object")
                .orShould().haveRawReturnType("org.springframework.http.ResponseEntity")
                .because("Controllers should delegate to use cases via RestUseCaseExecutor and custom presenters");

        rule.check(classes);
    }

    @Test
    void jpaEntitiesShouldHaveTableAnnotation() {
        final ArchRule rule = classes()
                .that().areAnnotatedWith("jakarta.persistence.Entity")
                .should().beAnnotatedWith("jakarta.persistence.Table")
                .because("JPA entities should have @Table annotation with schema");

        rule.check(classes);
    }

    @Test
    void listenerMethodsShouldHaveRabbitListenerAnnotation() {
        final ArchRule rule = methods()
                .that().areDeclaredInClassesThat().haveSimpleNameEndingWith("Listener")
                .and().areDeclaredInClassesThat().resideInAPackage("..infrastructure.listener..")
                .and().haveNameMatching("handle")
                .should().beAnnotatedWith("org.springframework.amqp.rabbit.annotation.RabbitListener")
                .because("Listener handle methods must have @RabbitListener annotation");

        rule.check(classes);
    }

    @Test
    void controllersShouldUsePostMappingOrGetMapping() {
        final ArchRule rule = methods()
                .that().areDeclaredInClassesThat().resideInAPackage("..infrastructure.rest..")
                .and().areDeclaredInClassesThat().haveSimpleNameEndingWith("Controller")
                .and().arePublic()
                .should().beAnnotatedWith("org.springframework.web.bind.annotation.PostMapping")
                .orShould().beAnnotatedWith("org.springframework.web.bind.annotation.GetMapping")
                .orShould().beAnnotatedWith("org.springframework.web.bind.annotation.PutMapping")
                .orShould().beAnnotatedWith("org.springframework.web.bind.annotation.DeleteMapping")
                .orShould().beAnnotatedWith("org.springframework.web.bind.annotation.PatchMapping")
                .orShould().beAnnotatedWith("org.springframework.web.bind.annotation.RequestMapping")
                .because("Controller methods must have HTTP mapping annotations");

        rule.check(classes);
    }

    @Test
    void jpaRepositoriesShouldExtendJpaRepository() {
        final ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("JpaRepository")
                .and().resideInAPackage("..infrastructure.jpa.repositories..")
                .should().beInterfaces()
                .andShould().beAssignableTo("org.springframework.data.jpa.repository.JpaRepository")
                .because("JPA repositories must extend Spring Data JpaRepository");

        rule.check(classes);
    }

    @Test
    void infrastructureClassesShouldNotAccessDomainDirectly() {
        final ArchRule rule = classes()
                .that().resideInAPackage("..infrastructure..")
                .and().resideOutsideOfPackage("..infrastructure.repositories..")
                .and().resideOutsideOfPackage("..infrastructure.jpa.entities..")
                .should().onlyAccessClassesThat(
                        com.tngtech.archunit.base.DescribedPredicate.describe(
                                "are not aggregates",
                                javaClass -> !javaClass.isAssignableTo("br.com.logistics.tms.commons.domain.AbstractAggregateRoot") ||
                                        javaClass.getPackageName().contains(".infrastructure.repositories")
                        )
                )
                .because("Infrastructure (except repositories) should not directly manipulate aggregates - only through usecases");

        rule.check(classes);
    }

    @Test
    void controllersShouldHaveRestUseCaseExecutor() {
        ArchRule rule = classes()
                .that().resideInAPackage("..infrastructure.rest..")
                .and().haveSimpleNameEndingWith("Controller")
                .should(haveFieldOfTypeContaining("RestUseCaseExecutor", "RestPresenter"))
                .because("Controllers should use RestUseCaseExecutor or DefaultRestPresenter");

        rule.check(classes);
    }

    @Test
    void listenersShouldHaveVoidUseCaseExecutor() {
        ArchRule rule = classes()
                .that().resideInAPackage("..infrastructure.listener..")
                .and().haveSimpleNameEndingWith("Listener")
                .should(haveFieldOfTypeExactly("VoidUseCaseExecutor"))
                .because("Listeners should use VoidUseCaseExecutor");

        rule.check(classes);
    }

    @Test
    void jpaEntitiesShouldHaveIdAnnotation() {
        ArchRule rule = classes()
                .that().areAnnotatedWith("jakarta.persistence.Entity")
                .should(haveFieldAnnotatedWith("jakarta.persistence.Id"))
                .because("JPA entities must have a field annotated with @Id");

        rule.check(classes);
    }

}
