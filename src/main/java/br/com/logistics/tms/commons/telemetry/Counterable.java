package br.com.logistics.tms.commons.telemetry;

import java.util.Map;

public interface Counterable {

    MetricCounter createLongCounter(String name, String description);

    MetricCounter createDoubleCounter(String name, String description);

}
