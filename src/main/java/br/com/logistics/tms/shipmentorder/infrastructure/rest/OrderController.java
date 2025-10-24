package br.com.logistics.tms.shipmentorder.infrastructure.rest;

import br.com.logistics.tms.commons.domain.Id;
import br.com.logistics.tms.commons.infrastructure.telemetry.Counterable;
import br.com.logistics.tms.commons.infrastructure.telemetry.MetricCounter;
import br.com.logistics.tms.commons.infrastructure.usecases.RestUseCaseExecutor;
import br.com.logistics.tms.shipmentorder.application.GetOrderByCompanyIdUseCase;
import br.com.logistics.tms.shipmentorder.infrastructure.rest.dto.OrderDTO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping(value = "orders")
@Slf4j
public class OrderController {

    private final RestUseCaseExecutor restUseCaseExecutor;
    private final GetOrderByCompanyIdUseCase getOrderByCompanyIdUseCase;

    private final Counterable counterable;
    private final MetricCounter orderRequestCounter;

    @Autowired
    public OrderController(RestUseCaseExecutor restUseCaseExecutor, GetOrderByCompanyIdUseCase getOrderByCompanyIdUseCase, Counterable counterable) {
        this.getOrderByCompanyIdUseCase = getOrderByCompanyIdUseCase;
        this.counterable = counterable;
        this.restUseCaseExecutor = restUseCaseExecutor;

        this.orderRequestCounter = counterable.createLongCounter("tms.order.order_id_requests", "Counts order id requests");

        /*this.ordersInProcessGauge = meter
                .gaugeBuilder("tms.order.orders_in_process")
                .setDescription("Current number of orders being processed this moment")
                .setUnit("orders")
                .ofLongs()
                .build();

        this.orderProcessingTimeHistogram = meter
                .histogramBuilder("tms.order.processing_time")
                .setDescription("Distribution of order processing times")
                .setUnit("milliseconds")
                .build();*/

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

        /*
        ordersInProcessGauge.set(new Random().nextInt(10), Attributes.of(
                AttributeKey.stringKey("tms.order.orders_in_process_id"), id.toString(),
                AttributeKey.stringKey("tms.order.orders_in_process_client"), marketplace));

        orderProcessingTimeHistogram.record(new Random().nextInt(100, 1000), Attributes.of(
                AttributeKey.stringKey("tms.order.orders_in_process_id"), id.toString(),
                AttributeKey.stringKey("tms.order.orders_in_process_client"), marketplace));*/

        return new OrderDTO(Id.unique(), false, "12345678909", Instant.now(), Instant.now());


    }

    @GetMapping("/company/{id}")
    public Object getByCompany(@PathVariable String id) {
        return restUseCaseExecutor
                .from(getOrderByCompanyIdUseCase)
                .withInput(new GetOrderByCompanyIdUseCase.Input(id))
                .execute();
    }

}
