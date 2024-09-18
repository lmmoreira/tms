package br.com.logistics.tms.company.application.usecases;

import br.com.logistics.tms.commons.application.gateways.QueueGateway;
import br.com.logistics.tms.commons.application.usecases.UseCase;
import br.com.logistics.tms.commons.domain.exception.ValidationException;
import br.com.logistics.tms.company.application.repositories.CompanyRepository;
import br.com.logistics.tms.company.domain.Cnpj;
import br.com.logistics.tms.company.domain.Company;
import br.com.logistics.tms.company.domain.CompanyCreated;

public class CreateCompanyUseCase extends
    UseCase<CreateCompanyUseCase.Input, CreateCompanyUseCase.Output> {

    private final CompanyRepository companyRepository;
    private final QueueGateway queueGateway;

    public CreateCompanyUseCase(CompanyRepository companyRepository, QueueGateway queueGateway) {
        this.companyRepository = companyRepository;
        this.queueGateway = queueGateway;
    }

    public Output execute(final Input input) {
        if (companyRepository.getCompanyByCnpj(new Cnpj(input.cnpj)).isPresent()) {
            throw new ValidationException("Company already exists");
        }

        final Company company = companyRepository.create(
            Company.createCompany(input.name, input.cnpj));

        final CompanyCreated event = new CompanyCreated(company.companyId().value().toString());

        queueGateway.publish(event.router(), event.routingKey(), event);

        return new Output(company.companyId().value().toString(), company.name(),
            company.cnpj().value());
    }

    public record Input(String name, String cnpj) {

    }

    public record Output(String id, String name, String cnpj) {

    }

}
