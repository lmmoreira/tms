package br.com.logistics.tms.company.infrastructure.rest.presenters;

import br.com.logistics.tms.commons.application.presenters.Presenter;
import br.com.logistics.tms.company.application.usecases.CreateCompanyUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class CreatedCompanyByCliIdPresenter implements Presenter<CreateCompanyUseCase.Output, String> {

    @Override
    public String present(CreateCompanyUseCase.Output input) {
        return "From UseCase Directly - Company Name: " + input.name() + ", CNPJ: " + input.cnpj();
    }

    @Override
    public String present(CreateCompanyUseCase.Output input, HttpStatus successStatus) {
        return "From UseCase Directly - Company Name: " + input.name() + ", CNPJ: " + input.cnpj();
    }

    @Override
    public String present(Throwable error) {
        return "An error has been occurred: " + error.getMessage();
    }

}
