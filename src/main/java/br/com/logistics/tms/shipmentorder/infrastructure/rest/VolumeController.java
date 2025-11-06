package br.com.logistics.tms.shipmentorder.infrastructure.rest;

import java.util.Map;
import java.util.Set;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.DatabaseRole;
import br.com.logistics.tms.commons.infrastructure.presenters.rest.DefaultRestPresenter;
import br.com.logistics.tms.commons.infrastructure.usecases.RestUseCaseExecutor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "volumes")
@Slf4j
@Cqrs(DatabaseRole.READ)
@AllArgsConstructor
public class VolumeController {

    private final RestUseCaseExecutor restUseCaseExecutor;
    private final DefaultRestPresenter defaultRestPresenter;

    @GetMapping("/order/{id}")
    public Object getByOrder(@PathVariable Long id, @RequestHeader Map<String, String> headers) {
        log.info("Oiaaaa");
        return Set.of("1", "2", "3");
    }

}
