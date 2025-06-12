package br.com.logistics.tms.company.infrastructure.config;

import org.neo4j.cypherdsl.core.renderer.Dialect;
import org.neo4j.driver.Driver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableNeo4jRepositories(basePackages = "br.com.logistics.tms.company.infrastructure.jpa.neo4j",
        transactionManagerRef = "neo4JTransactionManager")
public class Neo4JConfig {
    @Bean
    @Primary
    public PlatformTransactionManager neo4JTransactionManager(Driver driver) {
        return new Neo4jTransactionManager(driver);
    }

    @Bean
    org.neo4j.cypherdsl.core.renderer.Configuration cypherDslConfiguration() {
        return org.neo4j.cypherdsl.core.renderer.Configuration.newConfig()
                .withDialect(Dialect.NEO4J_5).build();
    }

}

