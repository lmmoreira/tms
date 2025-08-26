package br.com.logistics.tms.commons.infrastructure.telemetry.log;

import br.com.logistics.tms.commons.application.usecases.UseCaseInterceptor;
import br.com.logistics.tms.commons.infrastructure.telemetry.Logable;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Component
public class Slf4jLoggerAdapter implements Logable, UseCaseInterceptor {

    final Map<Class<?>, Logger> loggerMap;

    public Slf4jLoggerAdapter() {
        this.loggerMap = new ConcurrentHashMap<>();
    }

    @Override
    public void info(Class<?> clazz, String var1, Object... var2) {
        loggerMap.putIfAbsent(clazz, org.slf4j.LoggerFactory.getLogger(clazz));
        loggerMap.get(clazz).info(var1, var2);
    }

    @Override
    public void error(Class<?> clazz, String var1, Object... var2) {
        loggerMap.putIfAbsent(clazz, org.slf4j.LoggerFactory.getLogger(clazz));
        loggerMap.get(clazz).error(var1, var2);
    }

    @Override
    public <T> T intercept(Supplier<T> next) {
        this.info(this.getClass(), "Starting logging interception");
        final T value = next.get();
        this.info(this.getClass(), "Ending logging interception");
        return value;
    }
}
