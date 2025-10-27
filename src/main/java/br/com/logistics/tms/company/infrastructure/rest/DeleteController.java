package br.com.logistics.tms.company.infrastructure.rest;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.Role;
import br.com.logistics.tms.commons.infrastructure.presenters.rest.DefaultRestPresenter;
import br.com.logistics.tms.commons.infrastructure.usecases.RestUseCaseExecutor;
import br.com.logistics.tms.company.application.usecases.DeleteCompanyByIdUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "companies")
@Slf4j
@Cqrs(Role.WRITE)
public class DeleteController {

    private final DeleteCompanyByIdUseCase deleteCompanyByIdUseCase;
    private final DefaultRestPresenter defaultRestPresenter;
    private final RestUseCaseExecutor restUseCaseExecutor;

    public DeleteController(DefaultRestPresenter defaultRestPresenter,
                            DeleteCompanyByIdUseCase deleteCompanyByIdUseCase,
                            RestUseCaseExecutor restUseCaseExecutor
    ) {
        this.defaultRestPresenter = defaultRestPresenter;
        this.deleteCompanyByIdUseCase = deleteCompanyByIdUseCase;
        this.restUseCaseExecutor = restUseCaseExecutor;
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
