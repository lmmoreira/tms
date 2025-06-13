package br.com.logistics.tms.commons.infrastructure.telemetry;

import br.com.logistics.tms.commons.telemetry.TraceSpan;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;

import java.util.function.Supplier;

public class OpenTelemetryTraceSpan implements TraceSpan {

    private final Span span;

    public OpenTelemetryTraceSpan(Span span) {
        this.span = span;
    }

    @Override
    public void disposeAction() {
        this.span.end();
    }

    @Override
    public void runWithinScope(Runnable action) {
        runWithinScope(action, null);
    }

    @Override
    public void runWithinScope(Runnable action, Runnable disposeAction) {

        try (Scope scope = span.makeCurrent()) {
            action.run();
        } finally {
            disposeAction();

            if (disposeAction != null) {
                disposeAction.run();
            }
        }

    }

    @Override
    public <T> T runWithinScopeAndReturn(Supplier<T> action) {
        return runWithinScopeAndReturn(action, null);
    }

    @Override
    public <T> T runWithinScopeAndReturn(Supplier<T> action, Runnable disposeAction) {
        try (Scope scope = span.makeCurrent()) {
            return action.get();
        } finally {
            disposeAction();

            if (disposeAction != null) {
                disposeAction.run();
            }
        }
    }

}
