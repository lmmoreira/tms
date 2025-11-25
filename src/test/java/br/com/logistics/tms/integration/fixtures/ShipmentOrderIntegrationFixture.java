package br.com.logistics.tms.integration.fixtures;

import br.com.logistics.tms.commons.infrastructure.gateways.outbox.AbstractOutboxEntity;
import br.com.logistics.tms.company.infrastructure.jpa.repositories.CompanyJpaRepository;
import br.com.logistics.tms.shipmentorder.domain.ShipmentOrderId;
import br.com.logistics.tms.shipmentorder.infrastructure.dto.CreateShipmentOrderDTO;
import br.com.logistics.tms.shipmentorder.infrastructure.jpa.repositories.ShipmentOrderOutboxJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ShipmentOrderIntegrationFixture {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final ShipmentOrderOutboxJpaRepository shipmentOrderOutboxJpaRepository;
    private final CompanyJpaRepository companyJpaRepository;

    public ShipmentOrderIntegrationFixture(final MockMvc mockMvc,
                                           final ObjectMapper objectMapper,
                                           final ShipmentOrderOutboxJpaRepository shipmentOrderOutboxJpaRepository,
                                           final CompanyJpaRepository companyJpaRepository) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.shipmentOrderOutboxJpaRepository = shipmentOrderOutboxJpaRepository;
        this.companyJpaRepository = companyJpaRepository;
    }

    public ShipmentOrderId createShipmentOrder(final CreateShipmentOrderDTO dto) throws Exception {
        final ShipmentOrderId shipmentOrderId = createShipmentOrderWithoutWaiting(dto);
        waitForOutboxPublished(shipmentOrderId);
        waitForCompanyCounterIncremented(dto.companyId());
        return shipmentOrderId;
    }

    public ShipmentOrderId createShipmentOrderWithoutWaiting(final CreateShipmentOrderDTO dto) throws Exception {
        final String json = objectMapper.writeValueAsString(dto);

        final String response = mockMvc.perform(post("/shipmentorders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shipmentOrderId").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        return ShipmentOrderId.with((String) responseMap.get("shipmentOrderId"));
    }

    public void createShipmentOrderWithDeletedCompany(final CreateShipmentOrderDTO dto) throws Exception {
        final String json = objectMapper.writeValueAsString(dto);

        final String response = mockMvc.perform(post("/shipmentorders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().is4xxClientError())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        assertThat(responseMap.get("type")).isEqualTo("about:blank");
        assertThat(responseMap.get("title")).asString().startsWith("Cannot create shipment order for an inactive company:");
        assertThat(responseMap.get("status")).isEqualTo(400);
        assertThat(responseMap.get("detail")).asString().startsWith("Cannot create shipment order for an inactive company:");
        assertThat(responseMap.get("instance")).isEqualTo("/shipmentorders");
    }

    private void waitForOutboxPublished(final ShipmentOrderId shipmentOrderId) {
        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofMillis(200))
                .untilAsserted(() -> assertThat(shipmentOrderOutboxJpaRepository.findFirstByAggregateIdOrderByCreatedAtDesc(shipmentOrderId.value()))
                        .isPresent()
                        .get()
                        .extracting(AbstractOutboxEntity::getStatus)
                        .isEqualTo(br.com.logistics.tms.commons.infrastructure.gateways.outbox.OutboxStatus.PUBLISHED));
    }

    private void waitForCompanyCounterIncremented(final java.util.UUID companyId) {
        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofMillis(200))
                .untilAsserted(() -> assertThat(companyJpaRepository.findById(companyId))
                        .isPresent()
                        .get()
                        .extracting(company -> company.getConfiguration().get("shipmentOrderNumber"))
                        .isNotNull());
    }
}
