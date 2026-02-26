package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.AbstractAggregateRoot;
import br.com.logistics.tms.commons.domain.AbstractDomainEvent;
import br.com.logistics.tms.commons.domain.Status;
import br.com.logistics.tms.commons.domain.exception.ValidationException;

import java.time.Instant;
import java.util.*;

public class Company extends AbstractAggregateRoot {

    private final CompanyId companyId;
    private final String name;
    private final Cnpj cnpj;
    private final CompanyTypes companyTypes;
    private final Configurations configurations;
    private final Set<Agreement> agreements;
    private final Status status;

    public Company(final CompanyId companyId,
                   final String name,
                   final Cnpj cnpj,
                   final CompanyTypes companyTypes,
                   final Configurations configurations,
                   final Set<Agreement> agreements,
                   final Status status,
                   final Set<AbstractDomainEvent> domainEvents,
                   final Map<String, Object> persistentMetadata) {
        super(new HashSet<>(domainEvents), new HashMap<>(persistentMetadata));

        if (companyId == null) throw new ValidationException("Invalid companyId for Company");
        if (name == null || name.isBlank()) throw new ValidationException("Invalid name for Company");
        if (cnpj == null) throw new ValidationException("Invalid cnpj for Company");
        if (companyTypes == null) throw new ValidationException("Invalid type for Company");
        if (configurations == null) throw new ValidationException("Invalid configuration for Company");
        if (agreements == null) throw new ValidationException("Invalid agreements for Company");
        if (status == null) throw new ValidationException("Invalid status for Company");

        this.companyId = companyId;
        this.name = name;
        this.cnpj = cnpj;
        this.companyTypes = companyTypes;
        this.configurations = configurations;
        this.agreements = agreements;
        this.status = status;
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
                Status.active(),
                new HashSet<>(),
                new HashMap<>());
        company.placeDomainEvent(new CompanyCreated(company.companyId.value(), company.name, company.companyTypes.getTypeNames()));
        return company;
    }

    public Company updateName(final String name) {
        validateCanUpdate();
        if (this.name.equals(name))
            return this;

        final Company updated = new Company(
                this.companyId,
                name,
                this.cnpj,
                this.companyTypes,
                this.configurations,
                this.agreements,
                this.status,
                this.getDomainEvents(),
                this.getPersistentMetadata()
        );

        updated.placeDomainEvent(new CompanyUpdated(updated.companyId.value(), "name", this.name, name));
        return updated;
    }

    public Company updateCnpj(final String cnpj) {
        validateCanUpdate();
        if (this.cnpj.value().equals(cnpj))
            return this;

        final Company updated = new Company(
                this.companyId,
                this.name,
                new Cnpj(cnpj),
                this.companyTypes,
                this.configurations,
                this.agreements,
                this.status,
                this.getDomainEvents(),
                this.getPersistentMetadata()
        );

        updated.placeDomainEvent(new CompanyUpdated(updated.companyId.value(), "cnpj", this.cnpj.value(), cnpj));
        return updated;
    }

    public Company updateTypes(final Set<CompanyType> types) {
        validateCanUpdate();
        if (this.companyTypes.equals(CompanyTypes.with(types)))
            return this;

        final Company updated = new Company(
                this.companyId,
                this.name,
                this.cnpj,
                CompanyTypes.with(types),
                this.configurations,
                this.agreements,
                this.status,
                this.getDomainEvents(),
                this.getPersistentMetadata()
        );

        updated.placeDomainEvent(new CompanyUpdated(updated.companyId.value(), "types", this.companyTypes.value().toString(), types.toString()));
        return updated;
    }

    public Company updateConfigurations(final Map<String, Object> configurations) {
        validateCanUpdate();
        if (this.configurations.equals(Configurations.with(configurations)))
            return this;

        final Company updated = new Company(
                this.companyId,
                this.name,
                this.cnpj,
                this.companyTypes,
                Configurations.with(configurations),
                this.agreements,
                this.status,
                this.getDomainEvents(),
                this.getPersistentMetadata()
        );

        updated.placeDomainEvent(new CompanyUpdated(updated.companyId.value(), "configurations", this.configurations.value().toString(), configurations.toString()));
        return updated;
    }

    public Company incrementOrderNumber() {
        final Map<String, Object> configuration = new HashMap<>(this.configurations.value());
        configuration.putIfAbsent("shipmentOrderNumber", 0);
        configuration.put("shipmentOrderNumber", (Integer) configuration.get("shipmentOrderNumber") + 1);
        return this.updateConfigurations(configuration);
    }

    public Company suspend() {
        if (this.status.isSuspended() || this.status.isDeleted()) {
            return this;
        }

        return this.updateStatus(Status.suspended());
    }

    public Company delete() {
        if (this.status.isDeleted()) {
            return this;
        }

        return this.updateStatus(Status.deleted());
    }

    public Company updateStatus(final Status newStatus) {
        if (this.status.equals(newStatus)) {
            return this;
        }

        final Company updated = new Company(
                this.companyId,
                this.name,
                this.cnpj,
                this.companyTypes,
                this.configurations,
                this.agreements,
                newStatus,
                this.getDomainEvents(),
                this.getPersistentMetadata()
        );

        updated.placeDomainEvent(new CompanyUpdated(updated.companyId.value(), "status", String.valueOf(this.status.value()), String.valueOf(newStatus.value())));
        return updated;
    }

    private void validateCanUpdate() {
        if (!this.status.isActive()) {
            throw new ValidationException(
                    String.format("Cannot update company in %s status", 
                            this.status.isDeleted() ? "DELETED" : "SUSPENDED")
            );
        }
    }

    public Set<Agreement> getAgreements() {
        return Collections.unmodifiableSet(agreements);
    }

    public Company addAgreement(final Agreement agreement) {
        if (agreements.contains(agreement)) {
            throw new ValidationException("Agreement already exists for this company");
        }

        if (!agreement.from().equals(this.companyId)) {
            throw new ValidationException("Agreement source must match company");
        }

        final boolean overlappingExists = agreements.stream()
                .filter(Agreement::isActive)
                .anyMatch(a -> a.overlapsWith(agreement));
        if (overlappingExists) {
            throw new ValidationException("Overlapping active agreement already exists");
        }

        final Set<Agreement> updatedAgreements = new HashSet<>(this.agreements);
        updatedAgreements.add(agreement);

        final Company updated = new Company(
                this.companyId,
                this.name,
                this.cnpj,
                this.companyTypes,
                this.configurations,
                updatedAgreements,
                this.status,
                this.getDomainEvents(),
                this.getPersistentMetadata()
        );

        updated.placeDomainEvent(new AgreementAdded(
                this.companyId.value(),
                agreement.agreementId().value(),
                agreement.to().value(),
                agreement.type().name()
        ));

        return updated;
    }

    public Company removeAgreement(final AgreementId agreementId) {
        final Agreement agreementToRemove = agreements.stream()
                .filter(a -> a.agreementId().equals(agreementId))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Agreement not found"));

        final Set<Agreement> updatedAgreements = new HashSet<>(this.agreements);
        updatedAgreements.remove(agreementToRemove);

        final Company updated = new Company(
                this.companyId,
                this.name,
                this.cnpj,
                this.companyTypes,
                this.configurations,
                updatedAgreements,
                this.status,
                this.getDomainEvents(),
                this.getPersistentMetadata()
        );

        updated.placeDomainEvent(new AgreementRemoved(
                this.companyId.value(),
                agreementToRemove.agreementId().value(),
                agreementToRemove.to().value()
        ));

        return updated;
    }

    public Company updateAgreement(final AgreementId agreementId, final Agreement updatedAgreement) {
        final Agreement existingAgreement = agreements.stream()
                .filter(a -> a.agreementId().equals(agreementId))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Agreement not found"));

        final boolean overlappingExists = agreements.stream()
                .filter(a -> !a.agreementId().equals(agreementId))
                .filter(Agreement::isActive)
                .anyMatch(a -> a.overlapsWith(updatedAgreement));
        if (overlappingExists) {
            throw new ValidationException("Update would create overlapping agreement");
        }

        final Set<Agreement> updatedAgreements = new HashSet<>(this.agreements);
        updatedAgreements.remove(existingAgreement);
        updatedAgreements.add(updatedAgreement);

        final Company updated = new Company(
                this.companyId,
                this.name,
                this.cnpj,
                this.companyTypes,
                this.configurations,
                updatedAgreements,
                this.status,
                this.getDomainEvents(),
                this.getPersistentMetadata()
        );

        String fieldChanged = "unknown";
        String oldValue = "";
        String newValue = "";

        final Instant existingValidTo = existingAgreement.validTo();
        final Instant updatedValidTo = updatedAgreement.validTo();
        
        if ((existingValidTo == null && updatedValidTo != null) || 
            (existingValidTo != null && !existingValidTo.equals(updatedValidTo))) {
            fieldChanged = "validTo";
            oldValue = existingValidTo != null ? existingValidTo.toString() : "null";
            newValue = updatedValidTo != null ? updatedValidTo.toString() : "null";
        } else if (!existingAgreement.conditions().equals(updatedAgreement.conditions())) {
            fieldChanged = "conditions";
            oldValue = String.valueOf(existingAgreement.conditions().size());
            newValue = String.valueOf(updatedAgreement.conditions().size());
        }

        updated.placeDomainEvent(new AgreementUpdated(
                this.companyId.value(),
                agreementId.value(),
                fieldChanged,
                oldValue,
                newValue
        ));

        return updated;
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

    public Status getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return Map.of(
                "companyId", companyId,
                "name", name,
                "cnpj", cnpj,
                "companyTypes", companyTypes,
                "configurations", configurations,
                "status", status,
                "agreements", agreements
        ).toString();
    }
}
