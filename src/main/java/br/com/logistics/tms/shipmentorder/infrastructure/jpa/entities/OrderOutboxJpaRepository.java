package br.com.logistics.tms.shipmentorder.infrastructure.jpa.entities;

import br.com.logistics.tms.commons.infrastructure.jpa.repositories.CustomJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;


@Repository
public interface OrderOutboxJpaRepository extends CustomJpaRepository<OrderOutboxEntity, UUID> {

}
