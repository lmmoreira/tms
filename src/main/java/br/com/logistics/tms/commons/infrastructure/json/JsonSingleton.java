package br.com.logistics.tms.commons.infrastructure.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class JsonSingleton {

    private static JsonAdapter INSTANCE;

    private JsonSingleton() {
    }

    public static synchronized JsonAdapter getInstance() {
        if (INSTANCE == null) {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.setSerializationInclusion(NON_NULL);
            INSTANCE = new JsonAdapterImpl(mapper);
        }

        return INSTANCE;
    }

}
