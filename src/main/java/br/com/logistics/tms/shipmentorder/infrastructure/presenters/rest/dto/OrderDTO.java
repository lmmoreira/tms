package br.com.logistics.tms.shipmentorder.infrastructure.presenters.rest.dto;

import java.time.Instant;
import java.util.UUID;

public record OrderDTO(UUID id, boolean archived, String externalId, Instant createdAt, Instant updatedAt) {

}