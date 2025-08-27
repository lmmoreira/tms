package br.com.logistics.tms.commons.infrastructure.gateways;

import org.springframework.amqp.rabbit.connection.CorrelationData;

import java.util.Map;
import java.util.function.Consumer;

public class RabbitMQCorrelationData extends CorrelationData {

    private final Map<String, Object> metadata;
    private final Consumer<Map<String, Object>> onSuccess;
    private final Consumer<Map<String, Object>> onFailure;

    public RabbitMQCorrelationData(Map<String, Object> metadata,
                                   Consumer<Map<String, Object>> onSuccess,
                                   Consumer<Map<String, Object>> onFailure) {
        this.metadata = metadata;
        this.onSuccess = onSuccess;
        this.onFailure = onFailure;
    }

    public void handleAck(boolean ack, String cause) {
        if (ack) {
            onSuccess.accept(metadata);
        } else {
            onFailure.accept(metadata);
        }
    }

}