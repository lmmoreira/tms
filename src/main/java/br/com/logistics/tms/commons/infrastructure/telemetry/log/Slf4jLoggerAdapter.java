package br.com.logistics.tms.commons.infrastructure.telemetry.log;

import br.com.logistics.tms.commons.infrastructure.telemetry.Logable;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Slf4jLoggerAdapter implements Logable {

    final Map<Class<?>, Logger> loggerMap;

    public Slf4jLoggerAdapter() {
        this.loggerMap = new ConcurrentHashMap<>();
    }

    @Override
    public void info(Class<?> clazz, String var1, Object... var2) {
        loggerMap.putIfAbsent(clazz, org.slf4j.LoggerFactory.getLogger(clazz));
        loggerMap.get(clazz).info(var1, var2);
    }
}
