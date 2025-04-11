package br.com.logistics.tms.company.infrastructure.rest;

import br.com.logistics.tms.commons.application.presenters.Presenter;
import br.com.logistics.tms.commons.infrastructure.presenters.rest.DefaultRestPresenter;
import br.com.logistics.tms.company.application.usecases.AddConfigurationToCompanyUseCase;
import br.com.logistics.tms.company.application.usecases.CreateCompanyUseCase;
import br.com.logistics.tms.company.application.usecases.GetCompanyByIdUseCase;
import br.com.logistics.tms.company.infrastructure.rest.dto.CreateCompanyDTO;
import br.com.logistics.tms.company.infrastructure.rest.dto.CreateCompanyResponseDTO;
import br.com.logistics.tms.company.infrastructure.rest.presenters.CreatedCompanyByCliIdPresenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "companies")
@Slf4j
public class CompanyController {

    private AddConfigurationToCompanyUseCase addConfigurationToCompanyUseCase;
    private GetCompanyByIdUseCase getCompanyByIdUseCase;
    private CreateCompanyUseCase createCompanyUseCase;
    private ObjectFactory<DefaultRestPresenter> defaultRestPresenterObjectFactory;
    private ObjectFactory<CreatedCompanyByCliIdPresenter> createdCompanyByCliIdPresenterFactory;

    public CompanyController(AddConfigurationToCompanyUseCase addConfigurationToCompanyUseCase,
                             GetCompanyByIdUseCase getCompanyByIdUseCase,
                             CreateCompanyUseCase createCompanyUseCase,
                             ObjectFactory<DefaultRestPresenter> defaultRestPresenterObjectFactory,
                             ObjectFactory<CreatedCompanyByCliIdPresenter> createdCompanyByCliIdPresenterFactory) {
        this.addConfigurationToCompanyUseCase = addConfigurationToCompanyUseCase;
        this.getCompanyByIdUseCase = getCompanyByIdUseCase;
        this.createCompanyUseCase = createCompanyUseCase;
        this.defaultRestPresenterObjectFactory = defaultRestPresenterObjectFactory;
        this.createdCompanyByCliIdPresenterFactory = createdCompanyByCliIdPresenterFactory;
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
    public Object create(@RequestHeader Map<String, String> headers,
                         @RequestBody CreateCompanyDTO createCompanyDTO) {
        Presenter<CreateCompanyResponseDTO, ResponseEntity<?>> presenter = defaultRestPresenterObjectFactory.getObject().withResponseStatus(HttpStatus.CREATED).typed();
        Presenter<CreateCompanyUseCase.Output, String> cliPresenter = createdCompanyByCliIdPresenterFactory.getObject();

        if (headers.get("cli") != null && headers.get("cli").equals("1")) {
            return createCompanyUseCase.execute(new CreateCompanyUseCase.Input(createCompanyDTO.name(),
                    createCompanyDTO.cnpj(), createCompanyDTO.types()), cliPresenter);
        }

        return createCompanyUseCase.execute(new CreateCompanyUseCase.Input(createCompanyDTO.name(),
                createCompanyDTO.cnpj(), createCompanyDTO.types()), CreateCompanyResponseDTO.class, presenter);
    }

}
