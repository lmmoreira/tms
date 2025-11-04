package br.com.logistics.tms.commons.infrastructure.config;

import br.com.logistics.tms.commons.infrastructure.gateways.RabbitMQCorrelationData;
import br.com.logistics.tms.commons.infrastructure.gateways.RabbitMQRoutingException;
import br.com.logistics.tms.commons.infrastructure.json.JsonSingleton;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.policy.SimpleRetryPolicy;

import java.util.Map;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter(JsonSingleton.registeredMapper());
    }

    @Bean
    public RetryOperationsInterceptor optimisticLockRetryInterceptor() {
        final SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(5,
                Map.of(
                        ObjectOptimisticLockingFailureException.class, true
                ), true);

        final FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(500);

        return RetryInterceptorBuilder.stateless()
                .retryPolicy(retryPolicy)
                .backOffPolicy(backOffPolicy)
                .recoverer((message, cause) -> {
                    throw new AmqpRejectAndDontRequeueException("Not retryable", cause);
                })
                .build();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter converter) {
        final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(10);
        factory.setTaskExecutor(Thread::startVirtualThread);
        factory.setAdviceChain(optimisticLockRetryInterceptor());
        return factory;
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