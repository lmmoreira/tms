package br.com.logistics.tms.order.infrastructure.rest;

import br.com.logistics.tms.order.application.GetOrderByCompanyIdUseCase;
import br.com.logistics.tms.order.application.GetOrderByCompanyIdUseCase.Input;
import br.com.logistics.tms.order.infrastructure.rest.dto.OrderDTO;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "orders")
public class OrderController {

    @Autowired
    GetOrderByCompanyIdUseCase getOrderByCompanyIdUseCase;

    @GetMapping("/{id}")
    public OrderDTO get(@PathVariable Long id) {
        return new OrderDTO(1L, "12345678909", Instant.now());
    }

    @GetMapping("/company/{id}")
    public Set<OrderDTO> getByCompany(@PathVariable Long id) {
        return getOrderByCompanyIdUseCase.execute(new Input(id)).order()
            .stream()
            .map(order -> new OrderDTO(order.id(), order.externalId(), order.date()))
            .collect(Collectors.toSet());
    }

}
