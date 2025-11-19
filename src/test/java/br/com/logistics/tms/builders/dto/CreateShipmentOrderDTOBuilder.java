package br.com.logistics.tms.builders.dto;

import br.com.logistics.tms.shipmentorder.infrastructure.dto.CreateShipmentOrderDTO;

import java.time.Instant;
import java.util.UUID;

public class CreateShipmentOrderDTOBuilder {

    private UUID companyId = UUID.randomUUID();
    private UUID shipperId = UUID.randomUUID();
    private String externalId = "EXT-ORDER-DEFAULT" + UUID.randomUUID().toString();
    private Instant createdAt = Instant.now();

    public static CreateShipmentOrderDTOBuilder aCreateShipmentOrderDTO() {
        return new CreateShipmentOrderDTOBuilder();
    }

    public CreateShipmentOrderDTOBuilder withCompanyId(final UUID companyId) {
        this.companyId = companyId;
        return this;
    }

    public CreateShipmentOrderDTOBuilder withShipperId(final UUID shipperId) {
        this.shipperId = shipperId;
        return this;
    }

    public CreateShipmentOrderDTOBuilder withExternalId(final String externalId) {
        this.externalId = externalId;
        return this;
    }

    public CreateShipmentOrderDTOBuilder withCreatedAt(final Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public CreateShipmentOrderDTO build() {
        return new CreateShipmentOrderDTO(companyId, shipperId, externalId, createdAt);
    }
}
