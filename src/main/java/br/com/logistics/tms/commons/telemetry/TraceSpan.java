package br.com.logistics.tms.commons.telemetry;

import java.util.function.Supplier;

public interface TraceSpan {

    void disposeAction();

    void runWithinScope(Runnable action);

    void runWithinScope(Runnable action, Runnable disposeAction);

    <T> T runWithinScopeAndReturn(Supplier<T> action);

    <T> T runWithinScopeAndReturn(Supplier<T> action, Runnable disposeAction);

}
