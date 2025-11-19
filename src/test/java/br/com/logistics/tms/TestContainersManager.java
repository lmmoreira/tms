package br.com.logistics.tms;

import org.testcontainers.containers.*;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

public final class TestContainersManager {

    private static TestContainersManager INSTANCE;

    private final Network network;
    private final PostgreSQLContainer<?> postgres;
    private final GenericContainer<?> flyway;
    private final RabbitMQContainer rabbit;

    private TestContainersManager() {
        network = Network.newNetwork();

        postgres = new PostgreSQLContainer<>("postgres:latest")
                .withDatabaseName("tms")
                .withUsername("tms")
                .withPassword("tms")
                .withNetwork(network)
                .withNetworkAliases("tms-database");
        postgres.start();

        flyway = new GenericContainer<>("flyway/flyway:latest")
                .withFileSystemBind("infra/database/migration", "/flyway/sql", BindMode.READ_ONLY)
                .withEnv("FLYWAY_URL", "jdbc:postgresql://tms-database:5432/tms")
                .withEnv("FLYWAY_USER", "tms")
                .withEnv("FLYWAY_PASSWORD", "tms")
                .withNetwork(network)
                .withCommand("migrate")
                .dependsOn(postgres);
        flyway.start();

        rabbit = new RabbitMQContainer("rabbitmq:management")
                .withCopyFileToContainer(
                        MountableFile.forHostPath("infra/rabbitmq/definitions.json"),
                        "/etc/rabbitmq/definitions.json"
                )
                .withEnv("RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS",
                        "-rabbitmq_management load_definitions \"/etc/rabbitmq/definitions.json\"")
                .withNetwork(network)
                .waitingFor(Wait.forLogMessage(".*Server startup complete.*", 1));
        rabbit.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            rabbit.stop();
            flyway.stop();
            postgres.stop();
            network.close();
        }));
    }

    public static synchronized TestContainersManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TestContainersManager();
        }
        return INSTANCE;
    }

    public PostgreSQLContainer<?> getPostgres() {
        return postgres;
    }

    public RabbitMQContainer getRabbit() {
        return rabbit;
    }
}
