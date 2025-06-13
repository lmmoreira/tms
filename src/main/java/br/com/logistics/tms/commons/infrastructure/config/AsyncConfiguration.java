package br.com.logistics.tms.commons.infrastructure.config;

import br.com.logistics.tms.commons.telemetry.TraceSpan;
import br.com.logistics.tms.commons.telemetry.Traceable;
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

    private final Traceable tracer;

    @Autowired
    public AsyncConfiguration(Traceable tracer) {
        this.tracer = tracer;
    }

    @Bean(name = DOMAIN_EVENT_QUEUE_GATEWAY_EXECUTOR)
    public AsyncTaskExecutor threadPoolTaskExecutor() {
        return new VirtualThreadTaskExecutor(DOMAIN_EVENT_QUEUE_GATEWAY_EXECUTOR) {
            @Override
            public void execute(Runnable task) {
                final Map<String, String> mdcContext = MDC.getCopyOfContextMap();
                final TraceSpan span = tracer.createSpan("GatewayExecutorTask", mdcContext);

                super.execute(() -> span.runWithinScope(() -> {
                    MDC.setContextMap(mdcContext);
                    task.run();
                }, MDC::clear));
            }
        };
    }

}