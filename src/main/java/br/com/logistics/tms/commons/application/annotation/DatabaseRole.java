package br.com.logistics.tms.commons.application.annotation;

public enum DatabaseRole {
    READ, WRITE;

    public static boolean isReadOnly(Class<?> type) {
        final Cqrs cqrs = type.getAnnotation(Cqrs.class);
        return cqrs != null && cqrs.value() == DatabaseRole.READ;
    }
}