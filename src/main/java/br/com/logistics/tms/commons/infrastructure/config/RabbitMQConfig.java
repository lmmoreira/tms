package br.com.logistics.tms.commons.infrastructure.config;

import br.com.logistics.tms.commons.infrastructure.gateways.RabbitMQCorrelationData;
import br.com.logistics.tms.commons.infrastructure.gateways.RabbitMQRoutingException;
import br.com.logistics.tms.commons.infrastructure.json.JsonSingleton;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter(JsonSingleton.registeredMapper());
    }

    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter);
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