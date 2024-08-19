package br.com.logistics.tms.order.infrastructure.rest.dto;

import java.time.Instant;

public record OrderDTO(Long id, String externalId, Instant date) {

}