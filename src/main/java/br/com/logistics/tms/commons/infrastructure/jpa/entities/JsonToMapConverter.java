package br.com.logistics.tms.commons.infrastructure.jpa.entities;

import br.com.logistics.tms.commons.infrastructure.json.JsonSingleton;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.HashMap;
import java.util.Map;

@Converter
@SuppressWarnings("unchecked")
public class JsonToMapConverter implements AttributeConverter<Map<String, Object>, String> {

    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        return JsonSingleton.getInstance().toJson(attribute);
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        return JsonSingleton.getInstance().fromJson(dbData, HashMap.class);
    }

}