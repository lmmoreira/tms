package br.com.logistics.tms.commons.application.gateways;

import br.com.logistics.tms.commons.domain.DomainEvent;

@FunctionalInterface
public interface DomainEventQueueGateway {

    void publish(DomainEvent content);
}