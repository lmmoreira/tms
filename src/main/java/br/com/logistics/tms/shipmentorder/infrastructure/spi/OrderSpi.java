package br.com.logistics.tms.shipmentorder.infrastructure.spi;

import br.com.logistics.tms.shipmentorder.infrastructure.spi.dto.OrderDTO;
import java.util.Set;
import java.util.UUID;

public interface OrderSpi {

    Set<OrderDTO> getOrderByCompanyId(UUID companyId);

}
