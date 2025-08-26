package br.com.logistics.tms.company.infrastructure.repositories;

import br.com.logistics.tms.company.application.repositories.CompanyRepository;
import br.com.logistics.tms.company.domain.Cnpj;
import br.com.logistics.tms.company.domain.Company;
import br.com.logistics.tms.company.domain.CompanyId;
import br.com.logistics.tms.company.infrastructure.jpa.entities.CompanyEntity;
import br.com.logistics.tms.company.infrastructure.jpa.entities.CompanyOutboxEntity;
import br.com.logistics.tms.company.infrastructure.jpa.repositories.CompanyJpaRepository;
import br.com.logistics.tms.company.infrastructure.jpa.repositories.CompanyOutboxJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class CompanyRepositoryImpl implements CompanyRepository {

    private final CompanyJpaRepository companyJpaRepository;
    private final CompanyOutboxJpaRepository companyOutboxJpaRepository;

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
        companyOutboxJpaRepository.saveAll(CompanyOutboxEntity.of(company.getDomainEvents()));
        return company;
    }

    @Override
    public Company update(Company company) {
        final CompanyEntity companyEntity = CompanyEntity.of(company);
        companyJpaRepository.save(companyEntity);
        companyOutboxJpaRepository.saveAll(CompanyOutboxEntity.of(company.getDomainEvents()));
        return company;
    }

    @Override
    public void delete(Company company) {
        companyJpaRepository.deleteById(company.getCompanyId().value());
        companyOutboxJpaRepository.saveAll(CompanyOutboxEntity.of(company.getDomainEvents()));
    }

}


