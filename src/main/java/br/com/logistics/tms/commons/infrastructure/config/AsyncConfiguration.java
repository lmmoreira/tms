package br.com.logistics.tms.commons.infrastructure.config;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfiguration {

    public final static String DOMAIN_EVENT_QUEUE_GATEWAY_EXECUTOR = "domainEventQueueGatewayExecutor";

    @Bean(name = DOMAIN_EVENT_QUEUE_GATEWAY_EXECUTOR)
    public AsyncTaskExecutor threadPoolTaskExecutor() {
        return new VirtualThreadTaskExecutor(DOMAIN_EVENT_QUEUE_GATEWAY_EXECUTOR) {
            @Override
            public void execute(Runnable task) {
                Context currentContext = Context.current();
                super.execute(() -> {
                    try (Scope scope = currentContext.makeCurrent()) {
                        task.run();
                    }
                });
            }
        };
    }

}