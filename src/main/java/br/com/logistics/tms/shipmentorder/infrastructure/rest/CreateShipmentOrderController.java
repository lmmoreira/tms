package br.com.logistics.tms.shipmentorder.infrastructure.rest;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.DatabaseRole;
import br.com.logistics.tms.commons.infrastructure.presenters.rest.DefaultRestPresenter;
import br.com.logistics.tms.commons.infrastructure.usecases.RestUseCaseExecutor;
import br.com.logistics.tms.shipmentorder.application.usecases.CreateShipmentOrderUseCase;
import br.com.logistics.tms.shipmentorder.infrastructure.dto.CreateShipmentOrderDTO;
import br.com.logistics.tms.shipmentorder.infrastructure.dto.CreateShipmentOrderResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "shipmentorders")
@Cqrs(DatabaseRole.WRITE)
public class CreateShipmentOrderController {

    private final RestUseCaseExecutor restUseCaseExecutor;
    private final DefaultRestPresenter defaultRestPresenter;
    private final CreateShipmentOrderUseCase createShipmentOrderUseCase;

    @Autowired
    public CreateShipmentOrderController(RestUseCaseExecutor restUseCaseExecutor,
                                         DefaultRestPresenter defaultRestPresenter,
                                         CreateShipmentOrderUseCase createShipmentOrderUseCase) {
        this.createShipmentOrderUseCase = createShipmentOrderUseCase;
        this.defaultRestPresenter = defaultRestPresenter;
        this.restUseCaseExecutor = restUseCaseExecutor;
    }

    @PostMapping
    public Object create(@RequestBody CreateShipmentOrderDTO createShipmentOrderDTO) {
        return restUseCaseExecutor
                .from(createShipmentOrderUseCase)
                .withInput(createShipmentOrderDTO)
                .mapOutputTo(CreateShipmentOrderResponseDTO.class)
                .presentWith(output -> defaultRestPresenter.present(output, HttpStatus.CREATED.value()))
                .execute();
    }

}
