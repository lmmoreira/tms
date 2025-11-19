package br.com.logistics.tms;

import br.com.logistics.tms.company.infrastructure.jpa.repositories.CompanyJpaRepository;
import br.com.logistics.tms.company.infrastructure.jpa.repositories.CompanyOutboxJpaRepository;
import br.com.logistics.tms.integration.fixtures.CompanyIntegrationFixture;
import br.com.logistics.tms.integration.fixtures.ShipmentOrderIntegrationFixture;
import br.com.logistics.tms.shipmentorder.infrastructure.jpa.repositories.ShipmentOrderCompanyJpaRepository;
import br.com.logistics.tms.shipmentorder.infrastructure.jpa.repositories.ShipmentOrderJpaRepository;
import br.com.logistics.tms.shipmentorder.infrastructure.jpa.repositories.ShipmentOrderOutboxJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;

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