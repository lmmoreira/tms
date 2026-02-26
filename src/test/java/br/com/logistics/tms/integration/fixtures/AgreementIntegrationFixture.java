package br.com.logistics.tms.integration.fixtures;

import br.com.logistics.tms.company.domain.AgreementId;
import br.com.logistics.tms.company.infrastructure.dto.CreateAgreementDTO;
import br.com.logistics.tms.company.infrastructure.dto.UpdateAgreementDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AgreementIntegrationFixture {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    public AgreementIntegrationFixture(final MockMvc mockMvc,
                                       final ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    public AgreementId createAgreement(final UUID sourceCompanyId,
                                       final CreateAgreementDTO dto) throws Exception {
        final String json = objectMapper.writeValueAsString(dto);

        final String response = mockMvc.perform(post("/companies/" + sourceCompanyId + "/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.agreementId").exists())
                .andExpect(jsonPath("$.sourceCompanyId").value(sourceCompanyId.toString()))
                .andExpect(jsonPath("$.destinationCompanyId").value(dto.destinationCompanyId().toString()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        final Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        return AgreementId.with((String) responseMap.get("agreementId"));
    }

    public void updateAgreement(final UUID sourceCompanyId,
                                final UUID agreementId,
                                final UpdateAgreementDTO dto) throws Exception {
        final String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put("/companies/" + sourceCompanyId + "/agreements/" + agreementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agreementId").value(agreementId.toString()));
    }

    public void removeAgreement(final UUID sourceCompanyId,
                                final UUID agreementId) throws Exception {
        mockMvc.perform(delete("/companies/" + sourceCompanyId + "/agreements/" + agreementId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    public Map<String, Object> getAgreement(final UUID sourceCompanyId,
                                            final UUID agreementId) throws Exception {
        final String response = mockMvc.perform(get("/companies/" + sourceCompanyId + "/agreements/" + agreementId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agreementId").value(agreementId.toString()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, Map.class);
    }
}
