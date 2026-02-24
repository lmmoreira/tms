# Prompt: Add Event Listener

## ⚡ TL;DR

- **When:** Module needs to react to events from another module
- **Why:** Event-driven inter-module communication without direct coupling
- **Pattern:** `@RabbitListener(queues = "integration.module.event") + VoidUseCaseExecutor`
- **See:** Read on for complete template

---

## Purpose
Template for creating a new event listener that enables module-to-module communication.

---

## Instructions for AI Assistant

Create a complete event listener implementation following TMS event-driven patterns.

### Required Information

**Event Details:**
- **Event Name:** `[EventName]` (e.g., ShipmentOrderCreated, CompanyUpdated)
- **Source Module:** `[module-name]` (where event originates)
- **Target Module:** `[module-name]` (where listener exists)
- **Description:** `[Brief description of what should happen when event occurs]`

**Event Payload:**
```
- field1: Type (description)
- field2: Type (description)
```

**Action to Perform:**
```
- Use Case: [UseCaseName] (e.g., IncrementShipmentOrderUseCase)
- Input: [What data to pass to use case]
- Expected Outcome: [What should change]
```

---

## Implementation Checklist

AI Assistant should generate:

### 1. Event DTO (Infrastructure Layer)
- [ ] Location: `{target-module}/infrastructure/dto/{EventName}DTO.java`
- [ ] Record with `@JsonProperty` annotations
- [ ] All fields from event payload

### 2. Event Listener (Infrastructure Layer)
- [ ] Location: `{target-module}/infrastructure/listener/{EventName}Listener.java`
- [ ] `@Component` annotation
- [ ] `@Cqrs(DatabaseRole.WRITE)` annotation
- [ ] `@Lazy(false)` annotation (ensure registration at startup)
- [ ] `@RabbitListener` with correct queue name
- [ ] Inject `VoidUseCaseExecutor` and target use case
- [ ] Handle message acknowledgment (basicAck/basicNack)
- [ ] Error handling with logging

### 3. Queue Configuration (if new)
- [ ] Location: `{target-module}/infrastructure/config/RabbitMQConfig.java`
- [ ] Queue declaration: `integration.{target-module}.{event-source}`
- [ ] Binding to exchange

### 4. Use Case (if doesn't exist)
- [ ] Location: `{target-module}/application/usecases/{Action}UseCase.java`
- [ ] Implement `VoidUseCase<Input>`
- [ ] `@DomainService` + `@Cqrs(DatabaseRole.WRITE)`
- [ ] Business logic to handle event

---

## Example Template

```java
// 1. Event DTO
package br.com.logistics.tms.{target-module}.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record {EventName}DTO(
    @JsonProperty("field1") Type field1,
    @JsonProperty("field2") Type field2
) {}

// 2. Event Listener
package br.com.logistics.tms.{target-module}.infrastructure.listener;

import br.com.logistics.tms.commons.application.VoidUseCaseExecutor;
import br.com.logistics.tms.commons.application.annotations.Cqrs;
import br.com.logistics.tms.commons.application.annotations.DatabaseRole;
import br.com.logistics.tms.{target-module}.application.usecases.{Action}UseCase;
import br.com.logistics.tms.{target-module}.infrastructure.dto.{EventName}DTO;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Cqrs(DatabaseRole.WRITE)
@Lazy(false)
public class {EventName}Listener {

    private static final Logger logger = LoggerFactory.getLogger({EventName}Listener.class);

    private final VoidUseCaseExecutor voidUseCaseExecutor;
    private final {Action}UseCase {actionUseCase};

    public {EventName}Listener(VoidUseCaseExecutor voidUseCaseExecutor,
                              {Action}UseCase {actionUseCase}) {
        this.voidUseCaseExecutor = voidUseCaseExecutor;
        this.{actionUseCase} = {actionUseCase};
    }

    @RabbitListener(queues = "integration.{target-module}.{event-source}")
    public void handle({EventName}DTO dto,
                      Message message,
                      Channel channel,
                      @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            logger.info("Processing {} event: {}", "{EventName}", dto);

            voidUseCaseExecutor
                .from({actionUseCase})
                .withInput(new {Action}UseCase.Input(dto.field1()))
                .execute();

            channel.basicAck(deliveryTag, false);
            logger.info("Successfully processed {} event", "{EventName}");

        } catch (Exception e) {
            logger.error("Error processing {} event: {}", "{EventName}", dto, e);
            try {
                channel.basicNack(deliveryTag, false, true); // Requeue on error
            } catch (IOException ioException) {
                logger.error("Failed to nack message", ioException);
            }
        }
    }
}

// 3. Queue Configuration (add to existing config)
@Bean
public Queue {eventSource}Queue() {
    return QueueBuilder.durable("integration.{target-module}.{event-source}").build();
}

@Bean
public Binding {eventSource}Binding(Queue {eventSource}Queue, TopicExchange {sourceModule}Exchange) {
    return BindingBuilder.bind({eventSource}Queue)
        .to({sourceModule}Exchange)
        .with("integration.{source-module}.{event-name}");
}
```

---

## Queue Naming Convention

**Pattern:** `integration.{target-module}.{event-source}`

**Examples:**
- `integration.company.shipmentorder.created`
- `integration.shipmentorder.company.updated`
- `integration.quotation.company.deleted`

**Rules:**
- Target module comes first
- Event source describes where event comes from
- Use lowercase with dots

---

## Testing Event Listener

```java
@SpringBootTest
@Testcontainers
class {EventName}ListenerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private {TargetModule}Repository repository;

    @Test
    void shouldHandle{EventName}Event() throws InterruptedException {
        // Given
        {EventName}DTO dto = new {EventName}DTO(field1, field2);

        // When
        rabbitTemplate.convertAndSend(
            "integration.{source-module}.events",
            "integration.{source-module}.{event-name}",
            dto
        );

        // Then - Wait for async processing
        Thread.sleep(2000);

        // Verify expected changes
        // assertThat(...)
    }
}
```

---

## Error Handling Strategies

### Strategy 1: Requeue on Error (Default)
```java
channel.basicNack(deliveryTag, false, true); // true = requeue
```
Use when: Transient errors (database connection, etc.)

### Strategy 2: Dead Letter Queue
```java
channel.basicNack(deliveryTag, false, false); // false = send to DLQ
```
Use when: Permanent errors (validation, business rule violations)

### Strategy 3: Retry with Delay
```java
@RabbitListener(queues = "...", containerFactory = "retryContainerFactory")
```
Configure retry policy in container factory.

---

## Common Pitfalls

❌ **Forgetting @Lazy(false):** Listener won't register at startup
❌ **Wrong Queue Name:** Event won't be received
❌ **Not Acknowledging:** Messages stay in queue
❌ **Throwing Exceptions:** Can cause infinite requeue loops
❌ **Long Processing:** Blocks other messages, use async processing if needed

---

## Best Practices

✅ **Idempotency:** Ensure use case can handle duplicate events
✅ **Logging:** Log event receipt, processing, and completion
✅ **Error Handling:** Always wrap in try-catch
✅ **Acknowledgment:** Always ack or nack
✅ **Timeouts:** Set reasonable processing timeouts
✅ **Monitoring:** Add metrics for event processing
