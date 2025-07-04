package br.com.logistics.tms.commons.infrastructure.telemetry;

import java.util.Map;

public interface Traceable {

    TraceSpan createSpan(String name, Map<String, String> context);

}
