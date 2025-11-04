package br.com.logistics.tms.company.application.usecases;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.DatabaseRole;
import br.com.logistics.tms.commons.application.annotation.DomainService;
import br.com.logistics.tms.commons.application.usecases.VoidUseCase;
import br.com.logistics.tms.commons.domain.exception.ValidationException;
import br.com.logistics.tms.company.application.repositories.CompanyRepository;
import br.com.logistics.tms.company.domain.Company;
import br.com.logistics.tms.company.domain.CompanyId;

import java.util.Optional;
import java.util.UUID;

@DomainService
@Cqrs(DatabaseRole.WRITE)
public class IncrementShipmentOrderUseCase implements VoidUseCase<IncrementShipmentOrderUseCase.Input> {

    private final CompanyRepository companyRepository;

    public IncrementShipmentOrderUseCase(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public void execute(final Input input) {
        final Optional<Company> existingCompany = companyRepository.getCompanyById(CompanyId.with(input.companyId));

        if (existingCompany.isEmpty()) {
            throw new ValidationException("Company not found");
        }

        final Company incrementedCompany = existingCompany.get().incrementOrderNumber();
        companyRepository.update(incrementedCompany);
    }

    public record Input(UUID companyId) {
    }

}
