package br.com.logistics.tms.integration.fixtures;

import br.com.logistics.tms.commons.infrastructure.gateways.outbox.AbstractOutboxEntity;
import br.com.logistics.tms.commons.infrastructure.gateways.outbox.OutboxStatus;
import br.com.logistics.tms.company.domain.CompanyId;
import br.com.logistics.tms.company.infrastructure.dto.CreateCompanyDTO;
import br.com.logistics.tms.company.infrastructure.dto.UpdateCompanyDTO;
import br.com.logistics.tms.company.infrastructure.jpa.repositories.CompanyOutboxJpaRepository;
import br.com.logistics.tms.shipmentorder.infrastructure.jpa.entities.ShipmentOrderCompanyEntity;
import br.com.logistics.tms.shipmentorder.infrastructure.jpa.repositories.ShipmentOrderCompanyJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CompanyIntegrationFixture {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final CompanyOutboxJpaRepository companyOutboxJpaRepository;
    private final ShipmentOrderCompanyJpaRepository shipmentOrderCompanyJpaRepository;

    public CompanyIntegrationFixture(final MockMvc mockMvc,
                                     final ObjectMapper objectMapper,
                                     final CompanyOutboxJpaRepository companyOutboxJpaRepository,
                                     final ShipmentOrderCompanyJpaRepository shipmentOrderCompanyJpaRepository) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.companyOutboxJpaRepository = companyOutboxJpaRepository;
        this.shipmentOrderCompanyJpaRepository = shipmentOrderCompanyJpaRepository;
    }

    public CompanyId createCompany(final CreateCompanyDTO dto) throws Exception {
        final CompanyId companyId = createCompanyWithoutWaiting(dto);
        waitForOutboxPublished(companyId);
        waitForSynchronizationToShipmentOrderSchema(companyId);
        return companyId;
    }

    public CompanyId createCompanyWithoutWaiting(final CreateCompanyDTO dto) throws Exception {
        final String json = objectMapper.writeValueAsString(dto);

        final String response = mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.companyId").exists())
                .andExpect(jsonPath("$.name").value(dto.name()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        final Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        return CompanyId.with((String) responseMap.get("companyId"));
    }

    public void updateCompany(final CompanyId companyId, final UpdateCompanyDTO dto) throws Exception {
        updateCompanyWithoutWaiting(companyId, dto);
        waitForOutboxPublished(companyId);
        waitForSynchronizationToShipmentOrderSchema(companyId);
    }

    public void deleteCompany(final CompanyId companyId) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/companies/" + companyId.value())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        waitForOutboxPublished(companyId);
        waitForSynchronizationToShipmentOrderSchema(companyId, company -> assertThat(company.getStatus()).isEqualTo('D'));
    }

    public void updateCompanyWithoutWaiting(final CompanyId companyId, final UpdateCompanyDTO dto) throws Exception {
        final String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put("/companies/" + companyId.value())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(dto.name()));
    }

    private void waitForOutboxPublished(final CompanyId companyId) {
        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofMillis(200))
                .untilAsserted(() -> assertThat(companyOutboxJpaRepository.findFirstByAggregateIdOrderByCreatedAtDesc(companyId.value()))
                        .isPresent()
                        .get()
                        .extracting(AbstractOutboxEntity::getStatus)
                        .isEqualTo(OutboxStatus.PUBLISHED));
    }

    private void waitForSynchronizationToShipmentOrderSchema(final CompanyId companyId) {
        waitForSynchronizationToShipmentOrderSchema(companyId, entity -> {});
    }

    private void waitForSynchronizationToShipmentOrderSchema(final CompanyId companyId, final Consumer<ShipmentOrderCompanyEntity> assertion) {
        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofMillis(200))
                .untilAsserted(() -> assertThat(shipmentOrderCompanyJpaRepository.findById(companyId.value()))
                        .isPresent()
                        .get()
                        .satisfies(assertion));
    }
}
