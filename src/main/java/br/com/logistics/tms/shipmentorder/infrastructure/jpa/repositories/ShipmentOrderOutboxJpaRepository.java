package br.com.logistics.tms.shipmentorder.infrastructure.jpa.repositories;

import br.com.logistics.tms.commons.infrastructure.jpa.repositories.CustomJpaRepository;
import br.com.logistics.tms.shipmentorder.infrastructure.jpa.entities.ShipmentOrderOutboxEntity;
import org.springframework.stereotype.Repository;

import java.util.UUID;


@Repository
public interface ShipmentOrderOutboxJpaRepository extends CustomJpaRepository<ShipmentOrderOutboxEntity, UUID> {

}
