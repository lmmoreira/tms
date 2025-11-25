package br.com.logistics.tms.shipmentorder.application.usecases;

import br.com.logistics.tms.commons.application.annotation.Cqrs;
import br.com.logistics.tms.commons.application.annotation.DatabaseRole;
import br.com.logistics.tms.commons.application.annotation.DomainService;
import br.com.logistics.tms.commons.application.usecases.VoidUseCase;
import br.com.logistics.tms.commons.domain.Status;
import br.com.logistics.tms.shipmentorder.application.repositories.CompanyRepository;
import br.com.logistics.tms.shipmentorder.domain.Company;
import br.com.logistics.tms.shipmentorder.domain.CompanyId;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@DomainService
@Cqrs(DatabaseRole.WRITE)
public class SynchronizeCompanyUseCase implements VoidUseCase<SynchronizeCompanyUseCase.Input> {

    public static final String TYPES_KEY = "types";
    public static final String STATUS_KEY = "status";

    private final CompanyRepository companyRepository;

    public SynchronizeCompanyUseCase(final CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public void execute(final Input input) {
        if ((input.data() == null) ||
            (input.data().isEmpty()) ||
            (!input.contains(TYPES_KEY) && !input.contains(STATUS_KEY))) {
            return;
        }

        final Optional<Company> companyOptional = companyRepository.findById(CompanyId.with(input.companyId()));

        if (companyOptional.isEmpty()) {
            companyRepository.save(Company.createCompany(input.companyId(), input.getTypesKey()));
        } else {
            final Company existing = companyOptional.get();
            final Company updated;

            if (input.getStatus().isPresent()) {
                updated = existing.updateStatus(input.getStatus().get());
            } else if (existing.getStatus().isActive()) {
                updated = existing.updateData(input.getTypesKey());
            } else {
                updated = existing;
            }

            companyRepository.save(updated);
        }

    }

    public record Input(UUID companyId, Map<String, Object> data) {

        public Map<String, Object> getTypesKey() {
            if (data.get(TYPES_KEY) != null) {
                return Map.of(TYPES_KEY, data.get(TYPES_KEY));
            }

            return Map.of();
        }

        public Optional<Status> getStatus() {
            if (data.get(STATUS_KEY) != null) {
                return Optional.of(Status.of(data.get(STATUS_KEY).toString()));
            }

            return Optional.empty();
        }

        public boolean contains(final String key) {
            return (data.get(key) != null);
        }
    }
}
