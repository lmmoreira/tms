package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.AbstractAggregateRoot;
import br.com.logistics.tms.commons.domain.AbstractDomainEvent;
import br.com.logistics.tms.commons.domain.exception.ValidationException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Company extends AbstractAggregateRoot {

    private final CompanyId companyId;
    private final String name;
    private final Cnpj cnpj;
    private final CompanyTypes companyTypes;
    private final Configurations configurations;
    private final Set<Agreement> agreements;

    public Company(final CompanyId companyId,
                   final String name,
                   final Cnpj cnpj,
                   final CompanyTypes companyTypes,
                   final Configurations configurations,
                   final Set<Agreement> agreements,
                   final Set<AbstractDomainEvent> domainEvents) {
        super(new HashSet<>(domainEvents));

        if (companyId == null) throw new ValidationException("Invalid companyId for Company");
        if (name == null || name.isBlank()) throw new ValidationException("Invalid name for Company");
        if (cnpj == null) throw new ValidationException("Invalid cnpj for Company");
        if (companyTypes == null) throw new ValidationException("Invalid type for Company");
        if (configurations == null) throw new ValidationException("Invalid configuration for Company");
        if (agreements == null) throw new ValidationException("Invalid agreements for Company");

        this.companyId = companyId;
        this.name = name;
        this.cnpj = cnpj;
        this.companyTypes = companyTypes;
        this.configurations = configurations;
        this.agreements = agreements;
    }

    public static Company createCompany(final String name,
                                        final String cnpj,
                                        final Set<CompanyType> types,
                                        final Map<String, Object> configuration) {
        final Company company = new Company(CompanyId.unique(),
                name,
                new Cnpj(cnpj),
                CompanyTypes.with(types),
                Configurations.with(configuration),
                new HashSet<>(),
                new HashSet<>());
        company.placeDomainEvent(new CompanyCreated(company.companyId.value().toString(), company.toString()));
        return company;
    }

    public Company updateName(final String name) {
        if (this.name.equals(name))
            return this;

        final Company updated = new Company(
                this.companyId,
                name,
                this.cnpj,
                this.companyTypes,
                this.configurations,
                this.agreements,
                this.getDomainEvents()
        );
        updated.placeDomainEvent(new CompanyUpdated(updated.companyId.value().toString(), "name", this.name, name));
        return updated;
    }

    public Company updateCnpj(final String cnpj) {
        if (this.cnpj.value().equals(cnpj))
            return this;

        final Company updated = new Company(
                this.companyId,
                this.name,
                new Cnpj(cnpj),
                this.companyTypes,
                this.configurations,
                this.agreements,
                this.getDomainEvents()
        );
        updated.placeDomainEvent(new CompanyUpdated(updated.companyId.value().toString(), "cnpj", this.cnpj.value(), cnpj));
        return updated;
    }

    public Company updateTypes(final Set<CompanyType> types) {
        if (this.companyTypes.equals(CompanyTypes.with(types)))
            return this;

        final Company updated = new Company(
                this.companyId,
                this.name,
                this.cnpj,
                CompanyTypes.with(types),
                this.configurations,
                this.agreements,
                this.getDomainEvents()
        );
        updated.placeDomainEvent(new CompanyUpdated(updated.companyId.value().toString(), "types", this.companyTypes.value().toString(), types.toString()));
        return updated;
    }

    public Company updateConfigurations(final Map<String, Object> configurations) {
        if (this.configurations.equals(Configurations.with(configurations)))
            return this;

        final Company updated = new Company(
                this.companyId,
                this.name,
                this.cnpj,
                this.companyTypes,
                Configurations.with(configurations),
                this.agreements,
                this.getDomainEvents()
        );

        updated.placeDomainEvent(new CompanyUpdated(updated.companyId.value().toString(), "configurations", this.configurations.value().toString(), configurations.toString()));
        return updated;
    }

    public Set<Agreement> getAgreements() {
        return Collections.unmodifiableSet(agreements);
    }

    public void addAgreement(final Agreement agreement) {
        if (agreements.contains(agreement)) {
            throw new ValidationException("Agreement already exists for this company");
        }

        agreements.add(agreement);
    }

    public void removeAgreement(final Agreement agreement) {
        if (!agreements.contains(agreement)) {
            throw new ValidationException("Agreement not found for this company");
        }

        agreements.remove(agreement);
    }

    public boolean hasAgreementWith(final CompanyId other, final AgreementType type) {
        return agreements.stream()
                .anyMatch(a -> a.to().equals(other) && a.type().equals(type));
    }

    public CompanyId getCompanyId() {
        return companyId;
    }

    public String getName() {
        return name;
    }

    public Cnpj getCnpj() {
        return cnpj;
    }

    public CompanyTypes getCompanyTypes() {
        return companyTypes;
    }

    public Configurations getConfigurations() {
        return configurations;
    }

    @Override
    public String toString() {
        return "Company{" +
                "companyId=" + companyId +
                ", name='" + name + '\'' +
                ", cnpj=" + cnpj +
                ", companyTypes=" + companyTypes +
                ", configurations=" + configurations +
                ", agreements=" + agreements +
                '}';
    }
}
