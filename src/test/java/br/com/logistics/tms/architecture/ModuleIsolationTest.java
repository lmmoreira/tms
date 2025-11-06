package br.com.logistics.tms.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * ArchUnit tests to enforce module isolation in TMS.
 *
 * Module Rules:
 * - Company module and ShipmentOrder module must NOT call each other directly
 * - Modules communicate ONLY via events (listeners + DTOs)
 * - Commons module can be accessed by all modules
 */
class ModuleIsolationTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setup() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("br.com.logistics.tms");
    }

    @Test
    void companyModuleShouldNotDependOnShipmentOrderModule() {
        final ArchRule rule = noClasses()
                .that().resideInAPackage("..company..")
                .should().dependOnClassesThat().resideInAPackage("..shipmentorder..")
                .because("Company module must not directly depend on ShipmentOrder module (use events)");

        rule.check(classes);
    }

    @Test
    void shipmentOrderModuleShouldNotDependOnCompanyModule() {
        final ArchRule rule = noClasses()
                .that().resideInAPackage("..shipmentorder..")
                .should().dependOnClassesThat().resideInAPackage("..company..")
                .because("ShipmentOrder module must not directly depend on Company module (use events)");

        rule.check(classes);
    }

    @Test
    void noModuleShouldAccessAnotherModules() {
        ArchRule companyRule = noClasses()
                .that().resideInAPackage("..company..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "..shipmentorder..")
                .because("Modules must not access other module's");

        ArchRule shipmentOrderRule = noClasses()
                .that().resideInAPackage("..shipmentorder..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "..company..")
                .because("Modules must not access other module's repositories");

        companyRule.check(classes);
        shipmentOrderRule.check(classes);
    }

    @Test
    void commonsModuleMayBeAccessedByAllModulesAndCantDependeOnIt() {
        ArchRule rule = ArchRuleDefinition.noClasses()
                .that().resideInAPackage("..commons..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..company..", "..shipmentorder..")
                .because("Commons must not depend on specific modules");

        rule.check(classes);
    }

}
