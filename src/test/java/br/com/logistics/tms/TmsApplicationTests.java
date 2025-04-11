package br.com.logistics.tms;

import br.com.logistics.tms.commons.infrastructure.config.modules.CommonsModuleConfig;
import br.com.logistics.tms.commons.infrastructure.config.modules.CompanyModuleConfig;
import br.com.logistics.tms.commons.infrastructure.config.modules.OrderModuleConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(classes = {TmsApplication.class})
@TestPropertySource(locations = "classpath:env-test")
class TmsApplicationTests {

	@Test
	void contextLoads() {
	}

}
