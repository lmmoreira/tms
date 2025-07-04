package br.com.logistics.tms.commons.infrastructure.telemetry.metric;

import br.com.logistics.tms.commons.infrastructure.telemetry.Counterable;
import br.com.logistics.tms.commons.infrastructure.telemetry.MetricCounter;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OpenTelemetryCounterAdapter implements Counterable {

    private final Meter meter;

    @Autowired
    public OpenTelemetryCounterAdapter(final Meter meter) {
        this.meter = meter;
    }

    @Override
    public MetricCounter createLongCounter(String name, String description) {
        final LongCounter longCounter = meter
                .counterBuilder(name)
                .setDescription(description)
                .build();

        return new OpenTelemetryMetricCounter(longCounter);
    }

    @Override
    public MetricCounter createDoubleCounter(String name, String description) {
        final DoubleCounter doubleCounter = meter
                .counterBuilder(name)
                .setDescription(description)
                .ofDoubles().build();

        return new OpenTelemetryMetricCounter(doubleCounter);
    }
}
