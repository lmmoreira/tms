package br.com.logistics.tms.company.infrastructure.rest;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.DatabaseRole;
import br.com.logistics.tms.commons.application.presenters.Presenter;
import br.com.logistics.tms.commons.infrastructure.presenters.rest.DefaultRestPresenter;
import br.com.logistics.tms.commons.infrastructure.usecases.RestUseCaseExecutor;
import br.com.logistics.tms.company.application.usecases.GetCompanyByIdUseCase;
import br.com.logistics.tms.company.infrastructure.presenters.GetCompanyByIdCliPresenter;
import br.com.logistics.tms.company.infrastructure.dto.GetCompanyByIdMobileResponseDTO;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "companies")
@Cqrs(DatabaseRole.READ)
public class GetByIdController {

    private final GetCompanyByIdUseCase getCompanyByIdUseCase;
    private final DefaultRestPresenter defaultRestPresenter;
    private final GetCompanyByIdCliPresenter getCompanyByIdCliPresenter;
    private final RestUseCaseExecutor restUseCaseExecutor;

    public GetByIdController(GetCompanyByIdUseCase getCompanyByIdUseCase,
                             DefaultRestPresenter defaultRestPresenter,
                             RestUseCaseExecutor restUseCaseExecutor,
                             GetCompanyByIdCliPresenter getCompanyByIdCliPresenter) {
        this.getCompanyByIdUseCase = getCompanyByIdUseCase;
        this.defaultRestPresenter = defaultRestPresenter;
        this.restUseCaseExecutor = restUseCaseExecutor;
        this.getCompanyByIdCliPresenter = getCompanyByIdCliPresenter;
    }

    @GetMapping("/{companyId}")
    public Object get(
            @RequestHeader Map<String, String> headers,
            @PathVariable UUID companyId) {
        final Presenter<?, ?> presenter = Boolean.parseBoolean(headers.get("cli")) ? getCompanyByIdCliPresenter : defaultRestPresenter;
        return restUseCaseExecutor
                .from(getCompanyByIdUseCase)
                .withInput(new GetCompanyByIdUseCase.Input(companyId))
                .mapOutputTo(Boolean.parseBoolean(headers.get("mobile")) ? GetCompanyByIdMobileResponseDTO.class : null)
                .presentWith(presenter)
                .execute();
    }

}
