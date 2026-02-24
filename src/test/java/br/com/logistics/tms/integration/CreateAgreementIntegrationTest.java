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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Create Agreement Integration Tests")
class CreateAgreementIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Should create agreement and persist via cascade")
    void shouldCreateAgreementViaCascade() throws Exception {
        final CompanyId sourceCompanyId = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withName("Shoppe Marketplace")
                        .withTypes(CompanyType.MARKETPLACE)
                        .build()
        );

        final CompanyId destinationCompanyId = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withName("Loggi Logistics")
                        .withTypes(CompanyType.LOGISTICS_PROVIDER)
                        .build()
        );

        final Instant validFrom = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        final String requestBody = objectMapper.writeValueAsString(Map.of(
                "destinationCompanyId", destinationCompanyId.value().toString(),
                "type", "DELIVERS_WITH",
                "configuration", Map.of("priority", "high"),
                "conditions", List.of(
                        Map.of(
                                "type", "DISCOUNT_PERCENTAGE",
                                "conditions", Map.of("percentage", 10.0)
                        )
                ),
                "validFrom", validFrom.toString(),
                "validTo", (Object) null
        ));

        final String response = mockMvc.perform(post("/companies/" + sourceCompanyId.value() + "/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.value.agreementId").exists())
                .andExpect(jsonPath("$.value.sourceCompanyId").value(sourceCompanyId.value().toString()))
                .andExpect(jsonPath("$.value.destinationCompanyId").value(destinationCompanyId.value().toString()))
                .andExpect(jsonPath("$.value.agreementType").value("DELIVERS_WITH"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        final CompanyEntity sourceEntity = companyJpaRepository.findById(sourceCompanyId.value())
                .orElseThrow();

        assertThat(sourceEntity.getAgreements()).hasSize(1);
        assertThat(sourceEntity.getAgreements().iterator().next().getRelationType())
                .isEqualTo("DELIVERS_WITH");
    }

    @Test
    @DisplayName("Should generate AgreementAdded domain event in outbox")
    void shouldGenerateAgreementAddedEvent() throws Exception {
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

        final String requestBody = objectMapper.writeValueAsString(Map.of(
                "destinationCompanyId", destinationCompanyId.value().toString(),
                "type", "DELIVERS_WITH",
                "configuration", Map.of(),
                "conditions", List.of(),
                "validFrom", validFrom.toString(),
                "validTo", (Object) null
        ));

        final long outboxCountBefore = companyOutboxJpaRepository.count();

        mockMvc.perform(post("/companies/" + sourceCompanyId.value() + "/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        final long outboxCountAfter = companyOutboxJpaRepository.count();

        assertThat(outboxCountAfter).isGreaterThan(outboxCountBefore);
        assertThat(companyOutboxJpaRepository.findAll().stream()
                .anyMatch(outbox -> outbox.getType().equals("AgreementAdded")))
                .isTrue();
    }

    @Test
    @DisplayName("Should reject agreement with self-reference")
    void shouldRejectSelfReferenceAgreement() throws Exception {
        final CompanyId companyId = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withName("Test Company")
                        .withTypes(CompanyType.MARKETPLACE)
                        .build()
        );

        final Instant validFrom = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        final String requestBody = objectMapper.writeValueAsString(Map.of(
                "destinationCompanyId", companyId.value().toString(),
                "type", "DELIVERS_WITH",
                "configuration", Map.of(),
                "conditions", List.of(),
                "validFrom", validFrom.toString(),
                "validTo", (Object) null
        ));

        mockMvc.perform(post("/companies/" + companyId.value() + "/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ValidationException"))
                .andExpect(jsonPath("$.message").value("Agreement source and destination must be different"));
    }

    @Test
    @DisplayName("Should reject duplicate active agreement")
    void shouldRejectDuplicateAgreement() throws Exception {
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

        final String requestBody = objectMapper.writeValueAsString(Map.of(
                "destinationCompanyId", destinationCompanyId.value().toString(),
                "type", "DELIVERS_WITH",
                "configuration", Map.of(),
                "conditions", List.of(),
                "validFrom", validFrom.toString(),
                "validTo", (Object) null
        ));

        mockMvc.perform(post("/companies/" + sourceCompanyId.value() + "/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/companies/" + sourceCompanyId.value() + "/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ValidationException"))
                .andExpect(jsonPath("$.message").value("Agreement already exists for this company"));
    }

    @Test
    @DisplayName("Should reject overlapping active agreement")
    void shouldRejectOverlappingAgreement() throws Exception {
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
                "conditions", List.of(),
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
                "conditions", List.of(),
                "validFrom", baseDate.plus(15, ChronoUnit.DAYS).toString(),
                "validTo", baseDate.plus(45, ChronoUnit.DAYS).toString()
        ));

        mockMvc.perform(post("/companies/" + sourceCompanyId.value() + "/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request2))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ValidationException"))
                .andExpect(jsonPath("$.message").value("Overlapping active agreement exists"));
    }

    @Test
    @DisplayName("Should reject agreement when source company not found")
    void shouldRejectWhenSourceCompanyNotFound() throws Exception {
        final CompanyId destinationCompanyId = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withName("Destination Company")
                        .withTypes(CompanyType.LOGISTICS_PROVIDER)
                        .build()
        );

        final Instant validFrom = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        final String requestBody = objectMapper.writeValueAsString(Map.of(
                "destinationCompanyId", destinationCompanyId.value().toString(),
                "type", "DELIVERS_WITH",
                "configuration", Map.of(),
                "conditions", List.of(),
                "validFrom", validFrom.toString(),
                "validTo", (Object) null
        ));

        final String nonExistentId = CompanyId.unique().value().toString();

        mockMvc.perform(post("/companies/" + nonExistentId + "/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NotFoundException"))
                .andExpect(jsonPath("$.message").value("Source company not found"));
    }

    @Test
    @DisplayName("Should reject agreement when destination company not found")
    void shouldRejectWhenDestinationCompanyNotFound() throws Exception {
        final CompanyId sourceCompanyId = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withName("Source Company")
                        .withTypes(CompanyType.MARKETPLACE)
                        .build()
        );

        final String nonExistentId = CompanyId.unique().value().toString();
        final Instant validFrom = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        final String requestBody = objectMapper.writeValueAsString(Map.of(
                "destinationCompanyId", nonExistentId,
                "type", "DELIVERS_WITH",
                "configuration", Map.of(),
                "conditions", List.of(),
                "validFrom", validFrom.toString(),
                "validTo", (Object) null
        ));

        mockMvc.perform(post("/companies/" + sourceCompanyId.value() + "/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NotFoundException"))
                .andExpect(jsonPath("$.message").value("Destination company not found"));
    }

    @Test
    @DisplayName("Should create agreement with multiple conditions")
    void shouldCreateAgreementWithMultipleConditions() throws Exception {
        final CompanyId sourceCompanyId = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withName("Biquelo Electronics")
                        .withTypes(CompanyType.SELLER)
                        .build()
        );

        final CompanyId destinationCompanyId = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withName("Loggi Logistics")
                        .withTypes(CompanyType.LOGISTICS_PROVIDER)
                        .build()
        );

        final Instant validFrom = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        final String requestBody = objectMapper.writeValueAsString(Map.of(
                "destinationCompanyId", destinationCompanyId.value().toString(),
                "type", "DELIVERS_WITH",
                "configuration", Map.of("priority", "express"),
                "conditions", List.of(
                        Map.of(
                                "type", "DISCOUNT_PERCENTAGE",
                                "conditions", Map.of("percentage", 30.0)
                        ),
                        Map.of(
                                "type", "DELIVERY_SLA_DAYS",
                                "conditions", Map.of("maxDays", 1)
                        )
                ),
                "validFrom", validFrom.toString(),
                "validTo", (Object) null
        ));

        mockMvc.perform(post("/companies/" + sourceCompanyId.value() + "/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.value.agreementId").exists());

        final CompanyEntity sourceEntity = companyJpaRepository.findById(sourceCompanyId.value())
                .orElseThrow();

        assertThat(sourceEntity.getAgreements()).hasSize(1);
        assertThat(sourceEntity.getAgreements().iterator().next().getConditions()).hasSize(2);
    }
}
