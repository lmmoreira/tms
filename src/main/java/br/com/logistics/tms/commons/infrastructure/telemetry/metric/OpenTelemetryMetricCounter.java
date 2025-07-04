package br.com.logistics.tms.commons.infrastructure.telemetry.metric;

import br.com.logistics.tms.commons.infrastructure.telemetry.MetricCounter;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.LongCounter;

import java.util.Map;

public class OpenTelemetryMetricCounter implements MetricCounter {

    private LongCounter longCounter;
    private DoubleCounter doubleCounter;

    public OpenTelemetryMetricCounter(LongCounter longCounter) {
        this(longCounter, null);
    }

    public OpenTelemetryMetricCounter(DoubleCounter doubleCounter) {
        this(null, doubleCounter);
    }

    private OpenTelemetryMetricCounter(LongCounter longCounter, DoubleCounter doubleCounter) {
        this.longCounter = longCounter;
        this.doubleCounter = doubleCounter;
    }

    @Override
    public <T extends Number> void add(T value, Map<String, String> attributes) {

        if ((value instanceof Long) || (value instanceof Integer)) {
            add(value.longValue(), attributes);
        } else if (value instanceof Double) {
            add(value.doubleValue(), attributes);
        } else {
            throw new IllegalArgumentException("Unsupported number type: " + value.getClass().getName());
        }

    }

    private void add(long value, Map<String, String> attributes) {
        if (longCounter == null) {
            throw new IllegalStateException("LongCounter is not initialized.");
        }

        final AttributesBuilder attributesBuilder = Attributes.builder();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            attributesBuilder.put(AttributeKey.stringKey(entry.getKey()), entry.getValue());
        }

        longCounter.add(value, attributesBuilder.build());
    }

    private void add(double value, Map<String, String> attributes) {
        if (doubleCounter == null) {
            throw new IllegalStateException("DoubleCounter is not initialized.");
        }

        final AttributesBuilder attributesBuilder = Attributes.builder();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            attributesBuilder.put(AttributeKey.stringKey(entry.getKey()), entry.getValue());
        }

        doubleCounter.add(value, attributesBuilder.build());
    }

}
