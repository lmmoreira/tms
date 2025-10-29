package br.com.logistics.tms.commons.infrastructure.database.routing;

import br.com.logistics.tms.commons.application.annotation.DatabaseRole;

public class DataSourceContextHolder {

    private static final ThreadLocal<DatabaseRole> CONTEXT = ThreadLocal.withInitial(() -> DatabaseRole.WRITE);

    public static DatabaseRole getDataSourceType() {
        return CONTEXT.get();
    }

    public static void clearReadOnlyContext() {
        CONTEXT.set(DatabaseRole.WRITE);
    }

    public static void markAsReadOnly() {
        CONTEXT.set(DatabaseRole.READ);
    }

    public static void markAsWrite() {
        CONTEXT.set(DatabaseRole.WRITE);
    }
}