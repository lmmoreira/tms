package br.com.logistics.tms.company.infrastructure.rest;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.DatabaseRole;
import br.com.logistics.tms.commons.infrastructure.presenters.rest.DefaultRestPresenter;
import br.com.logistics.tms.commons.infrastructure.usecases.RestUseCaseExecutor;
import br.com.logistics.tms.company.application.usecases.GetAgreementsByCompanyUseCase;
import br.com.logistics.tms.company.infrastructure.dto.AgreementsListResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("companies/{companyId}/agreements")
@Cqrs(DatabaseRole.READ)
public class GetAgreementsByCompanyController {

    private final GetAgreementsByCompanyUseCase getAgreementsByCompanyUseCase;
    private final DefaultRestPresenter defaultRestPresenter;
    private final RestUseCaseExecutor restUseCaseExecutor;

    public GetAgreementsByCompanyController(final GetAgreementsByCompanyUseCase getAgreementsByCompanyUseCase,
                                           final DefaultRestPresenter defaultRestPresenter,
                                           final RestUseCaseExecutor restUseCaseExecutor) {
        this.getAgreementsByCompanyUseCase = getAgreementsByCompanyUseCase;
        this.defaultRestPresenter = defaultRestPresenter;
        this.restUseCaseExecutor = restUseCaseExecutor;
    }

    @GetMapping
    public Object getAgreements(@PathVariable final UUID companyId) {
        return restUseCaseExecutor
                .from(getAgreementsByCompanyUseCase)
                .withInput(new GetAgreementsByCompanyUseCase.Input(companyId))
                .mapOutputTo(AgreementsListResponseDTO.class)
                .presentWith(output -> defaultRestPresenter.present(output, HttpStatus.OK.value()))
                .execute();
    }
}
