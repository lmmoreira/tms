package br.com.logistics.tms.commons.infrastructure.gateways;

import static br.com.logistics.tms.commons.infrastructure.config.AsyncConfiguration.DOMAIN_EVENT_QUEUE_GATEWAY_EXECUTOR;

import br.com.logistics.tms.commons.application.gateways.DomainEventQueueGateway;
import br.com.logistics.tms.commons.domain.AbstractDomainEvent;
import br.com.logistics.tms.commons.infrastructure.json.JsonAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class RabbitMQDomainEventQueueGateway<T extends AbstractDomainEvent> implements
    DomainEventQueueGateway<T> {

    private final AmqpTemplate amqpTemplate;
    private final JsonAdapter jsonAdapter;

    @Async(DOMAIN_EVENT_QUEUE_GATEWAY_EXECUTOR)
    @Override
    public void publish(T content) {
        amqpTemplate.convertAndSend(content.router(), content.routingKey(),
            jsonAdapter.toJson(content));
        log.info("Publishing to RabbitMQ router {} key {} content {}", content.router(),
            content.routingKey(), jsonAdapter.toJson(content));
    }
}
