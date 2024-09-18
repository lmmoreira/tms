package br.com.logistics.tms.company.infrastructure.rest;

import br.com.logistics.tms.commons.domain.exception.ValidationException;
import br.com.logistics.tms.company.application.usecases.AddConfigurationToCompanyUseCase;
import br.com.logistics.tms.company.application.usecases.CreateCompanyUseCase;
import br.com.logistics.tms.company.application.usecases.GetCompanyByIdUseCase;
import br.com.logistics.tms.company.infrastructure.dto.CreateCompanyDTO;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "companies")
public class CompanyController {

    private AddConfigurationToCompanyUseCase addConfigurationToCompanyUseCase;
    private GetCompanyByIdUseCase getCompanyByIdUseCase;
    private CreateCompanyUseCase createCompanyUseCase;

    public CompanyController(AddConfigurationToCompanyUseCase addConfigurationToCompanyUseCase,
        GetCompanyByIdUseCase getCompanyByIdUseCase, CreateCompanyUseCase createCompanyUseCase) {
        this.addConfigurationToCompanyUseCase = addConfigurationToCompanyUseCase;
        this.getCompanyByIdUseCase = getCompanyByIdUseCase;
        this.createCompanyUseCase = createCompanyUseCase;
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

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateCompanyDTO createCompanyDTO) {
        try {
            final var output =
                createCompanyUseCase.execute(new CreateCompanyUseCase.Input(createCompanyDTO.name(),
                    createCompanyDTO.cnpj()));

            return ResponseEntity.created(URI.create("/companies/" + output.id())).body(output);
        } catch (ValidationException ex) {
            return ResponseEntity.unprocessableEntity().body(ex.getMessage());
        }
    }

}
