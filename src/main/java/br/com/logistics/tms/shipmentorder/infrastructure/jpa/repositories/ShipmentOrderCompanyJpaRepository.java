package br.com.logistics.tms.shipmentorder.infrastructure.jpa.repositories;

import br.com.logistics.tms.shipmentorder.infrastructure.jpa.entities.ShipmentOrderCompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ShipmentOrderCompanyJpaRepository extends JpaRepository<ShipmentOrderCompanyEntity, UUID> {
}
