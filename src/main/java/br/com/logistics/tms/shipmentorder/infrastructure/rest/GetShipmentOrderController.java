package br.com.logistics.tms.shipmentorder.infrastructure.rest;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.DatabaseRole;
import br.com.logistics.tms.commons.infrastructure.presenters.rest.DefaultRestPresenter;
import br.com.logistics.tms.commons.infrastructure.usecases.RestUseCaseExecutor;
import br.com.logistics.tms.shipmentorder.application.usecases.GetShipmentOrderByCompanyIdUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "orders")
@Cqrs(DatabaseRole.READ)
public class GetShipmentOrderController {

    private final RestUseCaseExecutor restUseCaseExecutor;
    private final DefaultRestPresenter defaultRestPresenter;
    private final GetShipmentOrderByCompanyIdUseCase getShipmentOrderByCompanyIdUseCase;

    @Autowired
    public GetShipmentOrderController(RestUseCaseExecutor restUseCaseExecutor,
                                      DefaultRestPresenter defaultRestPresenter,
                                      GetShipmentOrderByCompanyIdUseCase getShipmentOrderByCompanyIdUseCase) {
        this.getShipmentOrderByCompanyIdUseCase = getShipmentOrderByCompanyIdUseCase;
        this.defaultRestPresenter = defaultRestPresenter;
        this.restUseCaseExecutor = restUseCaseExecutor;
    }

    @GetMapping("/{companyId}")
    public Object get(
            @RequestHeader Map<String, String> headers,
            @PathVariable UUID companyId,
            @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return restUseCaseExecutor
                .from(getShipmentOrderByCompanyIdUseCase)
                .withInput(new GetShipmentOrderByCompanyIdUseCase.Input(companyId, pageable.getPageNumber(), pageable.getPageSize()))
                .presentWith(defaultRestPresenter)
                .execute();
    }

}
