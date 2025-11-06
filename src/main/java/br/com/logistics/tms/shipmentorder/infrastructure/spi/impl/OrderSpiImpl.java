package br.com.logistics.tms.shipmentorder.infrastructure.spi.impl;

import br.com.logistics.tms.shipmentorder.application.usecases.GetShipmentOrderByCompanyIdUseCase;
import br.com.logistics.tms.shipmentorder.infrastructure.spi.OrderSpi;
import br.com.logistics.tms.shipmentorder.infrastructure.spi.dto.OrderDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class OrderSpiImpl implements OrderSpi {

    private final GetShipmentOrderByCompanyIdUseCase getShipmentOrderByCompanyIdUseCase;

    @Override
    public Set<OrderDTO> getOrderByCompanyId(UUID companyId) {

        return getShipmentOrderByCompanyIdUseCase.execute(new GetShipmentOrderByCompanyIdUseCase.Input(companyId, 1, 10)).shipmentOrders()
                .stream()
                .map(order -> new OrderDTO(order.shipmentOrderId(), false, order.externalId(), order.createdAt(), null))
                .collect(Collectors.toSet());
    }
}
