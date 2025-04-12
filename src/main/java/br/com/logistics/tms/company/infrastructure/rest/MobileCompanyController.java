package br.com.logistics.tms.company.infrastructure.rest;

import br.com.logistics.tms.company.application.usecases.GetCompanyByIdUseCase;
import br.com.logistics.tms.company.infrastructure.rest.presenters.GetCompanyByIdUseCaseMobilePresenter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "mobile-companies")
public class MobileCompanyController {

    private GetCompanyByIdUseCase getCompanyByIdUseCase;
    private GetCompanyByIdUseCaseMobilePresenter getCompanyByIdUseCaseMobilePresenter;

    public MobileCompanyController(GetCompanyByIdUseCase getCompanyByIdUseCase,
        GetCompanyByIdUseCaseMobilePresenter getCompanyByIdUseCaseMobilePresenter) {
        this.getCompanyByIdUseCase = getCompanyByIdUseCase;
        this.getCompanyByIdUseCaseMobilePresenter = getCompanyByIdUseCaseMobilePresenter;
    }

    @GetMapping("/{id}")
    public Object get(@PathVariable String id) {
        /*return getCompanyByIdUseCase.execute(new GetCompanyByIdUseCase.Input(id),
            getCompanyByIdUseCaseMobilePresenter);*/
        return null;
    }

}
