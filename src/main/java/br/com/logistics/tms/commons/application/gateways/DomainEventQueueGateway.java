package br.com.logistics.tms.commons.application.gateways;

import br.com.logistics.tms.commons.domain.AbstractDomainEvent;

@FunctionalInterface
public interface DomainEventQueueGateway<T extends AbstractDomainEvent> {

    void publish(T content);
}