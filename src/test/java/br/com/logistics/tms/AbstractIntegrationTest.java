package br.com.logistics.tms;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.*;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

@SpringBootTest(classes = {TmsApplication.class})
@TestPropertySource(locations = "classpath:env-test")
@Testcontainers
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    private static final Network network = Network.newNetwork();

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("tms")
            .withUsername("tms")
            .withPassword("tms")
            .withNetwork(network)
            .withNetworkAliases("tms-database");

    @Container
    static final GenericContainer<?> flyway = new GenericContainer<>("flyway/flyway:latest")
            .withFileSystemBind("infra/database/migration", "/flyway/sql", BindMode.READ_ONLY)
            .withEnv("FLYWAY_URL", "jdbc:postgresql://tms-database:5432/tms") // use network alias + internal port
            .withEnv("FLYWAY_USER", "tms")
            .withEnv("FLYWAY_PASSWORD", "tms")
            .withNetwork(network)
            .withCommand("migrate")
            .dependsOn(postgres);

    @Container
    static final RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:management")
            .withCopyFileToContainer(
                    MountableFile.forHostPath("infra/rabbitmq/definitions.json"),
                    "/etc/rabbitmq/definitions.json"
            )
            .withEnv("RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS",
                    "-rabbitmq_management load_definitions \"/etc/rabbitmq/definitions.json\"")
            .withReuse(true)
            .withNetwork(network);

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.write.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.write.username", postgres::getUsername);
        registry.add("spring.datasource.write.password", postgres::getPassword);

        registry.add("spring.datasource.read.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.read.username", postgres::getUsername);
        registry.add("spring.datasource.read.password", postgres::getPassword);
    }
}