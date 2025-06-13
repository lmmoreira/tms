package br.com.logistics.tms.commons.infrastructure.telemetry;

import br.com.logistics.tms.commons.telemetry.TraceSpan;
import br.com.logistics.tms.commons.telemetry.Traceable;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OpenTelemetryTraceAdapter implements Traceable {

    private final Tracer tracer;

    @Autowired
    public OpenTelemetryTraceAdapter(final Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public TraceSpan createSpan(String name, Map<String, String> context) {
        final Context parentContext = Context.current();
        final Span span = tracer.spanBuilder(name).setParent(parentContext).startSpan();

        for (Map.Entry<String, String> entry : context.entrySet()) {
            span.setAttribute(entry.getKey(), entry.getValue());
        }

        return new OpenTelemetryTraceSpan(span);
    }
}
