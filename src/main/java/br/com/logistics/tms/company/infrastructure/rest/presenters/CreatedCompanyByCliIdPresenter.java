package br.com.logistics.tms.company.infrastructure.rest.presenters;

import br.com.logistics.tms.commons.application.presenters.Presenter;
import br.com.logistics.tms.company.application.usecases.CreateCompanyUseCase;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class CreatedCompanyByCliIdPresenter implements Presenter<CreateCompanyUseCase.Output, String> {

    @Override
    public String present(CreateCompanyUseCase.Output input) {
        return "From UseCase Directly - Company Name: " + input.name() + ", CNPJ: " + input.cnpj();
    }

    @Override
    public String present(Throwable error) {
        return "An error has been occurred: " + error.getMessage();
    }

}
