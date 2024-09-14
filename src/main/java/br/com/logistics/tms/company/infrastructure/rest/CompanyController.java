package br.com.logistics.tms.company.infrastructure.rest;

import br.com.logistics.tms.company.application.usecases.AddConfigurationToCompanyUseCase;
import br.com.logistics.tms.company.application.usecases.GetCompanyByIdUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "companies")
public class CompanyController {

    private AddConfigurationToCompanyUseCase addConfigurationToCompanyUseCase;
    private GetCompanyByIdUseCase getCompanyByIdUseCase;

    public CompanyController(
        AddConfigurationToCompanyUseCase addConfigurationToCompanyUseCase,
        GetCompanyByIdUseCase getCompanyByIdUseCase) {
        this.addConfigurationToCompanyUseCase = addConfigurationToCompanyUseCase;
        this.getCompanyByIdUseCase = getCompanyByIdUseCase;
    }

    @GetMapping("/{id}")
    public Object get(@PathVariable String id) {
        return getCompanyByIdUseCase.execute(new GetCompanyByIdUseCase.Input(id));
    }

    @PostMapping(path = "/{id}/config/{configurationId}")
    public ResponseEntity<Void> addConfigurationToCompany(@PathVariable String id,
        @PathVariable String configurationId) {
        addConfigurationToCompanyUseCase.execute(
            new AddConfigurationToCompanyUseCase.Input(id, configurationId, "valor"));
        return ResponseEntity.ok().build();
    }

}
