package br.com.logistics.tms.commons.infrastructure.config;

import br.com.logistics.tms.commons.infrastructure.json.JsonSingleton;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return JsonSingleton.registeredMapper();
    }
}