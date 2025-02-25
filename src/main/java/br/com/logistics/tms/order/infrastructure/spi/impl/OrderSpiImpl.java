package br.com.logistics.tms.order.infrastructure.spi.impl;

import br.com.logistics.tms.order.application.GetOrderByCompanyIdUseCase;
import br.com.logistics.tms.order.infrastructure.spi.OrderSpi;
import br.com.logistics.tms.order.infrastructure.spi.dto.OrderDTO;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderSpiImpl implements OrderSpi {

    @Autowired
    GetOrderByCompanyIdUseCase getOrderByCompanyIdUseCase;

    @Override
    public Set<OrderDTO> getOrderByCompanyId(String companyId) {

        log.info("Getting orders by company id on SPI: {}", companyId);

        return getOrderByCompanyIdUseCase.execute(new GetOrderByCompanyIdUseCase.Input(companyId)).order()
            .stream()
            .map(order -> new OrderDTO(order.id(), order.archived(), order.externalId(), order.createdAt(), order.updatedAt() ))
            .collect(Collectors.toSet());
    }
}
