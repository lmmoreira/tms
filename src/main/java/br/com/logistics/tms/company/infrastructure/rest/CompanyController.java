package br.com.logistics.tms.company.infrastructure.rest;

import br.com.logistics.tms.commons.application.presenters.View;
import br.com.logistics.tms.commons.infrastructure.rest.presenter.DefaultRestPresenter;
import br.com.logistics.tms.company.application.usecases.AddConfigurationToCompanyUseCase;
import br.com.logistics.tms.company.application.usecases.CreateCompanyUseCase;
import br.com.logistics.tms.company.application.usecases.GetCompanyByIdUseCase;
import br.com.logistics.tms.company.infrastructure.rest.dto.CreateCompanyDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping(value = "companies")
@Slf4j
public class CompanyController {

    private AddConfigurationToCompanyUseCase addConfigurationToCompanyUseCase;
    private GetCompanyByIdUseCase getCompanyByIdUseCase;
    private CreateCompanyUseCase createCompanyUseCase;
    private DefaultRestPresenter<CreateCompanyUseCase.Output> defaultRestPresenter;

    public CompanyController(AddConfigurationToCompanyUseCase addConfigurationToCompanyUseCase,
        GetCompanyByIdUseCase getCompanyByIdUseCase,
        CreateCompanyUseCase createCompanyUseCase,
        DefaultRestPresenter<CreateCompanyUseCase.Output> defaultRestPresenter) {
        this.addConfigurationToCompanyUseCase = addConfigurationToCompanyUseCase;
        this.getCompanyByIdUseCase = getCompanyByIdUseCase;
        this.createCompanyUseCase = createCompanyUseCase;
        this.defaultRestPresenter = defaultRestPresenter;
    }

    @GetMapping("/{id}")
    public Object get(@PathVariable String id) {
        return getCompanyByIdUseCase.execute(new GetCompanyByIdUseCase.Input(id));
    }

    @GetMapping("/leo")
    public Object getLeo(@RequestHeader Map<String, String> headers) {
        log.info("Leonardo Machado Moreira");
        return "leo";
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
        final View output =
            createCompanyUseCase.execute(new CreateCompanyUseCase.Input(createCompanyDTO.name(),
                createCompanyDTO.cnpj(), createCompanyDTO.types()), defaultRestPresenter);
        return ResponseEntity.created(URI.create("/companies/")).body(output.of());
    }
}
