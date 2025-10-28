package br.com.logistics.tms.shipmentorder.infrastructure.jpa.repositories;

import br.com.logistics.tms.commons.infrastructure.jpa.repositories.CustomJpaRepository;
import br.com.logistics.tms.shipmentorder.infrastructure.jpa.entities.ShipmentOrderEntity;
import br.com.logistics.tms.shipmentorder.infrastructure.jpa.entities.ShipmentOrderOutboxEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface ShipmentOrderJpaRepository extends CustomJpaRepository<ShipmentOrderEntity, UUID> {

    Optional<ShipmentOrderEntity> findByExternalId(String externalId);

    Page<ShipmentOrderEntity> findByCompanyId(UUID companyId, Pageable pageable);


}
