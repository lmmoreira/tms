package br.com.logistics.tms.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

public class ArchUnitConditions {

    public static ArchCondition<JavaClass> matchSimpleNamePattern(String regex) {
        return new ArchCondition<>("have simple name matching " + regex) {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                if (!javaClass.getSimpleName().matches(regex)) {
                    String message = String.format("Class %s does not match naming pattern %s",
                            javaClass.getSimpleName(), regex);
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }

    public static ArchCondition<JavaClass> haveSetters() {
        return new ArchCondition<>("have setters") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents conditionEvents) {
                for (JavaMethod method : javaClass.getMethods()) {
                    if (method.getName().startsWith("set") && method.getRawParameterTypes().size() == 1) {
                        String message = String.format("has setter method %s", method.getFullName());
                        conditionEvents.add(SimpleConditionEvent.violated(method, message));
                    }
                }
            }
        };
    }

    public static ArchCondition<JavaMethod> returnTheSameClassAsDeclaring() {
        return new ArchCondition<>("return same type as declaring class") {
            @Override
            public void check(final JavaMethod method, final ConditionEvents events) {
                final boolean ok = method.getReturnType().toErasure().equals(method.getOwner());
                final String message = ok
                        ? method.getFullName() + " returns same type as declaring class"
                        : method.getFullName() + " should return same type as declaring class but returns " + method.getReturnType().toErasure().getName();
                events.add(new SimpleConditionEvent(method, ok, message));
            }
        };
    }

    public static ArchCondition<JavaClass> haveStaticMethodNamed(String methodName) {
        return new ArchCondition<>("have static method named '" + methodName + "'") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean hasMethod = javaClass.getMethods().stream()
                        .anyMatch(m -> m.getName().equals(methodName) && m.getModifiers().contains(JavaModifier.STATIC));

                if (!hasMethod) {
                    events.add(SimpleConditionEvent.violated(javaClass,
                            javaClass.getSimpleName() + " should have static method '" + methodName + "'"));
                }
            }
        };
    }

    public static ArchCondition<JavaClass> haveMethodNamed(String methodName) {
        return new ArchCondition<>("have method named '" + methodName + "'") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean hasMethod = javaClass.getMethods().stream()
                        .anyMatch(m -> m.getName().equals(methodName));

                if (!hasMethod) {
                    events.add(SimpleConditionEvent.violated(javaClass,
                            javaClass.getSimpleName() + " should have method '" + methodName + "'"));
                }
            }
        };
    }

    public static ArchCondition<JavaClass> haveFieldOfTypeContaining(String... typeFragments) {
        return new ArchCondition<>("have field of type containing " + String.join(" or ", typeFragments)) {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean hasField = javaClass.getAllFields().stream()
                        .anyMatch(f -> {
                            String typeName = f.getRawType().getName();
                            for (String fragment : typeFragments) {
                                if (typeName.contains(fragment)) return true;
                            }
                            return false;
                        });

                if (!hasField) {
                    events.add(SimpleConditionEvent.violated(javaClass,
                            javaClass.getSimpleName() + " should have field of type containing one of: " + String.join(", ", typeFragments)));
                }
            }
        };
    }

    public static ArchCondition<JavaClass> haveFieldOfTypeExactly(String typeName) {
        return new ArchCondition<>("have field of type exactly '" + typeName + "'") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                final boolean hasField = javaClass.getAllFields().stream()
                        .anyMatch(f -> {
                            final String fqName = f.getRawType().getName();
                            final String simpleName = f.getRawType().getSimpleName();
                            return fqName.equals(typeName) || simpleName.equals(typeName);
                        });

                if (!hasField) {
                    events.add(SimpleConditionEvent.violated(javaClass,
                            javaClass.getSimpleName() + " should have field of type " + typeName));
                }
            }
        };
    }

    public static ArchCondition<JavaClass> haveFieldAnnotatedWith(String annotationName) {
        return new ArchCondition<>("have field annotated with " + annotationName) {
            @Override
            public void check(JavaClass javaClass, ConditionEvents conditionEvents) {
                boolean hasAnnotatedField = javaClass.getAllFields().stream()
                        .anyMatch(f -> f.isAnnotatedWith(annotationName));

                if (!hasAnnotatedField) {
                    conditionEvents.add(SimpleConditionEvent.violated(javaClass,
                            javaClass.getSimpleName() + " should have a field annotated with " + annotationName));
                }
            }

        };
    }

    public static ArchCondition<JavaClass> haveSimpleNameEndingWithAny(final String... suffixes) {
        return new ArchCondition<>("have simple name ending with " + java.util.Arrays.toString(suffixes)) {
            @Override
            public void check(final JavaClass clazz, final ConditionEvents events) {
                final String name = clazz.getSimpleName();
                final boolean matches = java.util.Arrays.stream(suffixes).anyMatch(name::endsWith);
                if (!matches) {
                    final String expected = java.util.Arrays.stream(suffixes)
                            .map(s -> "*" + s)
                            .collect(java.util.stream.Collectors.joining(" or "));
                    final String message = String.format("%s must be named %s", clazz.getName(), expected);
                    events.add(SimpleConditionEvent.violated(clazz, message));
                }
            }
        };
    }

}
