package br.com.logistics.tms.commons.infrastructure.telemetry;

public interface Logable {

    void info(Class<?> clazz, String var1, Object... var2);

    void error(Class<?> clazz, String var1, Object... var2);

}
