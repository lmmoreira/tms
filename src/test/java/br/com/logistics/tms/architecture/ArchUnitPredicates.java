package br.com.logistics.tms.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;

import java.util.Objects;
import java.util.regex.Pattern;

public class ArchUnitPredicates {

    public static DescribedPredicate<JavaClass> matchSimpleNamePattern(final String regex) {
        final Pattern pattern = Pattern.compile(regex);

        return new DescribedPredicate<JavaClass>("have simple name matching pattern \"" + pattern.pattern() + "\"") {
            @Override
            public boolean test(final JavaClass javaClass) {
                if (Objects.isNull(javaClass)) return false;
                final String name = javaClass.getSimpleName();
                return name != null && pattern.matcher(name).matches();
            }
        };
    }

}
