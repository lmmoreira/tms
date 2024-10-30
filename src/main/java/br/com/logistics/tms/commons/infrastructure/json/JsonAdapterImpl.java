package br.com.logistics.tms.commons.infrastructure.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.stereotype.Component;

@Component
public class JsonAdapterImpl implements JsonAdapter {

    private final ObjectMapper objectMapper;

    public JsonAdapterImpl() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JsonFormatException("Error converting object to JSON string", e);
        }
    }

    @Override
    public <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new JsonFormatException("Error converting JSON string to object", e);
        }
    }

}
