package br.com.logistics.tms.commons.infrastructure.database.transaction;

public class TransactionContextHolder {

    private static final ThreadLocal<Boolean> READ_ONLY_CONTEXT = ThreadLocal.withInitial(() -> Boolean.FALSE);

    public static Boolean isReadOnly() {
        return READ_ONLY_CONTEXT.get();
    }

    public static void markAsReadOnly() {
        READ_ONLY_CONTEXT.set(Boolean.TRUE);
    }

    public static void markAsReadWrite() {
        READ_ONLY_CONTEXT.set(Boolean.FALSE);
    }

    public static void clearReadOnlyContext() {
        READ_ONLY_CONTEXT.set(Boolean.FALSE);
    }

}