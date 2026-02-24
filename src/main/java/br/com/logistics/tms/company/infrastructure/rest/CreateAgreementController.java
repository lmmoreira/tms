package br.com.logistics.tms.company.infrastructure.rest;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.DatabaseRole;
import br.com.logistics.tms.commons.infrastructure.presenters.rest.DefaultRestPresenter;
import br.com.logistics.tms.commons.infrastructure.usecases.RestUseCaseExecutor;
import br.com.logistics.tms.company.application.usecases.CreateAgreementUseCase;
import br.com.logistics.tms.company.infrastructure.dto.AgreementResponseDTO;
import br.com.logistics.tms.company.infrastructure.dto.CreateAgreementDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("companies/{companyId}/agreements")
@Cqrs(DatabaseRole.WRITE)
public class CreateAgreementController {

    private final CreateAgreementUseCase createAgreementUseCase;
    private final DefaultRestPresenter defaultRestPresenter;
    private final RestUseCaseExecutor restUseCaseExecutor;

    public CreateAgreementController(final CreateAgreementUseCase createAgreementUseCase,
                                    final DefaultRestPresenter defaultRestPresenter,
                                    final RestUseCaseExecutor restUseCaseExecutor) {
        this.createAgreementUseCase = createAgreementUseCase;
        this.defaultRestPresenter = defaultRestPresenter;
        this.restUseCaseExecutor = restUseCaseExecutor;
    }

    @PostMapping
    public Object create(@PathVariable final UUID companyId, @RequestBody final CreateAgreementDTO dto) {
        return restUseCaseExecutor
                .from(createAgreementUseCase)
                .withInput(dto.toInput(companyId))
                .mapOutputTo(AgreementResponseDTO.class)
                .presentWith(output -> defaultRestPresenter.present(output, HttpStatus.CREATED.value()))
                .execute();
    }
}
