package br.com.logistics.tms.commons.infrastructure.gateways;

import br.com.logistics.tms.commons.application.gateways.DomainEventQueueGateway;
import br.com.logistics.tms.commons.domain.AbstractDomainEvent;
import br.com.logistics.tms.commons.infrastructure.json.JsonAdapter;
import br.com.logistics.tms.commons.telemetry.Logable;
import lombok.AllArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import static br.com.logistics.tms.commons.infrastructure.config.AsyncConfiguration.DOMAIN_EVENT_QUEUE_GATEWAY_EXECUTOR;

@Component
@AllArgsConstructor
public class RabbitMQDomainEventQueueGateway<T extends AbstractDomainEvent> implements DomainEventQueueGateway<T> {

    private final AmqpTemplate amqpTemplate;
    private final JsonAdapter jsonAdapter;
    private final Logable logable;

    @Async(DOMAIN_EVENT_QUEUE_GATEWAY_EXECUTOR)
    @Override
    public void publish(T content) {
        amqpTemplate.convertAndSend(content.router(), content.routingKey(),
                jsonAdapter.toJson(content));
        logable.info(getClass(),"Publishing to RabbitMQ router {} key {} content {}", content.router(),
                content.routingKey(), jsonAdapter.toJson(content));
    }
}
