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

    private static final String TYPES_KEY = "types";

    private final CompanyRepository companyRepository;

    public SynchronizeCompanyUseCase(final CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public void execute(final Input input) {
        if (input.data() == null || !input.data().containsKey(TYPES_KEY)) {
            return;
        }

        final Map<String, Object> data = Map.of(TYPES_KEY, input.data().get(TYPES_KEY));
        companyRepository.save(companyRepository.findById(CompanyId.with(input.companyId()))
                .map(existing -> existing.updateData(data))
                .orElseGet(() -> Company.createCompany(input.companyId(), data)));
    }

    public record Input(UUID companyId, Map<String, Object> data) {}

}
