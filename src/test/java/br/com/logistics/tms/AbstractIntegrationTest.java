package br.com.logistics.tms;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.MountableFile;

@SpringBootTest(classes = {TmsApplication.class})
@TestPropertySource(locations = "classpath:env-test")
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("tms")
            .withUsername("tms")
            .withPassword("tms");

    static final RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:management")
            .withCopyFileToContainer(
                    MountableFile.forHostPath("infra/rabbitmq/definitions.json"),
                    "/etc/rabbitmq/definitions.json"
            )
            .withEnv("RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS",
                    "-rabbitmq_management load_definitions \"/etc/rabbitmq/definitions.json\"");

    static {
        postgres.start();
        rabbit.start();
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.write.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.write.username", postgres::getUsername);
        registry.add("spring.datasource.write.password", postgres::getPassword);

        registry.add("spring.datasource.read.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.read.username", postgres::getUsername);
        registry.add("spring.datasource.read.password", postgres::getPassword);

        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
    }
}