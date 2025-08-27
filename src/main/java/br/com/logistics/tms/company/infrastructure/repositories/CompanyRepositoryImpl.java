package br.com.logistics.tms.company.infrastructure.repositories;

import br.com.logistics.tms.commons.infrastructure.gateways.outbox.OutboxGateway;
import br.com.logistics.tms.company.application.repositories.CompanyRepository;
import br.com.logistics.tms.company.domain.Cnpj;
import br.com.logistics.tms.company.domain.Company;
import br.com.logistics.tms.company.domain.CompanyId;
import br.com.logistics.tms.company.infrastructure.config.CompanySchema;
import br.com.logistics.tms.company.infrastructure.jpa.entities.CompanyEntity;
import br.com.logistics.tms.company.infrastructure.jpa.entities.CompanyOutboxEntity;
import br.com.logistics.tms.company.infrastructure.jpa.repositories.CompanyJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class CompanyRepositoryImpl implements CompanyRepository {

    private final CompanyJpaRepository companyJpaRepository;
    private final OutboxGateway outboxGateway;

    @Override
    public Optional<Company> getCompanyById(CompanyId id) {
        return companyJpaRepository.findById(id.value())
                .map(CompanyEntity::toCompany);
    }

    @Override
    public Optional<Company> getCompanyByCnpj(Cnpj cnpj) {
        return companyJpaRepository.findByCnpj(cnpj.value())
                .map(CompanyEntity::toCompany);
    }

    @Override
    public Company create(final Company company) {
        final CompanyEntity companyEntity = CompanyEntity.of(company);
        companyJpaRepository.save(companyEntity);
        outboxGateway.save(CompanySchema.COMPANY_SCHEMA, company.getDomainEvents(), CompanyOutboxEntity.class);
        return company;
    }

    @Override
    public Company update(Company company) {
        final CompanyEntity companyEntity = CompanyEntity.of(company);
        companyJpaRepository.save(companyEntity);
        outboxGateway.save(CompanySchema.COMPANY_SCHEMA, company.getDomainEvents(), CompanyOutboxEntity.class);
        return company;
    }

    @Override
    public void delete(Company company) {
        companyJpaRepository.deleteById(company.getCompanyId().value());
        outboxGateway.save(CompanySchema.COMPANY_SCHEMA, company.getDomainEvents(), CompanyOutboxEntity.class);
    }

}


