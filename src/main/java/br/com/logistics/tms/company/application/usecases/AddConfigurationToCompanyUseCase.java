package br.com.logistics.tms.company.application.usecases;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.DatabaseRole;
import br.com.logistics.tms.commons.application.annotation.DomainService;
import br.com.logistics.tms.commons.application.usecases.UseCase;
import br.com.logistics.tms.company.domain.CompanyType;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@DomainService
@Cqrs(DatabaseRole.WRITE)
public class AddConfigurationToCompanyUseCase implements UseCase<AddConfigurationToCompanyUseCase.Input, AddConfigurationToCompanyUseCase.Output> {

    @Override
    public Output execute(Input input) {
        return null;
    }

    public record Input(String name) {
    }

    public record Output(UUID companyId) {
    }

}
