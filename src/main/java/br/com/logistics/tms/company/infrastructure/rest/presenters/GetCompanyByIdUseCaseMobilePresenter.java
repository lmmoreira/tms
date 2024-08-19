package br.com.logistics.tms.company.infrastructure.rest.presenters;

import br.com.logistics.tms.commons.application.presenters.Presenter;
import br.com.logistics.tms.company.application.usecases.GetCompanyByIdUseCase;
import br.com.logistics.tms.company.application.usecases.GetCompanyByIdUseCase.Output;
import org.springframework.stereotype.Component;

@Component
public class GetCompanyByIdUseCaseMobilePresenter implements Presenter<GetCompanyByIdUseCase.Output, Object> {

    @Override
    public Object present(Output input) {
        return input.company().name();
    }

    @Override
    public Object present(Throwable error) {
        return null;
    }
}
