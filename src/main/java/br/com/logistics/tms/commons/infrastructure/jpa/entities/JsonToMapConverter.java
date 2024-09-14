package br.com.logistics.tms.commons.infrastructure.jpa.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Converter
@SuppressWarnings("unchecked")
public class JsonToMapConverter implements AttributeConverter<Map<String, Object>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new JsonFormatException("Error converting map to JSON string", e);
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, HashMap.class);
        } catch (IOException e) {
            throw new JsonFormatException("Error converting JSON string to map", e);
        }
    }
}