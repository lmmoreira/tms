package br.com.logistics.tms.commons.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.datasource")
public record DataSourceProperties(DbConfig write, DbConfig read) {

    public record DbConfig(String url, String username, String password) {
    }
}