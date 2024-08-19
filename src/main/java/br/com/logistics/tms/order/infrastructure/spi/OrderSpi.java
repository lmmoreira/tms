package br.com.logistics.tms.order.infrastructure.spi;

import br.com.logistics.tms.order.infrastructure.spi.dto.OrderDTO;
import java.util.Set;

public interface OrderSpi {

    Set<OrderDTO> getOrderByCompanyId(Long companyId);

}
