package br.com.logistics.tms.order.domain;

import java.time.Instant;

public record Order(Long id, boolean archived, String externalId, Instant createdAt, Instant updatedAt) {

}
