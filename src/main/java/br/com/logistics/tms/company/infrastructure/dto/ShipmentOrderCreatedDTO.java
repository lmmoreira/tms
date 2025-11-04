package br.com.logistics.tms.company.infrastructure.dto;

import java.time.Instant;
import java.util.UUID;

public record ShipmentOrderCreatedDTO(UUID domainEventId,
        UUID shipmentOrderId,
        UUID companyId,
        String externalId,
        Instant occurredOn,
        String aggregateId,
        String type,
        String module) {
}
