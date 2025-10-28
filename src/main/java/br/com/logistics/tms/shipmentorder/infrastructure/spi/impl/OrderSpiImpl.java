package br.com.logistics.tms.shipmentorder.infrastructure.spi.impl;

import br.com.logistics.tms.shipmentorder.application.usecases.GetShipmentOrderByCompanyIdUseCase;
import br.com.logistics.tms.shipmentorder.infrastructure.spi.OrderSpi;
import br.com.logistics.tms.shipmentorder.infrastructure.spi.dto.OrderDTO;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderSpiImpl implements OrderSpi {

    @Autowired
    GetShipmentOrderByCompanyIdUseCase getShipmentOrderByCompanyIdUseCase;

    @Override
    public Set<OrderDTO> getOrderByCompanyId(UUID companyId) {

        log.info("Getting orders by company id on SPI: {}", companyId);

        return getShipmentOrderByCompanyIdUseCase.execute(new GetShipmentOrderByCompanyIdUseCase.Input(companyId, 1, 10)).shipmentOrders()
            .stream()
            .map(order -> new OrderDTO(order.shipmentOrderId(), false, order.externalId(), order.createdAt(), null))
            .collect(Collectors.toSet());
    }
}
