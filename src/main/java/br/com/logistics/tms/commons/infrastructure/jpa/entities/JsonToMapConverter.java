package br.com.logistics.tms.commons.infrastructure.jpa.entities;

import br.com.logistics.tms.commons.infrastructure.json.JsonAdapter;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Converter
@AllArgsConstructor
@SuppressWarnings("unchecked")
public class JsonToMapConverter implements AttributeConverter<Map<String, Object>, String> {

    private final JsonAdapter jsonAdapter;

    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        return jsonAdapter.toJson(attribute);
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        return jsonAdapter.fromJson(dbData, HashMap.class);
    }
}