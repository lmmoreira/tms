package br.com.logistics.tms.company.infrastructure.rest;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.Role;
import br.com.logistics.tms.commons.infrastructure.usecases.RestUseCaseExecutor;
import br.com.logistics.tms.company.application.usecases.UpdateCompanyUseCase;
import br.com.logistics.tms.company.infrastructure.presenters.rest.dto.UpdateCompanyDTO;
import br.com.logistics.tms.company.infrastructure.presenters.rest.dto.UpdateCompanyResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "companies")
@Slf4j
@Cqrs(Role.WRITE)
public class UpdateController {

    private final UpdateCompanyUseCase updateCompanyUseCase;
    private final RestUseCaseExecutor restUseCaseExecutor;

    public UpdateController(UpdateCompanyUseCase updateCompanyUseCase,
                            RestUseCaseExecutor restUseCaseExecutor
    ) {
        this.updateCompanyUseCase = updateCompanyUseCase;
        this.restUseCaseExecutor = restUseCaseExecutor;
    }

    @PutMapping("/{companyId}")
    public Object update(@PathVariable String companyId, @RequestBody UpdateCompanyDTO updateCompanyDTO) {
        return restUseCaseExecutor
                .from(updateCompanyUseCase)
                .withInput(new UpdateCompanyUseCase.Input(companyId,
                        updateCompanyDTO.name(),
                        updateCompanyDTO.cnpj(),
                        updateCompanyDTO.types(),
                        updateCompanyDTO.configuration()))
                .mapOutputTo(UpdateCompanyResponseDTO.class)
                .execute();
    }

}
