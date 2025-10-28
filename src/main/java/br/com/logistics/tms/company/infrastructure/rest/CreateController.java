package br.com.logistics.tms.company.infrastructure.rest;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.Role;
import br.com.logistics.tms.commons.infrastructure.presenters.rest.DefaultRestPresenter;
import br.com.logistics.tms.commons.infrastructure.usecases.RestUseCaseExecutor;
import br.com.logistics.tms.company.application.usecases.CreateCompanyUseCase;
import br.com.logistics.tms.company.infrastructure.presenters.rest.dto.CreateCompanyDTO;
import br.com.logistics.tms.company.infrastructure.presenters.rest.dto.CreateCompanyResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "companies")
@Cqrs(Role.WRITE)
public class CreateController {

    private final CreateCompanyUseCase createCompanyUseCase;
    private final DefaultRestPresenter defaultRestPresenter;
    private final RestUseCaseExecutor restUseCaseExecutor;

    public CreateController(CreateCompanyUseCase createCompanyUseCase,
                            DefaultRestPresenter defaultRestPresenter,
                            RestUseCaseExecutor restUseCaseExecutor) {
        this.createCompanyUseCase = createCompanyUseCase;
        this.defaultRestPresenter = defaultRestPresenter;
        this.restUseCaseExecutor = restUseCaseExecutor;
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

}
