package br.com.logistics.tms.commons.infrastructure.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class JsonSingleton {

    private static JsonAdapter INSTANCE;
    private static ObjectMapper mapper;

    private JsonSingleton() {
    }

    public static synchronized JsonAdapter getInstance() {
        if (INSTANCE == null) {
            mapper = new ObjectMapper()
                    .registerModule(new BlackbirdModule())
                    .registerModule(new JavaTimeModule());
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            mapper.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
            mapper.setSerializationInclusion(NON_NULL);
            INSTANCE = new JsonAdapterImpl(mapper);
        }

        return INSTANCE;
    }

    public static ObjectMapper registeredMapper() {
        if (INSTANCE == null) {
            getInstance();
        }

        return mapper;
    }

}
