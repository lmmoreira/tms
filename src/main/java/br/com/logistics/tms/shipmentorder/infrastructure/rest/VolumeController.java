package br.com.logistics.tms.shipmentorder.infrastructure.rest;

import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
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
