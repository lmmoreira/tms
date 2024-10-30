package br.com.logistics.tms.company.application.usecases;

import br.com.logistics.tms.commons.application.gateways.DomainEventQueueGateway;
import br.com.logistics.tms.commons.application.usecases.UseCase;
import br.com.logistics.tms.commons.domain.exception.ValidationException;
import br.com.logistics.tms.company.application.repositories.CompanyRepository;
import br.com.logistics.tms.company.domain.Cnpj;
import br.com.logistics.tms.company.domain.Company;
import br.com.logistics.tms.company.domain.CompanyCreated;
import br.com.logistics.tms.company.domain.Type;
import java.util.Set;
import java.util.stream.Collectors;

public class CreateCompanyUseCase extends
    UseCase<CreateCompanyUseCase.Input, CreateCompanyUseCase.Output> {

    private final CompanyRepository companyRepository;
    private final DomainEventQueueGateway<CompanyCreated> companyCreatedDomainEventQueueGateway;

    public CreateCompanyUseCase(CompanyRepository companyRepository,
        DomainEventQueueGateway<CompanyCreated> companyCreatedDomainEventQueueGateway) {
        this.companyRepository = companyRepository;
        this.companyCreatedDomainEventQueueGateway = companyCreatedDomainEventQueueGateway;
    }

    public Output execute(final Input input) {
        if (companyRepository.getCompanyByCnpj(new Cnpj(input.cnpj)).isPresent()) {
            throw new ValidationException("Company already exists");
        }

        final Company company = companyRepository.create(
            Company.createCompany(input.name, input.cnpj,
                input.types.stream().map(Type::with).collect(
                    Collectors.toSet())));

        final CompanyCreated event = new CompanyCreated(company.companyId().value());
        companyCreatedDomainEventQueueGateway.publish(event);

        return new Output(company.companyId().value().toString(), company.name(),
            company.cnpj().value(),
            company.types().stream().map(Type::toString).collect(Collectors.toSet()));
    }

    public record Input(String name, String cnpj, Set<String> types) {

    }

    public record Output(String companyId, String name, String cnpj, Set<String> types) {

    }

}
