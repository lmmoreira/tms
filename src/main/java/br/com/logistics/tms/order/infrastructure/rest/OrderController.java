package br.com.logistics.tms.order.infrastructure.rest;

import br.com.logistics.tms.commons.telemetry.Counterable;
import br.com.logistics.tms.commons.telemetry.MetricCounter;
import br.com.logistics.tms.order.application.GetOrderByCompanyIdUseCase;
import br.com.logistics.tms.order.application.GetOrderByCompanyIdUseCase.Input;
import br.com.logistics.tms.order.infrastructure.rest.dto.OrderDTO;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongGauge;
import io.opentelemetry.api.metrics.Meter;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "orders")
@Slf4j
public class OrderController {

    private final GetOrderByCompanyIdUseCase getOrderByCompanyIdUseCase;
    private final OpenTelemetry openTelemetry;
    private final Meter meter;
    private final LongGauge ordersInProcessGauge;
    private final DoubleHistogram orderProcessingTimeHistogram;

    private final Counterable counterable;
    private final MetricCounter orderRequestCounter;

    @Autowired
    public OrderController(GetOrderByCompanyIdUseCase getOrderByCompanyIdUseCase, OpenTelemetry openTelemetry, Counterable counterable) {
        this.getOrderByCompanyIdUseCase = getOrderByCompanyIdUseCase;
        this.counterable = counterable;

        this.openTelemetry = openTelemetry;
        this.meter = openTelemetry.getMeter("tmsOrder");

        this.orderRequestCounter = counterable.createLongCounter("tms.order.order_id_requests", "Counts order id requests");

        this.ordersInProcessGauge = meter
                .gaugeBuilder("tms.order.orders_in_process")
                .setDescription("Current number of orders being processed this moment")
                .setUnit("orders")
                .ofLongs()
                .build();

        this.orderProcessingTimeHistogram = meter
                .histogramBuilder("tms.order.processing_time")
                .setDescription("Distribution of order processing times")
                .setUnit("milliseconds")
                .build();

    }

    @PostConstruct
    public void init() {
        log.info("Leo");
    }

    @GetMapping("/{id}")
    public OrderDTO get(@PathVariable Long id, @RequestHeader Map<String, String> headers) {
        log.info("Iai");

        String marketplace = new Random().nextBoolean() ? "shein" : "shopee";
        orderRequestCounter.add(1, Map.of("tms.order.order_id_requests_id", id.toString(), "tms.order.order_id_requests_client", marketplace));

        ordersInProcessGauge.set(new Random().nextInt(10), Attributes.of(
                AttributeKey.stringKey("tms.order.orders_in_process_id"), id.toString(),
                AttributeKey.stringKey("tms.order.orders_in_process_client"), marketplace));

        orderProcessingTimeHistogram.record(new Random().nextInt(100, 1000), Attributes.of(
                AttributeKey.stringKey("tms.order.orders_in_process_id"), id.toString(),
                AttributeKey.stringKey("tms.order.orders_in_process_client"), marketplace));

        return new OrderDTO(1L, false, "12345678909", Instant.now(), Instant.now());


    }

    @GetMapping("/company/{id}")
    public Set<OrderDTO> getByCompany(@PathVariable String id) {
        return getOrderByCompanyIdUseCase.execute(new Input(id)).order()
                .stream()
                .map(order -> new OrderDTO(order.id(), order.archived(), order.externalId(), order.createdAt(), order.updatedAt()))
                .collect(Collectors.toSet());
    }

}
