package br.com.logistics.tms.shipmentorder.infrastructure.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record CompanyCreatedDTO(
        UUID domainEventId,
        UUID companyId,
        String company,
        Set<String> types,
        Instant occurredOn,
        String aggregateId,
        String type,
        String module) {
}
