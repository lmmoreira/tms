package br.com.logistics.tms.commons.application.gateways;

public interface QueueGateway {
    void publish(String router, String routingKey, Object content);
}