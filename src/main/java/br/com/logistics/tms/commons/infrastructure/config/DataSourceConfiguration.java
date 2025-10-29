package br.com.logistics.tms.commons.infrastructure.config;

import br.com.logistics.tms.commons.application.annotation.DatabaseRole;
import br.com.logistics.tms.commons.infrastructure.config.properties.DataSourceProperties;
import br.com.logistics.tms.commons.infrastructure.database.routing.RoutingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(DataSourceProperties.class)
public class DataSourceConfiguration {

    private final ApplicationContext ctx;
    private final DataSourceProperties dataSourceProperties;

    @Autowired
    public DataSourceConfiguration(DataSourceProperties dataSourceProperties, ApplicationContext ctx) {
        this.dataSourceProperties = dataSourceProperties;
        this.ctx = ctx;
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        final Map<Object, Object> dataSources = new HashMap<>();

        DataSource write = null;
        DataSource read = null;

        if (ctx.containsBean("writeDataSource")) {
            write = ctx.getBean("writeDataSource", DataSource.class);
            dataSources.put(DatabaseRole.WRITE, write);
        }

        if (ctx.containsBean("readOnlyDataSource")) {
            read = ctx.getBean("readOnlyDataSource", DataSource.class);
            dataSources.put(DatabaseRole.READ, read);
        }

        if (write == null && read == null) {
            throw new IllegalStateException("No data source beans created for current app.cqrs.mode");
        }

        final RoutingDataSource routingDataSource = new RoutingDataSource();
        routingDataSource.setTargetDataSources(dataSources);
        routingDataSource.setDefaultTargetDataSource(write != null ? write : read);

        return routingDataSource;
    }

    @Bean
    @ConditionalOnExpression("'${app.cqrs.mode}' == 'write' or '${app.cqrs.mode}' == 'both'")
    public DataSource writeDataSource() {
        final DataSourceProperties.DbConfig cfg = dataSourceProperties.write();
        return DataSourceBuilder.create()
                .url(cfg.url())
                .username(cfg.username())
                .password(cfg.password())
                .build();
    }

    @Bean
    @ConditionalOnExpression("'${app.cqrs.mode}' == 'read' or '${app.cqrs.mode}' == 'both'")
    public DataSource readOnlyDataSource() {
        final DataSourceProperties.DbConfig cfg = dataSourceProperties.read();
        return DataSourceBuilder.create()
                .url(cfg.url())
                .username(cfg.username())
                .password(cfg.password())
                .build();
    }
}