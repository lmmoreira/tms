package br.com.logistics.tms.commons.infrastructure.gateways;

import br.com.logistics.tms.commons.application.gateways.DomainEventQueueGateway;
import br.com.logistics.tms.commons.domain.DomainEvent;
import br.com.logistics.tms.commons.infrastructure.json.JsonSingleton;
import br.com.logistics.tms.commons.telemetry.Logable;
import lombok.AllArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import static br.com.logistics.tms.commons.infrastructure.config.AsyncConfiguration.DOMAIN_EVENT_QUEUE_GATEWAY_EXECUTOR;

@Component
@AllArgsConstructor
public class RabbitMQDomainEventQueueGateway implements DomainEventQueueGateway {

    private final String RABBIT_MQ_INTEGRATION_EXCHANGE = "tms.events";
    private final String RABBIT_MQ_INTEGRATION_ROUTING_KEY_PREFIX = "integration.";

    private final AmqpTemplate amqpTemplate;
    private final Logable logable;

    @Async(DOMAIN_EVENT_QUEUE_GATEWAY_EXECUTOR)
    @Override
    public void publish(DomainEvent content) {
        final String routingKey = RABBIT_MQ_INTEGRATION_ROUTING_KEY_PREFIX.concat(content.module()).concat(".").concat(content.type());
        amqpTemplate.convertAndSend(RABBIT_MQ_INTEGRATION_EXCHANGE, routingKey, JsonSingleton.getInstance().toJson(content));
        logable.info(getClass(), "Publishing to RabbitMQ router {} key {} content {}", RABBIT_MQ_INTEGRATION_EXCHANGE,
                routingKey, JsonSingleton.getInstance().toJson(content));
    }
}
