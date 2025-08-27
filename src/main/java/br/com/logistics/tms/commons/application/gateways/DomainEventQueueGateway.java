package br.com.logistics.tms.commons.application.gateways;

import br.com.logistics.tms.commons.domain.AbstractDomainEvent;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@FunctionalInterface
public interface DomainEventQueueGateway {

    void publish(AbstractDomainEvent content, UUID correlationId, Consumer<Map<String, Object>> onSuccess, Consumer<Map<String, Object>> onFailure);

}