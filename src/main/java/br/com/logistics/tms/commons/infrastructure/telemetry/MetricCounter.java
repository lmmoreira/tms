package br.com.logistics.tms.commons.infrastructure.telemetry;

import java.util.Map;

public interface MetricCounter {

    <T extends Number> void add(T value, Map<String, String> attributes);

}
