package br.com.logistics.tms.commons.infrastructure.cqrs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class CqrsAnnotationUtils {

    public static boolean determineReadOnlyFromCqrsAnnotation(Object instance) {
        final Class<?> cls = instance.getClass();
        for (Annotation a : cls.getAnnotations()) {
            String name = a.annotationType().getSimpleName();
            if ("Cqrs".equals(name)) {
                try {
                    Method m = a.annotationType().getMethod("value");
                    Object val = m.invoke(a);
                    return ("READ".equals(val.toString()));
                } catch (Exception exception) {
                    return false;
                }
            }
        }
        return false;
    }

}
