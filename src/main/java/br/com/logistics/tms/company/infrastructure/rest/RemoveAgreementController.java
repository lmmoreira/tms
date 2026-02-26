package br.com.logistics.tms.company.infrastructure.rest;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.DatabaseRole;
import br.com.logistics.tms.commons.infrastructure.presenters.rest.DefaultRestPresenter;
import br.com.logistics.tms.commons.infrastructure.usecases.RestUseCaseExecutor;
import br.com.logistics.tms.company.application.usecases.RemoveAgreementUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("companies/{companyId}/agreements")
@Cqrs(DatabaseRole.WRITE)
public class RemoveAgreementController {

    private final RemoveAgreementUseCase removeAgreementUseCase;
    private final DefaultRestPresenter defaultRestPresenter;
    private final RestUseCaseExecutor restUseCaseExecutor;

    public RemoveAgreementController(final RemoveAgreementUseCase removeAgreementUseCase,
                                    final DefaultRestPresenter defaultRestPresenter,
                                    final RestUseCaseExecutor restUseCaseExecutor) {
        this.removeAgreementUseCase = removeAgreementUseCase;
        this.defaultRestPresenter = defaultRestPresenter;
        this.restUseCaseExecutor = restUseCaseExecutor;
    }

    @DeleteMapping("/{agreementId}")
    public Object remove(@PathVariable final UUID companyId, @PathVariable final UUID agreementId) {
        return restUseCaseExecutor
                .from(removeAgreementUseCase)
                .withInput(new RemoveAgreementUseCase.Input(agreementId))
                .presentWith(output -> defaultRestPresenter.present(null, HttpStatus.NO_CONTENT.value()))
                .execute();
    }
}
