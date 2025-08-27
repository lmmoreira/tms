package br.com.logistics.tms.commons.infrastructure.config;

import br.com.logistics.tms.commons.infrastructure.gateways.RabbitMQCorrelationData;
import br.com.logistics.tms.commons.infrastructure.gateways.RabbitMQRoutingException;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (correlationData instanceof RabbitMQCorrelationData) {
                ((RabbitMQCorrelationData) correlationData).handleAck(ack, cause);
            }
        });

        rabbitTemplate.setReturnsCallback(returned -> {
            throw new RabbitMQRoutingException("Message returned: " +
                    returned.getMessage() +
                    ", replyCode: " + returned.getReplyCode() +
                    ", replyText: " + returned.getReplyText());
        });

        return rabbitTemplate;
    }

}