package br.com.logistics.tms;

import br.com.logistics.tms.commons.infrastructure.config.modules.CommonsModuleConfig;
import br.com.logistics.tms.commons.infrastructure.config.modules.CompanyModuleConfig;
import br.com.logistics.tms.commons.infrastructure.config.modules.ShipmentOrderModuleConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.Modulithic;

@Modulithic
@Configuration
@EnableAutoConfiguration
@Import({
        CommonsModuleConfig.class,
        ShipmentOrderModuleConfig.class,
        CompanyModuleConfig.class
})
public class TmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TmsApplication.class, args);
    }

}
