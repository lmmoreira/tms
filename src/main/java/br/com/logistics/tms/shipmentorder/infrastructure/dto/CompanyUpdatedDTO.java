package br.com.logistics.tms.shipmentorder.infrastructure.dto;

import java.time.Instant;
import java.util.UUID;

public record CompanyUpdatedDTO(
        UUID domainEventId,
        UUID companyId,
        String property,
        String oldValue,
        String newValue,
        Instant occurredOn,
        String aggregateId,
        String type,
        String module) {
}
