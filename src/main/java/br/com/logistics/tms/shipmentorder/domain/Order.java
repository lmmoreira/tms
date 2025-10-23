package br.com.logistics.tms.shipmentorder.domain;

import java.time.Instant;
import java.util.UUID;

public record Order(UUID id, boolean archived, String externalId, Instant createdAt, Instant updatedAt) {

}
