package br.com.logistics.tms.shipmentorder.application.usecases;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.DatabaseRole;
import br.com.logistics.tms.commons.application.annotation.DomainService;
import br.com.logistics.tms.commons.application.usecases.UseCase;
import br.com.logistics.tms.commons.application.usecases.VoidUseCase;
import br.com.logistics.tms.shipmentorder.application.repositories.CompanyRepository;
import br.com.logistics.tms.shipmentorder.domain.Company;
import br.com.logistics.tms.shipmentorder.domain.CompanyId;

import java.util.Map;
import java.util.UUID;

@DomainService
@Cqrs(DatabaseRole.WRITE)
public class SynchronizeCompanyUseCase implements VoidUseCase<SynchronizeCompanyUseCase.Input> {

    private final CompanyRepository companyRepository;

    public SynchronizeCompanyUseCase(final CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public void execute(final Input input) {
        if (input.data() == null || !input.data().containsKey("types")) {
            return;
        }

        final Map<String, Object> data = Map.of("types", input.data().get("types"));
        companyRepository.save(companyRepository.findById(CompanyId.with(input.companyId()))
                .map(existing -> existing.updateData(data))
                .orElseGet(() -> Company.createCompany(input.companyId(), data)));
    }

    public record Input(UUID companyId, Map<String, Object> data) {}

}
