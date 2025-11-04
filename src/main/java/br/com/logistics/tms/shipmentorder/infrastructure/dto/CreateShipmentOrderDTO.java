package br.com.logistics.tms.shipmentorder.infrastructure.dto;

import java.time.Instant;
import java.util.UUID;

public record CreateShipmentOrderDTO(UUID companyId, String externalId, Instant createdAt) {

}