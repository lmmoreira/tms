package br.com.logistics.tms.company.infrastructure.rest.presenters;

import br.com.logistics.tms.commons.application.presenters.Presenter;
import br.com.logistics.tms.company.application.usecases.GetCompanyByIdUseCase;
import org.springframework.stereotype.Component;

@Component
public class GetCompanyByIdCliPresenter implements Presenter<GetCompanyByIdUseCase.Output, String> {

    @Override
    public String present() {
        return "";
    }

    @Override
    public String present(GetCompanyByIdUseCase.Output input) {
        return present(input, null);
    }

    @Override
    public String present(Integer customSuccessStatusCode) {
        return "";
    }

    @Override
    public String present(GetCompanyByIdUseCase.Output input, Integer customSuccessStatusCode) {
        return "Cli Presenter - Company Name: " + input.name() + ", CNPJ: " + input.cnpj();
    }

    @Override
    public String present(Throwable error) {
        return "An error has been occurred: " + error.getMessage();
    }

}
