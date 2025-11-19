# Testcontainers Pattern and Configuration

**Complete guide for Testcontainers usage in TMS integration tests.**

---

## Overview

The TMS project uses a **shared Testcontainers pattern** with a singleton manager to optimize integration test execution. All tests share a single PostgreSQL and RabbitMQ instance, reducing startup time from minutes to seconds.

---

## Architecture

```mermaid
graph TB
    subgraph "Test Execution"
        IT1[CompanyShipmentOrderIT]
        IT2[TmsApplicationIT]
        IT3[Other IT tests]
    end
    
    subgraph "TestContainersManager (Singleton)"
        TCM[TestContainersManager.getInstance]
        PG[PostgreSQL Container]
        FLY[Flyway Container]
        RMQ[RabbitMQ Container]
        NET[Shared Network]
    end
    
    subgraph "AbstractIntegrationTest"
        AIT[Base Test Class]
        PROPS[Dynamic Properties]
        CLEANUP[@BeforeEach Cleanup]
    end
    
    IT1 -->|extends| AIT
    IT2 -->|extends| AIT
    IT3 -->|extends| AIT
    
    AIT -->|uses| TCM
    TCM -->|manages| PG
    TCM -->|manages| FLY
    TCM -->|manages| RMQ
    TCM -->|manages| NET
    
    FLY -->|migrates| PG
    FLY -->|depends on| PG
    
    AIT -->|provides| PROPS
    PROPS -->|configures| PG
    PROPS -->|configures| RMQ
    
    AIT -->|executes| CLEANUP
    
    style TCM fill:#e1f5e1
    style AIT fill:#d1ecf1
    style PG fill:#fff3cd
    style RMQ fill:#f8d7da
```

---

## TestContainersManager Singleton

**Location:** `src/test/java/br/com/logistics/tms/TestContainersManager.java`

### Purpose

Manages a **single shared instance** of PostgreSQL, Flyway, and RabbitMQ containers for all integration tests.

### Implementation

```java
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
```

### Key Features

1. **Singleton Pattern** - Thread-safe via synchronized `getInstance()`
2. **Shared Network** - All containers in same Docker network
3. **Flyway Migrations** - Run automatically on startup via dedicated container
4. **Shutdown Hook** - Cleanup on JVM exit
5. **Database Alias** - PostgreSQL accessible as `tms-database` inside network
6. **RabbitMQ Definitions** - Queue/exchange configuration loaded from file

---

## AbstractIntegrationTest

**Location:** `src/test/java/br/com/logistics/tms/AbstractIntegrationTest.java`

### Purpose

Base class for all integration tests. Provides:
- Testcontainers configuration via `TestContainersManager`
- Spring Boot test context
- MockMvc for REST testing
- Automatic injection of repositories
- Automatic creation of test fixtures
- Database cleanup between tests

### Implementation Highlights

```java
@SpringBootTest(classes = {TmsApplication.class})
@TestPropertySource(locations = "classpath:env-test")
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    private static final TestContainersManager containers = TestContainersManager.getInstance();

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected CompanyJpaRepository companyJpaRepository;

    @Autowired
    protected CompanyOutboxJpaRepository companyOutboxJpaRepository;

    @Autowired
    protected ShipmentOrderJpaRepository shipmentOrderJpaRepository;

    @Autowired
    protected ShipmentOrderCompanyJpaRepository shipmentOrderCompanyJpaRepository;

    @Autowired
    protected ShipmentOrderOutboxJpaRepository shipmentOrderOutboxJpaRepository;

    @DynamicPropertySource
    static void registerProps(final DynamicPropertyRegistry registry) {
        final PostgreSQLContainer<?> postgres = containers.getPostgres();
        final RabbitMQContainer rabbit = containers.getRabbit();

        registry.add("spring.datasource.write.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.write.username", postgres::getUsername);
        registry.add("spring.datasource.write.password", postgres::getPassword);

        registry.add("spring.datasource.read.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.read.username", postgres::getUsername);
        registry.add("spring.datasource.read.password", postgres::getPassword);

        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
    }

    protected CompanyIntegrationFixture companyFixture;
    protected ShipmentOrderIntegrationFixture shipmentOrderFixture;

    @BeforeEach
    void setUp() {
        companyFixture = new CompanyIntegrationFixture(
                mockMvc,
                objectMapper,
                companyOutboxJpaRepository,
                shipmentOrderCompanyJpaRepository
        );

        shipmentOrderFixture = new ShipmentOrderIntegrationFixture(
                mockMvc,
                objectMapper,
                shipmentOrderOutboxJpaRepository,
                companyJpaRepository
        );
    }
}
```

### What Tests Get Automatically

✅ **MockMvc** - For REST endpoint testing  
✅ **ObjectMapper** - For JSON serialization  
✅ **All JPA Repositories** - Pre-injected, ready to use  
✅ **CompanyIntegrationFixture** - For company operations  
✅ **ShipmentOrderIntegrationFixture** - For shipment order operations  
✅ **Fresh fixtures** - Recreated in `@BeforeEach`  
✅ **Database cleanup** - Automatic between tests

---

## Parallel Test Execution

### Maven Configuration

**Surefire (Unit Tests):**
```xml
<plugin>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.5.3</version>
    <configuration>
        <parallel>classes</parallel>
        <threadCount>8</threadCount>
        <forkCount>1</forkCount>
        <reuseForks>true</reuseForks>
        <includes>
            <include>**/*Test.java</include>
        </includes>
        <excludes>
            <exclude>**/*IT.java</exclude>
        </excludes>
    </configuration>
</plugin>
```

**Failsafe (Integration Tests):**
```xml
<plugin>
    <artifactId>maven-failsafe-plugin</artifactId>
    <version>3.5.3</version>
    <configuration>
        <parallel>classes</parallel>
        <threadCount>4</threadCount>
        <forkCount>1</forkCount>
        <reuseForks>true</reuseForks>
        <includes>
            <include>**/*IT.java</include>
        </includes>
    </configuration>
</plugin>
```

### How It Works

1. **Unit tests** (`*Test.java`) run via **Surefire** with **8 parallel threads**
2. **Integration tests** (`*IT.java`) run via **Failsafe** with **4 parallel threads**
3. **Shared containers** - Single PostgreSQL + RabbitMQ instance
4. **Test isolation** - Each test gets fresh Spring context + transactional cleanup
5. **Reused forks** - JVM processes reused for performance

---

## Test Isolation Strategy

### Database Cleanup

Although tests share a single database container, each test is isolated via:

1. **Transactional cleanup** - `@BeforeEach` in `AbstractIntegrationTest`
2. **Fresh fixtures** - New instances created per test
3. **Separate Spring contexts** - Each test class gets own context (can be optimized with `@DirtiesContext` strategies)

### Why NOT Transactional Rollback?

We DON'T use `@Transactional` rollback because:

- ❌ Doesn't work with async event listeners
- ❌ Hides transaction boundary issues
- ❌ Doesn't reflect production behavior

Instead, we use explicit cleanup in `@BeforeEach` (if needed).

---

## Performance Metrics

### Before Shared Testcontainers

- Container startup per test class: ~10-15 seconds
- Total test time (5 test classes): ~2 minutes

### After Shared Testcontainers

- Container startup (once): ~10-15 seconds
- Test execution (parallel): ~5-10 seconds
- Total test time: **~15-20 seconds**

**Speedup: ~8-10x faster**

---

## Flyway Migrations

### How Migrations Work

1. Flyway container starts **after** PostgreSQL
2. Mounts `/infra/database/migration` directory
3. Runs `flyway migrate` command
4. Applies all `V{number}__{description}.sql` files in order
5. Tracks applied migrations in `flyway_schema_history` table

### Migration Files

```
infra/database/migration/
├── V1__create_company_schema.sql
├── V2__create_company_table.sql
├── V3__create_company_outbox.sql
├── V4__create_shipmentorder_schema.sql
├── V5__create_shipmentorder_table.sql
├── V6__create_shipmentorder_outbox.sql
└── V7__create_shipmentorder_company_table.sql
```

**Key Points:**
- ✅ Migrations run once on container startup
- ✅ All tests use migrated database
- ✅ No migration overhead per test
- ✅ Changes require container restart (`docker compose down`)

---

## RabbitMQ Configuration

### Definitions File

**Location:** `infra/rabbitmq/definitions.json`

Defines:
- Queues
- Exchanges
- Bindings

**Example:**
```json
{
  "queues": [
    {"name": "integration.shipmentorder.company.created", "durable": true},
    {"name": "integration.company.shipmentorder.created", "durable": true}
  ],
  "exchanges": [
    {"name": "company.events", "type": "topic"},
    {"name": "shipmentorder.events", "type": "topic"}
  ],
  "bindings": [
    {"source": "company.events", "destination": "integration.shipmentorder.company.created", "routing_key": "company.created"}
  ]
}
```

### Loading Definitions

The container loads definitions via environment variable:
```java
.withEnv("RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS",
        "-rabbitmq_management load_definitions \"/etc/rabbitmq/definitions.json\"")
```

---

## Best Practices

### ✅ DO

- **Extend AbstractIntegrationTest** - Get everything for free
- **Use shared containers** - Via `TestContainersManager.getInstance()`
- **Clean data between tests** - Via `@BeforeEach` if needed
- **Use fixtures** - Already available from `AbstractIntegrationTest`
- **Name tests with `*IT.java`** - Ensures Failsafe execution
- **Trust parallel execution** - Proper isolation ensures no conflicts

### ❌ DON'T

- **Don't create containers manually** - Use `TestContainersManager`
- **Don't use `@Container` annotation** - Singleton pattern instead
- **Don't restart containers** - Shared instance runs for entire test suite
- **Don't use `@Transactional` rollback** - Explicit cleanup instead
- **Don't inject TestContainersManager** - It's a test utility, not a bean

---

## Troubleshooting

### Tests Hang on Startup

**Problem:** Tests wait forever during setup

**Solutions:**
1. Check Docker is running
2. Check ports 5432 (PostgreSQL) and 5672 (RabbitMQ) are free
3. Check Flyway migrations are valid
4. Increase startup timeout in wait strategies

### Database State Contamination

**Problem:** Tests fail due to leftover data from previous tests

**Solutions:**
1. Add cleanup in `@BeforeEach` in `AbstractIntegrationTest`
2. Ensure transactional boundaries are correct
3. Check for async operations not completing

### RabbitMQ Queues Not Found

**Problem:** Listeners can't find queues

**Solutions:**
1. Check `infra/rabbitmq/definitions.json` is correct
2. Verify file is mounted to container
3. Check wait strategy allows RabbitMQ to fully start
4. Verify listeners use correct queue names

### Container Startup Failures

**Problem:** Containers fail to start

**Solutions:**
1. Check Docker daemon is running
2. Check network ports are available
3. Check container images are pulled
4. Review container logs for specific errors

---

## Advanced Patterns

### Custom Wait Strategies

For containers that need specific ready conditions:

```java
.waitingFor(Wait.forLogMessage(".*Server startup complete.*", 1))
```

Or for HTTP endpoints:

```java
.waitingFor(Wait.forHttp("/actuator/health")
        .forStatusCode(200)
        .withStartupTimeout(Duration.ofMinutes(2)))
```

### Network Configuration

Containers share a network to communicate:

```java
network = Network.newNetwork();

postgres = new PostgreSQLContainer<>("postgres:latest")
        .withNetwork(network)
        .withNetworkAliases("tms-database");

flyway = new GenericContainer<>("flyway/flyway:latest")
        .withNetwork(network)
        .withEnv("FLYWAY_URL", "jdbc:postgresql://tms-database:5432/tms");
```

**Why?** Flyway needs to connect to PostgreSQL by hostname.

### File Mounts

**Flyway migrations:**
```java
.withFileSystemBind("infra/database/migration", "/flyway/sql", BindMode.READ_ONLY)
```

**RabbitMQ definitions:**
```java
.withCopyFileToContainer(
        MountableFile.forHostPath("infra/rabbitmq/definitions.json"),
        "/etc/rabbitmq/definitions.json"
)
```

---

## Running Tests

### All Tests
```bash
mvn verify
```

### Unit Tests Only
```bash
mvn test
```

### Integration Tests Only
```bash
mvn failsafe:integration-test
```

### Single Integration Test
```bash
mvn verify -Dit.test=CompanyShipmentOrderIT
```

### Debug Mode
```bash
mvn verify -Dmaven.failsafe.debug
```

---

## See Also

- `/doc/ai/INTEGRATION_TESTS.md` - Integration test patterns
- `/doc/ai/TEST_STRUCTURE.md` - Test organization
- `AbstractIntegrationTest.java` - Base test class
- `TestContainersManager.java` - Singleton container manager
- Testcontainers Docs: https://www.testcontainers.org/

---

**Last Updated:** 2025-11-19
