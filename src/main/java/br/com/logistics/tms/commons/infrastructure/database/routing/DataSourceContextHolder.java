package br.com.logistics.tms.commons.infrastructure.database.routing;

import br.com.logistics.tms.commons.infrastructure.config.properties.DataSourceProperties;

public class DataSourceContextHolder {

    private static final ThreadLocal<String> CONTEXT = ThreadLocal.withInitial(() -> DataSourceProperties.WRITE);

    public static String getDataSourceType() {
        return CONTEXT.get();
    }

    public static void clearReadOnlyContext() {
        CONTEXT.set(DataSourceProperties.WRITE);
    }

    public static void markAsReadOnly() {
        CONTEXT.set(DataSourceProperties.READ);
    }

    public static void markAsWrite() {
        CONTEXT.set(DataSourceProperties.WRITE);
    }
}