package br.com.logistics.tms.shipmentorder.application.usecases;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.DatabaseRole;
import br.com.logistics.tms.commons.application.annotation.DomainService;
import br.com.logistics.tms.commons.application.usecases.VoidUseCase;
import br.com.logistics.tms.commons.domain.Status;
import br.com.logistics.tms.shipmentorder.application.repositories.CompanyRepository;
import br.com.logistics.tms.shipmentorder.domain.Company;
import br.com.logistics.tms.shipmentorder.domain.CompanyId;

import java.util.UUID;

@DomainService
@Cqrs(DatabaseRole.WRITE)
public class UpdateCompanyStatusUseCase implements VoidUseCase<UpdateCompanyStatusUseCase.Input> {

    private final CompanyRepository companyRepository;

    public UpdateCompanyStatusUseCase(final CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public void execute(final Input input) {
        companyRepository.findById(CompanyId.with(input.companyId()))
                .ifPresent(existing -> {
                    final Company updated = existing.updateStatus(Status.of(input.status()));
                    companyRepository.save(updated);
                });
    }

    public record Input(UUID companyId, char status) {}
}
