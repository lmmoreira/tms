package br.com.logistics.tms.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * ArchUnit tests to enforce dependency rules in TMS.
 * 
 * Dependency Rules:
 * - NO cyclic dependencies
 * - Constructor injection ONLY (no field injection)
 * - All injected fields are final
 * - Use cases implement UseCase interface
 * - NO ObjectMapper injection (use JsonSingleton)
 */
class DependencyRulesTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setup() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("br.com.logistics.tms");
    }

    @Test
    void domainPackageShouldBeFreeOfCycles() {
        final ArchRule rule = slices()
                .matching("..domain.(*)..")
                .should().beFreeOfCycles()
                .because("Domain package must be free of cyclic dependencies");

        rule.check(classes);
    }

    @Test
    void applicationPackageShouldBeFreeOfCycles() {
        final ArchRule rule = slices()
                .matching("..application.(*)..")
                .should().beFreeOfCycles()
                .because("Application package must be free of cyclic dependencies");

        rule.check(classes);
    }

    @Test
    void modulesShouldBeFreeOfCycles() {
        final ArchRule rule = slices()
                .matching("br.com.logistics.tms.(*)..")
                .should().beFreeOfCycles()
                .because("Modules must be free of cyclic dependencies");

        rule.check(classes);
    }

    @Test
    void noFieldInjection() {
        final ArchCondition<JavaClass> haveFieldInjection = new ArchCondition<>("have field injection") {
            @Override
            public void check(JavaClass clazz, ConditionEvents events) {
                for (JavaField field : clazz.getAllFields()) {
                    boolean hasAutowired = field.isAnnotatedWith("org.springframework.beans.factory.annotation.Autowired");
                    boolean hasInject = field.isAnnotatedWith("jakarta.inject.Inject");

                    if (hasAutowired || hasInject) {
                        String annotation = hasAutowired ? "@Autowired" : "@Inject";
                        String message = String.format(
                                "Class %s has field %s annotated with %s",
                                clazz.getName(),
                                field.getFullName(),
                                annotation
                        );
                        events.add(SimpleConditionEvent.violated(field, message));
                    }
                }
            }
        };

        final ArchRule rule = noClasses()
                .that().resideOutsideOfPackage("..commons..")
                .should(haveFieldInjection)
                .because("Field injection is not allowed - use constructor injection only");

        rule.check(classes);
    }

    @Test
    void injectedFieldsMustBeFinal() {
        final ArchRule rule = fields()
                .that().areDeclaredInClassesThat().resideInAPackage("..infrastructure..")
                .and().areDeclaredInClassesThat().areAnnotatedWith("org.springframework.stereotype.Component")
                .or().areDeclaredInClassesThat().areAnnotatedWith("org.springframework.stereotype.Service")
                .or().areDeclaredInClassesThat().areAnnotatedWith("org.springframework.stereotype.Repository")
                .or().areDeclaredInClassesThat().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .and().areNotStatic()
                .should().beFinal()
                .because("All injected fields must be final");

        rule.check(classes);
    }

    @Test
    void useCaseFieldsMustBeFinal() {
        final ArchRule rule = fields()
                .that().areDeclaredInClassesThat().resideInAPackage("..application.usecases..")
                .and().areDeclaredInClassesThat().haveSimpleNameEndingWith("UseCase")
                .and().areNotStatic()
                .should().beFinal()
                .because("Use case fields must be final");

        rule.check(classes);
    }

    @Test
    void repositoryFieldsMustBeFinal() {
        final ArchRule rule = fields()
                .that().areDeclaredInClassesThat().resideInAPackage("..infrastructure.repositories..")
                .and().areDeclaredInClassesThat().haveSimpleNameEndingWith("RepositoryImpl")
                .and().areNotStatic()
                .should().beFinal()
                .because("Repository implementation fields must be final");

        rule.check(classes);
    }

    @Test
    void listenerFieldsMustBeFinal() {
        final ArchRule rule = fields()
                .that().areDeclaredInClassesThat().resideInAPackage("..infrastructure.listener..")
                .and().areDeclaredInClassesThat().haveSimpleNameEndingWith("Listener")
                .and().areNotStatic()
                .should().beFinal()
                .because("Listener fields must be final");

        rule.check(classes);
    }

    @Test
    void controllerFieldsMustBeFinal() {
        final ArchRule rule = fields()
                .that().areDeclaredInClassesThat().resideInAPackage("..infrastructure.rest..")
                .and().areDeclaredInClassesThat().haveSimpleNameEndingWith("Controller")
                .and().areNotStatic()
                .should().beFinal()
                .because("Controller fields must be final");

        rule.check(classes);
    }

    @Test
    void useCasesShouldImplementUseCaseInterface() {
        final ArchRule rule = classes()
                .that().resideInAPackage("..application.usecases..")
                .and().haveSimpleNameEndingWith("UseCase")
                .and().areNotInterfaces()
                .should().implement("br.com.logistics.tms.commons.application.usecases.UseCase")
                .orShould().implement("br.com.logistics.tms.commons.application.usecases.VoidUseCase")
                .orShould().implement("br.com.logistics.tms.commons.application.usecases.NullaryUseCase")
                .because("Use cases must implement UseCase interface");

        rule.check(classes);
    }

    @Test
    void noClassesShouldInjectObjectMapper() {
        final ArchRule rule = noClasses()
                .that().resideOutsideOfPackage("..commons.infrastructure.mapper..")
                .and().resideOutsideOfPackage("..commons.infrastructure.json..")
                .and().resideOutsideOfPackage("..commons.infrastructure.config..")
                .should().dependOnClassesThat().haveFullyQualifiedName("com.fasterxml.jackson.databind.ObjectMapper")
                .because("Do not inject ObjectMapper - use JsonSingleton.getInstance() instead");

        rule.check(classes);
    }

    @Test
    void repositoryImplementationsShouldImplementRepositoryInterface() {
        final ArchCondition<JavaClass> implementRepoInterface = new ArchCondition<>("implement a repository interface from application layer") {
            @Override
            public void check(JavaClass clazz, ConditionEvents events) {
                boolean implementsAppRepo = clazz.getAllRawInterfaces().stream()
                        .anyMatch(iface -> iface.getPackageName().contains(".application.repositories"));

                if (!implementsAppRepo) {
                    String message = String.format(
                            "Class %s must implement an interface from a package containing '.application.repositories'",
                            clazz.getName()
                    );
                    events.add(SimpleConditionEvent.violated(clazz, message));
                }
            }
        };

        final ArchRule rule = classes()
                .that().resideInAPackage("..infrastructure.repositories..")
                .and().haveSimpleNameEndingWith("RepositoryImpl")
                .should(implementRepoInterface)
                .because("Repository implementations must implement repository interface from application layer");

        rule.check(classes);
    }

    @Test
    void applicationLayerShouldOnlyDependOnDomainAndCommons() {
        final ArchRule rule = classes()
                .that().resideInAPackage("..application..")
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                    "..application..",
                    "..domain..",
                    "..commons..",
                    "java.."
                )
                .because("Application layer should only depend on domain, commons and Java standard libraries");

        rule.check(classes);
    }

    @Test
    void domainLayerShouldOnlyDependOnDomainAndCommons() {
        final ArchRule rule = classes()
                .that().resideInAPackage("..domain..")
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                    "..domain..",
                    "..commons.domain..",
                    "java.."
                )
                .because("Domain layer should only depend on commons domain and Java standard libraries");

        rule.check(classes);
    }
}
