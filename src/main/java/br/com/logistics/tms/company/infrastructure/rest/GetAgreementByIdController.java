package br.com.logistics.tms.company.infrastructure.rest;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.DatabaseRole;
import br.com.logistics.tms.commons.infrastructure.presenters.rest.DefaultRestPresenter;
import br.com.logistics.tms.commons.infrastructure.usecases.RestUseCaseExecutor;
import br.com.logistics.tms.company.application.usecases.GetAgreementByIdUseCase;
import br.com.logistics.tms.company.infrastructure.dto.AgreementDetailResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("agreements")
@Cqrs(DatabaseRole.READ)
public class GetAgreementByIdController {

    private final GetAgreementByIdUseCase getAgreementByIdUseCase;
    private final DefaultRestPresenter defaultRestPresenter;
    private final RestUseCaseExecutor restUseCaseExecutor;

    public GetAgreementByIdController(final GetAgreementByIdUseCase getAgreementByIdUseCase,
                                     final DefaultRestPresenter defaultRestPresenter,
                                     final RestUseCaseExecutor restUseCaseExecutor) {
        this.getAgreementByIdUseCase = getAgreementByIdUseCase;
        this.defaultRestPresenter = defaultRestPresenter;
        this.restUseCaseExecutor = restUseCaseExecutor;
    }

    @GetMapping("/{agreementId}")
    public Object getAgreement(@PathVariable final UUID agreementId) {
        return restUseCaseExecutor
                .from(getAgreementByIdUseCase)
                .withInput(new GetAgreementByIdUseCase.Input(agreementId))
                .mapOutputTo(AgreementDetailResponseDTO.class)
                .presentWith(output -> defaultRestPresenter.present(output, HttpStatus.OK.value()))
                .execute();
    }
}
