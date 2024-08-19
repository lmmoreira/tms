package br.com.logistics.tms;

import com.tngtech.archunit.core.domain.JavaClass;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

public class ModularityTests {

    final ApplicationModules modules = ApplicationModules.of(TmsApplication.class,
        JavaClass.Predicates.resideInAPackage("br.com.logistics.tms.commons.."));

    @Test
    void verifiesModularStructure() {
        modules.forEach(System.out::println);
        modules.verify();
    }

    @Test
    void createModuleDocumentation() {
        new Documenter(modules).writeDocumentation();
    }

}
