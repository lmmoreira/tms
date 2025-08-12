package br.com.logistics.tms.company.infrastructure.rest;

import br.com.logistics.tms.commons.application.usecases.UseCaseExecutor;
import br.com.logistics.tms.commons.infrastructure.presenters.rest.DefaultRestPresenter;
import br.com.logistics.tms.commons.infrastructure.usecases.RestUseCaseExecutor;
import br.com.logistics.tms.company.application.usecases.AddConfigurationToCompanyUseCase;
import br.com.logistics.tms.company.application.usecases.CreateCompanyUseCase;
import br.com.logistics.tms.company.application.usecases.GetCompanyByIdUseCase;
import br.com.logistics.tms.company.infrastructure.rest.dto.CreateCompanyDTO;
import br.com.logistics.tms.company.infrastructure.rest.dto.CreateCompanyResponseDTO;
import br.com.logistics.tms.company.infrastructure.rest.presenters.CreatedCompanyByCliIdPresenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "companies")
@Slf4j
public class CompanyController {

    private final AddConfigurationToCompanyUseCase addConfigurationToCompanyUseCase;
    private final GetCompanyByIdUseCase getCompanyByIdUseCase;
    private final CreateCompanyUseCase createCompanyUseCase;
    private final DefaultRestPresenter defaultRestPresenter;
    private final CreatedCompanyByCliIdPresenter createdCompanyByCliIdPresenter;
    private final RestUseCaseExecutor restUseCaseExecutor;

    public CompanyController(AddConfigurationToCompanyUseCase addConfigurationToCompanyUseCase,
                             GetCompanyByIdUseCase getCompanyByIdUseCase,
                             CreateCompanyUseCase createCompanyUseCase,
                             DefaultRestPresenter defaultRestPresenter,
                             RestUseCaseExecutor restUseCaseExecutor,
                             CreatedCompanyByCliIdPresenter createdCompanyByCliIdPresenter) {
        this.addConfigurationToCompanyUseCase = addConfigurationToCompanyUseCase;
        this.getCompanyByIdUseCase = getCompanyByIdUseCase;
        this.createCompanyUseCase = createCompanyUseCase;
        this.defaultRestPresenter = defaultRestPresenter;
        this.restUseCaseExecutor = restUseCaseExecutor;
        this.createdCompanyByCliIdPresenter = createdCompanyByCliIdPresenter;
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

        if ("1".equals(headers.get("cli"))) {
            return UseCaseExecutor
                    .from(createCompanyUseCase)
                    .withInput(createCompanyDTO)
                    .presentWith(createdCompanyByCliIdPresenter)
                    .execute();
        }

        if ("1".equals(headers.get("fullentity"))) {
            return UseCaseExecutor
                    .from(createCompanyUseCase)
                    .withInput(new CreateCompanyUseCase.Input(createCompanyDTO.name(),
                            createCompanyDTO.cnpj(), createCompanyDTO.types()))
                    .presentWith(defaultRestPresenter, output -> defaultRestPresenter.present(output,
                            HttpStatus.CREATED.value()))
                    .execute();
        }

        return restUseCaseExecutor
                .from(createCompanyUseCase)
                .withInput(createCompanyDTO)
                .mapOutputTo(CreateCompanyResponseDTO.class)
                .execute();
    }

}
