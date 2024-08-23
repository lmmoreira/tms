package br.com.logistics.tms.order.infrastructure.spi.dto;

import java.time.Instant;

public record OrderDTO(Long id, boolean archived, String externalId, Instant createdAt, Instant updatedAt) {

}