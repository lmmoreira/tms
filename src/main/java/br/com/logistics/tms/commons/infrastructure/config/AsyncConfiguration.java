package br.com.logistics.tms.commons.infrastructure.config;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Map;

@Configuration
@EnableAsync
public class AsyncConfiguration {

    public final static String DOMAIN_EVENT_QUEUE_GATEWAY_EXECUTOR = "domainEventQueueGatewayExecutor";

    private OpenTelemetry openTelemetry;
    private Tracer tracer;

    @Autowired
    public AsyncConfiguration(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
        this.tracer = openTelemetry.getTracer("tmsTracer");
    }

    @Bean(name = DOMAIN_EVENT_QUEUE_GATEWAY_EXECUTOR)
    public AsyncTaskExecutor threadPoolTaskExecutor() {
        return new VirtualThreadTaskExecutor(DOMAIN_EVENT_QUEUE_GATEWAY_EXECUTOR) {
            @Override
            public void execute(Runnable task) {
                Context parentContext = Context.current();
                final Map<String, String> mdcContext = MDC.getCopyOfContextMap();

                super.execute(() -> {
                    Span span = tracer.spanBuilder("GatewayExecutorTask").setParent(parentContext).startSpan();
                    span.setAttribute("request_id", mdcContext.get("request_id"));
                    span.setAttribute("correlation_id", mdcContext.get("correlation_id"));

                    try (Scope scope = span.makeCurrent()) {
                        if (mdcContext != null) {
                            MDC.setContextMap(mdcContext);
                        }
                        task.run();
                    } finally {
                        span.end();
                        MDC.clear();
                    }
                });
            }
        };
    }

}