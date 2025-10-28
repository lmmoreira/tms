package br.com.logistics.tms.shipmentorder.application.repositories;

import br.com.logistics.tms.commons.domain.pagination.Page;

import br.com.logistics.tms.commons.domain.pagination.PageRequest;
import br.com.logistics.tms.shipmentorder.domain.ShipmentOrder;

import java.util.Optional;
import java.util.UUID;

public interface ShipmentOrderRepository {

    Optional<ShipmentOrder> getShipmentOrderByExternalId(String externalId);

    Page<ShipmentOrder> getShipmentOrderByCompanyId(UUID companyId, PageRequest pageRequest);

    ShipmentOrder create(ShipmentOrder shipmentOrder);

}
