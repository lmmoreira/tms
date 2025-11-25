package br.com.logistics.tms.company.infrastructure.rest;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.DatabaseRole;
import br.com.logistics.tms.commons.infrastructure.presenters.rest.DefaultRestPresenter;
import br.com.logistics.tms.commons.infrastructure.usecases.RestUseCaseExecutor;
import br.com.logistics.tms.company.application.usecases.DeleteCompanyUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(value = "companies")
@Cqrs(DatabaseRole.WRITE)
public class DeleteController {

    private final DeleteCompanyUseCase deleteCompanyUseCase;
    private final DefaultRestPresenter defaultRestPresenter;
    private final RestUseCaseExecutor restUseCaseExecutor;

    public DeleteController(DefaultRestPresenter defaultRestPresenter,
                            DeleteCompanyUseCase deleteCompanyUseCase,
                            RestUseCaseExecutor restUseCaseExecutor
    ) {
        this.defaultRestPresenter = defaultRestPresenter;
        this.deleteCompanyUseCase = deleteCompanyUseCase;
        this.restUseCaseExecutor = restUseCaseExecutor;
    }

    @DeleteMapping("/{companyId}")
    public Object delete(@PathVariable UUID companyId) {
        return restUseCaseExecutor
                .from(deleteCompanyUseCase)
                .withInput(new DeleteCompanyUseCase.Input(companyId))
                .presentWith(output -> defaultRestPresenter.present(HttpStatus.NO_CONTENT.value()))
                .execute();
    }

}
