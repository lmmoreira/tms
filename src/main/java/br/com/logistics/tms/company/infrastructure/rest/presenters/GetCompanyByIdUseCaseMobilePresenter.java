package br.com.logistics.tms.company.infrastructure.rest.presenters;

import br.com.logistics.tms.commons.application.presenters.Presenter;
import br.com.logistics.tms.commons.application.presenters.View;
import br.com.logistics.tms.company.application.usecases.GetCompanyByIdUseCase;
import br.com.logistics.tms.company.application.usecases.GetCompanyByIdUseCase.Output;
import org.springframework.stereotype.Component;

@Component
public class GetCompanyByIdUseCaseMobilePresenter implements Presenter<GetCompanyByIdUseCase.Output, View> {

    @Override
    public View present(Output input) {
        return null;
            //input.company().name()            ;
    }

    @Override
    public View present(Throwable error) {
        return null;
    }
}
