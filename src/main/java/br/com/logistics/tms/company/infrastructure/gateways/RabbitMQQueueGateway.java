package br.com.logistics.tms.company.infrastructure.gateways;

import br.com.logistics.tms.commons.application.gateways.QueueGateway;

public class RabbitMQQueueGateway implements QueueGateway {

    @Override
    public void publish(String router, Object content) {
        System.out.println("Publishing to RabbitMQ router " + router + " content " + content);
    }
}
