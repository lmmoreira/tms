package br.com.logistics.tms.company.infrastructure.rest;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.DatabaseRole;
import br.com.logistics.tms.commons.infrastructure.presenters.rest.DefaultRestPresenter;
import br.com.logistics.tms.commons.infrastructure.usecases.RestUseCaseExecutor;
import br.com.logistics.tms.company.application.usecases.UpdateAgreementUseCase;
import br.com.logistics.tms.company.infrastructure.dto.AgreementUpdateResponseDTO;
import br.com.logistics.tms.company.infrastructure.dto.UpdateAgreementDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("companies/{companyId}/agreements")
@Cqrs(DatabaseRole.WRITE)
public class UpdateAgreementController {

    private final UpdateAgreementUseCase updateAgreementUseCase;
    private final DefaultRestPresenter defaultRestPresenter;
    private final RestUseCaseExecutor restUseCaseExecutor;

    public UpdateAgreementController(final UpdateAgreementUseCase updateAgreementUseCase,
                                    final DefaultRestPresenter defaultRestPresenter,
                                    final RestUseCaseExecutor restUseCaseExecutor) {
        this.updateAgreementUseCase = updateAgreementUseCase;
        this.defaultRestPresenter = defaultRestPresenter;
        this.restUseCaseExecutor = restUseCaseExecutor;
    }

    @PutMapping("/{agreementId}")
    public Object update(@PathVariable final UUID companyId, @PathVariable final UUID agreementId, @RequestBody final UpdateAgreementDTO dto) {
        return restUseCaseExecutor
                .from(updateAgreementUseCase)
                .withInput(dto.toInput(agreementId))
                .mapOutputTo(AgreementUpdateResponseDTO.class)
                .presentWith(output -> defaultRestPresenter.present(output, HttpStatus.OK.value()))
                .execute();
    }
}
