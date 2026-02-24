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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Remove Agreement Integration Tests")
class RemoveAgreementIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Should remove agreement and cascade delete from database")
    void shouldRemoveAgreement() throws Exception {
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

        mockMvc.perform(delete("/agreements/" + agreementId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value.agreementId").value(agreementId))
                .andExpect(jsonPath("$.value.companyId").value(sourceCompanyId.value().toString()));

        final CompanyEntity sourceEntity = companyJpaRepository.findById(sourceCompanyId.value())
                .orElseThrow();

        assertThat(sourceEntity.getAgreements()).isEmpty();
    }

    @Test
    @DisplayName("Should generate AgreementRemoved domain event in outbox")
    void shouldGenerateAgreementRemovedEvent() throws Exception {
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

        mockMvc.perform(delete("/agreements/" + agreementId))
                .andExpect(status().isOk());

        final long outboxCountAfter = companyOutboxJpaRepository.count();

        assertThat(outboxCountAfter).isGreaterThan(outboxCountBefore);
        assertThat(companyOutboxJpaRepository.findAll().stream()
                .anyMatch(outbox -> outbox.getType().equals("AgreementRemoved")))
                .isTrue();
    }

    @Test
    @DisplayName("Should return 404 when agreement not found")
    void shouldReturn404WhenNotFound() throws Exception {
        final String nonExistentId = CompanyId.unique().value().toString();

        mockMvc.perform(delete("/agreements/" + nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NotFoundException"))
                .andExpect(jsonPath("$.message").value("Agreement not found"));
    }

    @Test
    @DisplayName("Should remove only the specified agreement, leaving others intact")
    void shouldRemoveOnlySpecifiedAgreement() throws Exception {
        final CompanyId sourceCompanyId = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withName("Source Company")
                        .withTypes(CompanyType.MARKETPLACE)
                        .build()
        );

        final CompanyId destination1Id = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withName("Destination Company 1")
                        .withTypes(CompanyType.LOGISTICS_PROVIDER)
                        .build()
        );

        final CompanyId destination2Id = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withName("Destination Company 2")
                        .withTypes(CompanyType.LOGISTICS_PROVIDER)
                        .build()
        );

        final Instant validFrom = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        final String request1 = objectMapper.writeValueAsString(Map.of(
                "destinationCompanyId", destination1Id.value().toString(),
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

        final String createResponse1 = mockMvc.perform(post("/companies/" + sourceCompanyId.value() + "/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request1))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final Map<String, Object> responseMap1 = objectMapper.readValue(createResponse1, Map.class);
        final Map<String, Object> valueMap1 = (Map<String, Object>) responseMap1.get("value");
        final String agreement1Id = (String) valueMap1.get("agreementId");

        final String request2 = objectMapper.writeValueAsString(Map.of(
                "destinationCompanyId", destination2Id.value().toString(),
                "type", "DELIVERS_WITH",
                "configuration", Map.of(),
                "conditions", List.of(
                        Map.of(
                                "type", "DISCOUNT_PERCENTAGE",
                                "conditions", Map.of("percentage", 15.0)
                        )
                ),
                "validFrom", validFrom.toString(),
                "validTo", (Object) null
        ));

        mockMvc.perform(post("/companies/" + sourceCompanyId.value() + "/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request2))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/agreements/" + agreement1Id))
                .andExpect(status().isOk());

        final CompanyEntity sourceEntity = companyJpaRepository.findById(sourceCompanyId.value())
                .orElseThrow();

        assertThat(sourceEntity.getAgreements()).hasSize(1);
        assertThat(sourceEntity.getAgreements().iterator().next().getTo().getId())
                .isEqualTo(destination2Id.value());
    }

    @Test
    @DisplayName("Should verify agreement is removed from company GET endpoint")
    void shouldVerifyAgreementRemovedFromGetEndpoint() throws Exception {
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

        mockMvc.perform(get("/companies/" + sourceCompanyId.value() + "/agreements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value.agreements").isArray())
                .andExpect(jsonPath("$.value.agreements.length()").value(1));

        mockMvc.perform(delete("/agreements/" + agreementId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/companies/" + sourceCompanyId.value() + "/agreements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value.agreements").isArray())
                .andExpect(jsonPath("$.value.agreements").isEmpty());
    }

    @Test
    @DisplayName("Should allow creating new agreement after removing old one")
    void shouldAllowCreatingNewAgreementAfterRemoval() throws Exception {
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

        mockMvc.perform(delete("/agreements/" + agreementId))
                .andExpect(status().isOk());

        final String newCreateRequest = objectMapper.writeValueAsString(Map.of(
                "destinationCompanyId", destinationCompanyId.value().toString(),
                "type", "DELIVERS_WITH",
                "configuration", Map.of(),
                "conditions", List.of(
                        Map.of(
                                "type", "DISCOUNT_PERCENTAGE",
                                "conditions", Map.of("percentage", 20.0)
                        )
                ),
                "validFrom", validFrom.plus(1, ChronoUnit.DAYS).toString(),
                "validTo", (Object) null
        ));

        mockMvc.perform(post("/companies/" + sourceCompanyId.value() + "/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newCreateRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.value.agreementId").exists());

        final CompanyEntity sourceEntity = companyJpaRepository.findById(sourceCompanyId.value())
                .orElseThrow();

        assertThat(sourceEntity.getAgreements()).hasSize(1);
    }
}
