package br.com.logistics.tms.integration;

import br.com.logistics.tms.AbstractIntegrationTest;
import br.com.logistics.tms.builders.dto.CreateCompanyDTOBuilder;
import br.com.logistics.tms.company.domain.CompanyId;
import br.com.logistics.tms.company.domain.CompanyType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Get Agreements Integration Tests")
class GetAgreementsIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Should get all agreements for a company")
    void shouldGetAgreementsByCompany() throws Exception {
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
                "conditions", List.of(),
                "validFrom", validFrom.toString(),
                "validTo", (Object) null
        ));

        mockMvc.perform(post("/companies/" + sourceCompanyId.value() + "/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request1))
                .andExpect(status().isCreated());

        final String request2 = objectMapper.writeValueAsString(Map.of(
                "destinationCompanyId", destination2Id.value().toString(),
                "type", "DELIVERS_WITH",
                "configuration", Map.of(),
                "conditions", List.of(),
                "validFrom", validFrom.toString(),
                "validTo", (Object) null
        ));

        mockMvc.perform(post("/companies/" + sourceCompanyId.value() + "/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request2))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/companies/" + sourceCompanyId.value() + "/agreements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value.companyId").value(sourceCompanyId.value().toString()))
                .andExpect(jsonPath("$.value.agreements").isArray())
                .andExpect(jsonPath("$.value.agreements.length()").value(2))
                .andExpect(jsonPath("$.value.agreements[0].agreementId").exists())
                .andExpect(jsonPath("$.value.agreements[0].from").value(sourceCompanyId.value().toString()))
                .andExpect(jsonPath("$.value.agreements[0].type").value("DELIVERS_WITH"))
                .andExpect(jsonPath("$.value.agreements[0].isActive").value(true));
    }

    @Test
    @DisplayName("Should return empty list when company has no agreements")
    void shouldReturnEmptyListWhenNoAgreements() throws Exception {
        final CompanyId companyId = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withName("Company Without Agreements")
                        .withTypes(CompanyType.MARKETPLACE)
                        .build()
        );

        mockMvc.perform(get("/companies/" + companyId.value() + "/agreements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value.companyId").value(companyId.value().toString()))
                .andExpect(jsonPath("$.value.agreements").isArray())
                .andExpect(jsonPath("$.value.agreements").isEmpty());
    }

    @Test
    @DisplayName("Should return 404 when company not found")
    void shouldReturn404WhenCompanyNotFound() throws Exception {
        final String nonExistentId = CompanyId.unique().value().toString();

        mockMvc.perform(get("/companies/" + nonExistentId + "/agreements"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NotFoundException"))
                .andExpect(jsonPath("$.message").value("Company not found"));
    }

    @Test
    @DisplayName("Should get agreement by ID")
    void shouldGetAgreementById() throws Exception {
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
                "configuration", Map.of("priority", "high"),
                "conditions", List.of(
                        Map.of(
                                "type", "DISCOUNT_PERCENTAGE",
                                "conditions", Map.of("percentage", 15.0)
                        )
                ),
                "validFrom", validFrom.toString(),
                "validTo", (Object) null
        ));

        final String createResponse = mockMvc.perform(post("/companies/" + sourceCompanyId.value() + "/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final Map<String, Object> responseMap = objectMapper.readValue(createResponse, Map.class);
        final Map<String, Object> valueMap = (Map<String, Object>) responseMap.get("value");
        final String agreementId = (String) valueMap.get("agreementId");

        mockMvc.perform(get("/agreements/" + agreementId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value.agreementId").value(agreementId))
                .andExpect(jsonPath("$.value.sourceCompanyId").value(sourceCompanyId.value().toString()))
                .andExpect(jsonPath("$.value.destinationCompanyId").value(destinationCompanyId.value().toString()))
                .andExpect(jsonPath("$.value.type").value("DELIVERS_WITH"))
                .andExpect(jsonPath("$.value.conditions").isArray())
                .andExpect(jsonPath("$.value.conditions.length()").value(1))
                .andExpect(jsonPath("$.value.isActive").value(true));
    }

    @Test
    @DisplayName("Should return 404 when agreement not found by ID")
    void shouldReturn404WhenAgreementNotFoundById() throws Exception {
        final String nonExistentId = CompanyId.unique().value().toString();

        mockMvc.perform(get("/agreements/" + nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NotFoundException"))
                .andExpect(jsonPath("$.message").value("Agreement not found"));
    }

    @Test
    @DisplayName("Should include condition details in agreement response")
    void shouldIncludeConditionDetailsInResponse() throws Exception {
        final CompanyId sourceCompanyId = companyFixture.createCompany(
                CreateCompanyDTOBuilder.aCreateCompanyDTO()
                        .withName("Source Company")
                        .withTypes(CompanyType.SELLER)
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
                "conditions", List.of(
                        Map.of(
                                "type", "DISCOUNT_PERCENTAGE",
                                "conditions", Map.of("percentage", 20.0)
                        ),
                        Map.of(
                                "type", "DELIVERY_SLA_DAYS",
                                "conditions", Map.of("maxDays", 2)
                        )
                ),
                "validFrom", validFrom.toString(),
                "validTo", (Object) null
        ));

        final String createResponse = mockMvc.perform(post("/companies/" + sourceCompanyId.value() + "/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final Map<String, Object> responseMap = objectMapper.readValue(createResponse, Map.class);
        final Map<String, Object> valueMap = (Map<String, Object>) responseMap.get("value");
        final String agreementId = (String) valueMap.get("agreementId");

        mockMvc.perform(get("/agreements/" + agreementId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value.conditions").isArray())
                .andExpect(jsonPath("$.value.conditions.length()").value(2))
                .andExpect(jsonPath("$.value.conditions[?(@.conditionType == 'DISCOUNT_PERCENTAGE')]").exists())
                .andExpect(jsonPath("$.value.conditions[?(@.conditionType == 'DELIVERY_SLA_DAYS')]").exists());
    }

    @Test
    @DisplayName("Should show agreement as active when within validity period")
    void shouldShowAgreementAsActive() throws Exception {
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

        final Instant validFrom = Instant.now().minus(10, ChronoUnit.DAYS);
        final Instant validTo = Instant.now().plus(10, ChronoUnit.DAYS);

        final String requestBody = objectMapper.writeValueAsString(Map.of(
                "destinationCompanyId", destinationCompanyId.value().toString(),
                "type", "DELIVERS_WITH",
                "configuration", Map.of(),
                "conditions", List.of(),
                "validFrom", validFrom.toString(),
                "validTo", validTo.toString()
        ));

        mockMvc.perform(post("/companies/" + sourceCompanyId.value() + "/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/companies/" + sourceCompanyId.value() + "/agreements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value.agreements[0].isActive").value(true));
    }

    @Test
    @DisplayName("Should show agreement as inactive when expired")
    void shouldShowAgreementAsInactive() throws Exception {
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

        final Instant validFrom = Instant.now().minus(30, ChronoUnit.DAYS);
        final Instant validTo = Instant.now().minus(1, ChronoUnit.DAYS);

        final String requestBody = objectMapper.writeValueAsString(Map.of(
                "destinationCompanyId", destinationCompanyId.value().toString(),
                "type", "DELIVERS_WITH",
                "configuration", Map.of(),
                "conditions", List.of(),
                "validFrom", validFrom.toString(),
                "validTo", validTo.toString()
        ));

        mockMvc.perform(post("/companies/" + sourceCompanyId.value() + "/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/companies/" + sourceCompanyId.value() + "/agreements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value.agreements[0].isActive").value(false));
    }
}
