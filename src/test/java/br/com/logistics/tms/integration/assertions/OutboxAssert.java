package br.com.logistics.tms.integration.assertions;

import br.com.logistics.tms.commons.infrastructure.gateways.outbox.AbstractOutboxEntity;
import br.com.logistics.tms.commons.infrastructure.gateways.outbox.OutboxStatus;
import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.Assertions.assertThat;

public class OutboxAssert extends AbstractAssert<OutboxAssert, AbstractOutboxEntity> {

    private OutboxAssert(final AbstractOutboxEntity actual) {
        super(actual, OutboxAssert.class);
    }

    public static OutboxAssert assertThatOutbox(final AbstractOutboxEntity actual) {
        return new OutboxAssert(actual);
    }

    public OutboxAssert isPublished() {
        isNotNull();
        assertThat(actual.getStatus())
                .as("Outbox status")
                .isEqualTo(OutboxStatus.PUBLISHED);
        return this;
    }

    public OutboxAssert isNew() {
        isNotNull();
        assertThat(actual.getStatus())
                .as("Outbox status")
                .isEqualTo(OutboxStatus.NEW);
        return this;
    }

    public OutboxAssert isProcessing() {
        isNotNull();
        assertThat(actual.getStatus())
                .as("Outbox status")
                .isEqualTo(OutboxStatus.PROCESSING);
        return this;
    }

    public OutboxAssert isFailed() {
        isNotNull();
        assertThat(actual.getStatus())
                .as("Outbox status")
                .isEqualTo(OutboxStatus.FAILED);
        return this;
    }

    public OutboxAssert hasStatus(final OutboxStatus status) {
        isNotNull();
        assertThat(actual.getStatus())
                .as("Outbox status")
                .isEqualTo(status);
        return this;
    }

    public OutboxAssert hasEventType(final String eventType) {
        isNotNull();
        assertThat(actual.getType())
                .as("Outbox event type")
                .isEqualTo(eventType);
        return this;
    }

    public OutboxAssert hasAggregateId(final java.util.UUID aggregateId) {
        isNotNull();
        assertThat(actual.getAggregateId())
                .as("Outbox aggregateId")
                .isEqualTo(aggregateId);
        return this;
    }

    public OutboxAssert hasContent() {
        isNotNull();
        assertThat(actual.getContent())
                .as("Outbox content")
                .isNotNull()
                .isNotEmpty();
        return this;
    }

    public OutboxAssert contentContains(final String text) {
        isNotNull();
        assertThat(actual.getContent())
                .as("Outbox content")
                .contains(text);
        return this;
    }
}
