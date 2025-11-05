package br.com.logistics.tms.shipmentorder.infrastructure.dto;

import java.time.Instant;
import java.util.UUID;

public record CreateShipmentOrderDTO(UUID companyId, UUID shipperId, String externalId, Instant createdAt) {

}