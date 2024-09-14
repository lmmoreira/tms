package br.com.logistics.tms.company.infrastructure.gateways;

import br.com.logistics.tms.commons.application.gateways.QueueGateway;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQQueueGateway implements QueueGateway {

    @Override
    public void publish(String router, String routingKey, Object content) {
        System.out.println("Publishing to RabbitMQ router " + router + " content " + content);
    }
}
