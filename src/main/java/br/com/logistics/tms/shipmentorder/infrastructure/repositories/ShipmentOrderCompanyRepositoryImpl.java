package br.com.logistics.tms.shipmentorder.infrastructure.repositories;

import br.com.logistics.tms.shipmentorder.application.repositories.CompanyRepository;
import br.com.logistics.tms.shipmentorder.domain.Company;
import br.com.logistics.tms.shipmentorder.domain.CompanyId;
import br.com.logistics.tms.shipmentorder.infrastructure.jpa.entities.ShipmentOrderCompanyEntity;
import br.com.logistics.tms.shipmentorder.infrastructure.jpa.repositories.ShipmentOrderCompanyJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
public class ShipmentOrderCompanyRepositoryImpl implements CompanyRepository {

    private final ShipmentOrderCompanyJpaRepository shipmentOrderCompanyJpaRepository;

    @Override
    public Company save(final Company company) {
        ShipmentOrderCompanyEntity entity = ShipmentOrderCompanyEntity.of(company);
        entity = shipmentOrderCompanyJpaRepository.save(entity);
        return entity.toDomain();
    }

    @Override
    public Optional<Company> findById(final CompanyId companyId) {
        return shipmentOrderCompanyJpaRepository.findById(companyId.value())
                .map(ShipmentOrderCompanyEntity::toDomain);
    }

    @Override
    public boolean existsById(final CompanyId companyId) {
        return shipmentOrderCompanyJpaRepository.existsById(companyId.value());
    }
}
