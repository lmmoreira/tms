package br.com.logistics.tms.company.infrastructure.rest;

import br.com.logistics.tms.commons.application.presenters.Presenter;
import br.com.logistics.tms.commons.infrastructure.presenters.rest.DefaultRestPresenter;
import br.com.logistics.tms.commons.infrastructure.usecases.RestUseCaseExecutor;
import br.com.logistics.tms.company.application.usecases.CreateCompanyUseCase;
import br.com.logistics.tms.company.application.usecases.DeleteCompanyByIdUseCase;
import br.com.logistics.tms.company.application.usecases.GetCompanyByIdUseCase;
import br.com.logistics.tms.company.application.usecases.UpdateCompanyUseCase;
import br.com.logistics.tms.company.infrastructure.rest.dto.*;
import br.com.logistics.tms.company.infrastructure.rest.presenters.GetCompanyByIdCliPresenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "companies")
@Slf4j
public class CompanyController {

    private final GetCompanyByIdUseCase getCompanyByIdUseCase;
    private final CreateCompanyUseCase createCompanyUseCase;
    private final UpdateCompanyUseCase updateCompanyUseCase;
    private final DeleteCompanyByIdUseCase deleteCompanyByIdUseCase;
    private final DefaultRestPresenter defaultRestPresenter;
    private final GetCompanyByIdCliPresenter getCompanyByIdCliPresenter;
    private final RestUseCaseExecutor restUseCaseExecutor;

    public CompanyController(GetCompanyByIdUseCase getCompanyByIdUseCase,
                             CreateCompanyUseCase createCompanyUseCase,
                             UpdateCompanyUseCase updateCompanyUseCase,
                             DefaultRestPresenter defaultRestPresenter,
                             DeleteCompanyByIdUseCase deleteCompanyByIdUseCase,
                             RestUseCaseExecutor restUseCaseExecutor,
                             GetCompanyByIdCliPresenter getCompanyByIdCliPresenter) {
        this.getCompanyByIdUseCase = getCompanyByIdUseCase;
        this.createCompanyUseCase = createCompanyUseCase;
        this.updateCompanyUseCase = updateCompanyUseCase;
        this.defaultRestPresenter = defaultRestPresenter;
        this.deleteCompanyByIdUseCase = deleteCompanyByIdUseCase;
        this.restUseCaseExecutor = restUseCaseExecutor;
        this.getCompanyByIdCliPresenter = getCompanyByIdCliPresenter;
    }

    @GetMapping("/{companyId}")
    public Object get(
            @RequestHeader Map<String, String> headers,
            @PathVariable String companyId) {
        final Presenter<?, ?> presenter = Boolean.parseBoolean(headers.get("cli")) ? getCompanyByIdCliPresenter : defaultRestPresenter;
        return restUseCaseExecutor
                .from(getCompanyByIdUseCase)
                .withInput(new GetCompanyByIdUseCase.Input(companyId))
                .mapOutputTo(Boolean.parseBoolean(headers.get("mobile")) ? GetCompanyByIdMobileResponseDTO.class : null)
                .presentWith(presenter)
                .execute();
    }

    @PostMapping
    public Object create(@RequestBody CreateCompanyDTO createCompanyDTO) {
        return restUseCaseExecutor
                .from(createCompanyUseCase)
                .withInput(createCompanyDTO)
                .mapOutputTo(CreateCompanyResponseDTO.class)
                .presentWith(output -> defaultRestPresenter.present(output, HttpStatus.CREATED.value()))
                .execute();
    }

    @PutMapping("/{companyId}")
    public Object update(@PathVariable String companyId, @RequestBody UpdateCompanyDTO updateCompanyDTO) {
        return restUseCaseExecutor
                .from(updateCompanyUseCase)
                .withInput(new UpdateCompanyUseCase.Input(companyId,
                        updateCompanyDTO.name(),
                        updateCompanyDTO.cnpj(),
                        updateCompanyDTO.types(),
                        updateCompanyDTO.configuration()))
                .mapOutputTo(UpdateCompanyResponseDTO.class)
                .execute();
    }

    @DeleteMapping("/{companyId}")
    public Object delete(@PathVariable String companyId) {
        return restUseCaseExecutor
                .from(deleteCompanyByIdUseCase)
                .withInput(new DeleteCompanyByIdUseCase.Input(companyId))
                .presentWith(output -> defaultRestPresenter.present(HttpStatus.NO_CONTENT.value()))
                .execute();
    }

}
