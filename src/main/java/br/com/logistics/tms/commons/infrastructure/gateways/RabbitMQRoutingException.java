package br.com.logistics.tms.commons.infrastructure.gateways;

public class RabbitMQRoutingException extends RuntimeException {

    public RabbitMQRoutingException(String message) {
        super(message, null, true, false);
    }

    public RabbitMQRoutingException(String message, Throwable cause) {
        super(message, cause, true, false);
    }

}