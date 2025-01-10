package br.com.logistics.tms.order.infrastructure.rest;

import br.com.logistics.tms.order.application.GetOrderByCompanyIdUseCase;
import br.com.logistics.tms.order.application.GetOrderByCompanyIdUseCase.Input;
import br.com.logistics.tms.order.infrastructure.rest.dto.OrderDTO;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "volumes")
@Slf4j
public class VolumeController {


    @GetMapping("/order/{id}")
    public Set<String> getByOrder(@PathVariable Long id, @RequestHeader Map<String, String> headers) {
        log.info("Oiaaaa");
        return Set.of("1", "2", "3");
    }

}
