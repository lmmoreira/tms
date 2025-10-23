package br.com.logistics.tms.commons.domain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DomainEventRegistry {

    private static final Map<String, Class<?>> eventRegistryMap = new ConcurrentHashMap<>();

    public static Class<?> getClass(final String module, final String type) {
        final String className = "br.com.logistics.tms." + module + ".domain." + type;
        eventRegistryMap.putIfAbsent(className, parseClass(className));
        return eventRegistryMap.get(className);
    }

    private static Class<?> parseClass(final String className) {
        try {
            return Class.forName(className);
        } catch (Exception ex) {
            throw new RuntimeException("Error getting domain event class");
        }
    }

}
