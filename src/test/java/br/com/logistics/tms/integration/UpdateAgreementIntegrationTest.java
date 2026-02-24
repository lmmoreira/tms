package br.com.logistics.tms.integration;

import br.com.logistics.tms.AbstractIntegrationTest;
import br.com.logistics.tms.builders.dto.CreateCompanyDTOBuilder;
import br.com.logistics.tms.company.domain.CompanyId;
import br.com.logistics.tms.company.domain.CompanyType;
import br.com.logistics.tms.company.infrastructure.jpa.entities.CompanyEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Update Agreement Integration Tests")
class UpdateAgreementIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Should update agreement validTo date")
    void shouldUpdateValidTo() throws Exception {
        final CompanyId sourceCompanyId = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withName("Source Company")
                        .withTypes(CompanyType.MARKETPLACE)
                        .build()
        );

        final CompanyId destinationCompanyId = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withName("Destination Company")
                        .withTypes(CompanyType.LOGISTICS_PROVIDER)
                        .build()
        );

        final Instant validFrom = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        final String createRequest = objectMapper.writeValueAsString(Map.of(
                "destinationCompanyId", destinationCompanyId.value().toString(),
                "type", "DELIVERS_WITH",
                "configuration", Map.of(),
                "conditions", List.of(
                        Map.of(
                                "type", "DISCOUNT_PERCENTAGE",
                                "conditions", Map.of("percentage", 10.0)
                        )
                ),
                "validFrom", validFrom.toString(),
                "validTo", (Object) null
        ));

        final String createResponse = mockMvc.perform(post("/companies/" + sourceCompanyId.value() + "/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final Map<String, Object> responseMap = objectMapper.readValue(createResponse, Map.class);
        final Map<String, Object> valueMap = (Map<String, Object>) responseMap.get("value");
        final String agreementId = (String) valueMap.get("agreementId");

        final Instant newValidTo = validFrom.plus(365, ChronoUnit.DAYS);

        final String updateRequest = objectMapper.writeValueAsString(Map.of(
                "validTo", newValidTo.toString(),
                "conditions", List.of(
                        Map.of(
                                "type", "DISCOUNT_PERCENTAGE",
                                "conditions", Map.of("percentage", 10.0)
                        )
                )
        ));

        mockMvc.perform(put("/agreements/" + agreementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value.agreementId").value(agreementId))
                .andExpect(jsonPath("$.value.message").value("Agreement updated successfully"));

        final CompanyEntity sourceEntity = companyJpaRepository.findById(sourceCompanyId.value())
                .orElseThrow();

        assertThat(sourceEntity.getAgreements()).hasSize(1);
        assertThat(sourceEntity.getAgreements().iterator().next().getValidTo())
                .isCloseTo(newValidTo, within(1, ChronoUnit.SECONDS));
    }

    @Test
    @DisplayName("Should update agreement conditions")
    void shouldUpdateConditions() throws Exception {
        final CompanyId sourceCompanyId = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withName("Source Company")
                        .withTypes(CompanyType.MARKETPLACE)
                        .build()
        );

        final CompanyId destinationCompanyId = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withName("Destination Company")
                        .withTypes(CompanyType.LOGISTICS_PROVIDER)
                        .build()
        );

        final Instant validFrom = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        final String createRequest = objectMapper.writeValueAsString(Map.of(
                "destinationCompanyId", destinationCompanyId.value().toString(),
                "type", "DELIVERS_WITH",
                "configuration", Map.of(),
                "conditions", List.of(
                        Map.of(
                                "type", "DISCOUNT_PERCENTAGE",
                                "conditions", Map.of("percentage", 10.0)
                        )
                ),
                "validFrom", validFrom.toString(),
                "validTo", (Object) null
        ));

        final String createResponse = mockMvc.perform(post("/companies/" + sourceCompanyId.value() + "/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final Map<String, Object> responseMap = objectMapper.readValue(createResponse, Map.class);
        final Map<String, Object> valueMap = (Map<String, Object>) responseMap.get("value");
        final String agreementId = (String) valueMap.get("agreementId");

        final String updateRequest = objectMapper.writeValueAsString(Map.of(
                "validTo", (Object) null,
                "conditions", List.of(
                        Map.of(
                                "type", "DISCOUNT_PERCENTAGE",
                                "conditions", Map.of("percentage", 15.0)
                        ),
                        Map.of(
                                "type", "DELIVERY_SLA_DAYS",
                                "conditions", Map.of("maxDays", 2)
                        )
                )
        ));

        mockMvc.perform(put("/agreements/" + agreementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk());

        final CompanyEntity sourceEntity = companyJpaRepository.findById(sourceCompanyId.value())
                .orElseThrow();

        assertThat(sourceEntity.getAgreements()).hasSize(1);
        assertThat(sourceEntity.getAgreements().iterator().next().getConditions()).hasSize(2);
    }

    @Test
    @DisplayName("Should generate AgreementUpdated domain event in outbox")
    void shouldGenerateAgreementUpdatedEvent() throws Exception {
        final CompanyId sourceCompanyId = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withName("Test Company")
                        .withTypes(CompanyType.SELLER)
                        .build()
        );

        final CompanyId destinationCompanyId = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withName("Provider Company")
                        .withTypes(CompanyType.LOGISTICS_PROVIDER)
                        .build()
        );

        final Instant validFrom = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        final String createRequest = objectMapper.writeValueAsString(Map.of(
                "destinationCompanyId", destinationCompanyId.value().toString(),
                "type", "DELIVERS_WITH",
                "configuration", Map.of(),
                "conditions", List.of(
                        Map.of(
                                "type", "DISCOUNT_PERCENTAGE",
                                "conditions", Map.of("percentage", 10.0)
                        )
                ),
                "validFrom", validFrom.toString(),
                "validTo", (Object) null
        ));

        final String createResponse = mockMvc.perform(post("/companies/" + sourceCompanyId.value() + "/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final Map<String, Object> responseMap = objectMapper.readValue(createResponse, Map.class);
        final Map<String, Object> valueMap = (Map<String, Object>) responseMap.get("value");
        final String agreementId = (String) valueMap.get("agreementId");

        final long outboxCountBefore = companyOutboxJpaRepository.count();

        final String updateRequest = objectMapper.writeValueAsString(Map.of(
                "validTo", validFrom.plus(180, ChronoUnit.DAYS).toString(),
                "conditions", List.of(
                        Map.of(
                                "type", "DISCOUNT_PERCENTAGE",
                                "conditions", Map.of("percentage", 10.0)
                        )
                )
        ));

        mockMvc.perform(put("/agreements/" + agreementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk());

        final long outboxCountAfter = companyOutboxJpaRepository.count();

        assertThat(outboxCountAfter).isGreaterThan(outboxCountBefore);
        assertThat(companyOutboxJpaRepository.findAll().stream()
                .anyMatch(outbox -> outbox.getType().equals("AgreementUpdated")))
                .isTrue();
    }

    @Test
    @DisplayName("Should reject update when agreement not found")
    void shouldRejectUpdateWhenNotFound() throws Exception {
        final String nonExistentId = CompanyId.unique().value().toString();

        final String updateRequest = objectMapper.writeValueAsString(Map.of(
                "validTo", Instant.now().plus(365, ChronoUnit.DAYS).toString(),
                "conditions", List.of(
                        Map.of(
                                "type", "DISCOUNT_PERCENTAGE",
                                "conditions", Map.of("percentage", 15.0)
                        )
                )
        ));

        mockMvc.perform(put("/agreements/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NotFoundException"))
                .andExpect(jsonPath("$.message").value("Agreement not found"));
    }

    @Test
    @DisplayName("Should reject update with empty conditions")
    void shouldRejectUpdateWithEmptyConditions() throws Exception {
        final CompanyId sourceCompanyId = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withName("Source Company")
                        .withTypes(CompanyType.MARKETPLACE)
                        .build()
        );

        final CompanyId destinationCompanyId = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withName("Destination Company")
                        .withTypes(CompanyType.LOGISTICS_PROVIDER)
                        .build()
        );

        final Instant validFrom = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        final String createRequest = objectMapper.writeValueAsString(Map.of(
                "destinationCompanyId", destinationCompanyId.value().toString(),
                "type", "DELIVERS_WITH",
                "configuration", Map.of(),
                "conditions", List.of(
                        Map.of(
                                "type", "DISCOUNT_PERCENTAGE",
                                "conditions", Map.of("percentage", 10.0)
                        )
                ),
                "validFrom", validFrom.toString(),
                "validTo", (Object) null
        ));

        final String createResponse = mockMvc.perform(post("/companies/" + sourceCompanyId.value() + "/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final Map<String, Object> responseMap = objectMapper.readValue(createResponse, Map.class);
        final Map<String, Object> valueMap = (Map<String, Object>) responseMap.get("value");
        final String agreementId = (String) valueMap.get("agreementId");

        final String updateRequest = objectMapper.writeValueAsString(Map.of(
                "validTo", (Object) null,
                "conditions", List.of()
        ));

        mockMvc.perform(put("/agreements/" + agreementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ValidationException"))
                .andExpect(jsonPath("$.message").value("Agreement must have at least one condition"));
    }

    @Test
    @DisplayName("Should reject update that would create overlap with other agreement")
    void shouldRejectUpdateCreatingOverlap() throws Exception {
        final CompanyId sourceCompanyId = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withName("Source Company")
                        .withTypes(CompanyType.MARKETPLACE)
                        .build()
        );

        final CompanyId destinationCompanyId = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withName("Destination Company")
                        .withTypes(CompanyType.LOGISTICS_PROVIDER)
                        .build()
        );

        final Instant baseDate = Instant.parse("2026-01-01T00:00:00Z");

        final String request1 = objectMapper.writeValueAsString(Map.of(
                "destinationCompanyId", destinationCompanyId.value().toString(),
                "type", "DELIVERS_WITH",
                "configuration", Map.of(),
                "conditions", List.of(
                        Map.of(
                                "type", "DISCOUNT_PERCENTAGE",
                                "conditions", Map.of("percentage", 10.0)
                        )
                ),
                "validFrom", baseDate.toString(),
                "validTo", baseDate.plus(30, ChronoUnit.DAYS).toString()
        ));

        mockMvc.perform(post("/companies/" + sourceCompanyId.value() + "/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request1))
                .andExpect(status().isCreated());

        final String request2 = objectMapper.writeValueAsString(Map.of(
                "destinationCompanyId", destinationCompanyId.value().toString(),
                "type", "DELIVERS_WITH",
                "configuration", Map.of(),
                "conditions", List.of(
                        Map.of(
                                "type", "DISCOUNT_PERCENTAGE",
                                "conditions", Map.of("percentage", 15.0)
                        )
                ),
                "validFrom", baseDate.plus(60, ChronoUnit.DAYS).toString(),
                "validTo", baseDate.plus(90, ChronoUnit.DAYS).toString()
        ));

        final String createResponse = mockMvc.perform(post("/companies/" + sourceCompanyId.value() + "/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request2))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final Map<String, Object> responseMap = objectMapper.readValue(createResponse, Map.class);
        final Map<String, Object> valueMap = (Map<String, Object>) responseMap.get("value");
        final String agreement2Id = (String) valueMap.get("agreementId");

        final String updateRequest = objectMapper.writeValueAsString(Map.of(
                "validTo", baseDate.plus(120, ChronoUnit.DAYS).toString(),
                "conditions", List.of(
                        Map.of(
                                "type", "DISCOUNT_PERCENTAGE",
                                "conditions", Map.of("percentage", 15.0)
                        )
                )
        ));

        mockMvc.perform(put("/agreements/" + agreement2Id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ValidationException"))
                .andExpect(jsonPath("$.message").value("Update would create overlapping agreement"));
    }
}
