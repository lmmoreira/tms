package br.com.logistics.tms.order.infrastructure.rest;

import br.com.logistics.tms.order.application.GetOrderByCompanyIdUseCase;
import br.com.logistics.tms.order.application.GetOrderByCompanyIdUseCase.Input;
import br.com.logistics.tms.order.infrastructure.rest.dto.OrderDTO;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "orders")
@Slf4j
public class OrderController {

    @Autowired
    GetOrderByCompanyIdUseCase getOrderByCompanyIdUseCase;

    private static final Meter meter = GlobalOpenTelemetry.getMeter("tmsOrder");

    LongCounter orderRequestCounter  =   meter
            .counterBuilder("tms.order.order_id_requests")
            .setDescription("Counts order id requests")
          .build();

    @PostConstruct
    public void init() {
        log.info("Leo");
    }

    @GetMapping("/{id}")
    public OrderDTO get(@PathVariable Long id, @RequestHeader Map<String, String> headers) {
        log.info("Iai");
        orderRequestCounter.add(1, Attributes.of(AttributeKey.stringKey("tms.order.order_id_requests_id"), id.toString()));
        return new OrderDTO(1L, false,"12345678909", Instant.now(), Instant.now());
    }

    @GetMapping("/company/{id}")
    public Set<OrderDTO> getByCompany(@PathVariable String id) {
        return getOrderByCompanyIdUseCase.execute(new Input(id)).order()
            .stream()
            .map(order -> new OrderDTO(order.id(), order.archived(), order.externalId(), order.createdAt(), order.updatedAt()))
            .collect(Collectors.toSet());
    }

}
