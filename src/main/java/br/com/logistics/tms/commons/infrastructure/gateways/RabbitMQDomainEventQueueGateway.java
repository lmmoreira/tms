package br.com.logistics.tms.commons.infrastructure.gateways;

import br.com.logistics.tms.commons.application.gateways.DomainEventQueueGateway;
import br.com.logistics.tms.commons.domain.AbstractDomainEvent;
import br.com.logistics.tms.commons.infrastructure.json.JsonSingleton;
import br.com.logistics.tms.commons.infrastructure.telemetry.Logable;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static br.com.logistics.tms.commons.infrastructure.config.AsyncConfiguration.DOMAIN_EVENT_QUEUE_GATEWAY_EXECUTOR;

@Component
@AllArgsConstructor
public class RabbitMQDomainEventQueueGateway implements DomainEventQueueGateway {

    private final String RABBIT_MQ_INTEGRATION_EXCHANGE = "tms.events";
    private final String RABBIT_MQ_INTEGRATION_ROUTING_KEY_PREFIX = "integration.";

    private final RabbitTemplate rabbitTemplate;
    private final Logable logable;

    @Async(DOMAIN_EVENT_QUEUE_GATEWAY_EXECUTOR)
    @Override
    public void publish(AbstractDomainEvent content, UUID correlationId, Consumer<Map<String, Object>> onSuccess, Consumer<Map<String, Object>> onFailure) {
        final String routingKey = RABBIT_MQ_INTEGRATION_ROUTING_KEY_PREFIX.concat(content.getModule()).concat(".").concat(content.getType());
        final CorrelationData correlationData = new RabbitMQCorrelationData(Map.of("module", content.getModule(), "correlationId", correlationId), onSuccess, onFailure);
        rabbitTemplate.convertAndSend(RABBIT_MQ_INTEGRATION_EXCHANGE,
                routingKey,
                content,
                correlationData);
        logable.info(getClass(), "Publishing to RabbitMQ router {} key {} content {}", RABBIT_MQ_INTEGRATION_EXCHANGE,
                routingKey, JsonSingleton.getInstance().toJson(content));
    }
}
