package br.com.logistics.tms.commons.infrastructure.jpa.entities;

import br.com.logistics.tms.commons.infrastructure.json.JsonAdapter;
import br.com.logistics.tms.commons.infrastructure.json.JsonAdapterImpl;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.HashMap;
import java.util.Map;

@Converter
@SuppressWarnings("unchecked")
public class JsonToMapConverter implements AttributeConverter<Map<String, Object>, String> {

    private final JsonAdapter jsonAdapter = new JsonAdapterImpl();

    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        return jsonAdapter.toJson(attribute);
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        return jsonAdapter.fromJson(dbData, HashMap.class);
    }
}