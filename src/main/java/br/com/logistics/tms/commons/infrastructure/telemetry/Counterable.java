package br.com.logistics.tms.commons.infrastructure.telemetry;

public interface Counterable {

    MetricCounter createLongCounter(String name, String description);

    MetricCounter createDoubleCounter(String name, String description);

}
