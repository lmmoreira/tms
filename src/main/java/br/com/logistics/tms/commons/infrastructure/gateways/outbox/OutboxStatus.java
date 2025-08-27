package br.com.logistics.tms.commons.infrastructure.gateways.outbox;

public enum OutboxStatus {
    NEW,
    PROCESSING,
    PUBLISHED,
    FAILED
}