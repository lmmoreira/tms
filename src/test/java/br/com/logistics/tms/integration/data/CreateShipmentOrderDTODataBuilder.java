package br.com.logistics.tms.integration.data;

import br.com.logistics.tms.shipmentorder.infrastructure.dto.CreateShipmentOrderDTO;

import java.time.Instant;
import java.util.UUID;

public class CreateShipmentOrderDTODataBuilder {

    private UUID companyId = UUID.randomUUID();
    private UUID shipperId = UUID.randomUUID();
    private String externalId = "EXT-ORDER-DEFAULT";
    private Instant createdAt = Instant.now();

    public static CreateShipmentOrderDTODataBuilder aCreateShipmentOrderDTO() {
        return new CreateShipmentOrderDTODataBuilder();
    }

    public CreateShipmentOrderDTODataBuilder withCompanyId(final UUID companyId) {
        this.companyId = companyId;
        return this;
    }

    public CreateShipmentOrderDTODataBuilder withShipperId(final UUID shipperId) {
        this.shipperId = shipperId;
        return this;
    }

    public CreateShipmentOrderDTODataBuilder withExternalId(final String externalId) {
        this.externalId = externalId;
        return this;
    }

    public CreateShipmentOrderDTODataBuilder withCreatedAt(final Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public CreateShipmentOrderDTO build() {
        return new CreateShipmentOrderDTO(companyId, shipperId, externalId, createdAt);
    }
}
