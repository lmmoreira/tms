package br.com.logistics.tms.commons.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.datasource")
public record DataSourceProperties(DbConfig write, DbConfig read) {

    public static final String WRITE = "WRITE";
    public static final String READ = "READ";

    public record DbConfig(String url, String username, String password) {}
}