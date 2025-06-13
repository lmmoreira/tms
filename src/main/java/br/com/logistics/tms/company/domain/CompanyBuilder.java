package br.com.logistics.tms.company.domain;

import java.util.Set;

public class CompanyBuilder {

    private CompanyId companyId;
    private String name;
    private Cnpj cnpj;
    private Set<Type> types;
    private Configuration configuration;
    private Set<Relationship> outgoingPaths;
    private Set<Relationship> incomingPaths;

    private CompanyBuilder() {
    }

    public static CompanyBuilder builder() {
        return new CompanyBuilder();
    }

    public CompanyBuilder companyId(CompanyId companyId) {
        this.companyId = companyId;
        return this;
    }

    public CompanyBuilder name(String name) {
        this.name = name;
        return this;
    }

    public CompanyBuilder cnpj(Cnpj cnpj) {
        this.cnpj = cnpj;
        return this;
    }

    public CompanyBuilder types(Set<Type> types) {
        this.types = types;
        return this;
    }

    public CompanyBuilder configuration(Configuration configurations) {
        this.configuration = configurations;
        return this;
    }

    public CompanyBuilder outgoingPaths(Set<Relationship> outgoingPaths) {
        this.outgoingPaths = outgoingPaths;
        return this;
    }

    public CompanyBuilder incomingPaths(Set<Relationship> incomingPaths) {
        this.incomingPaths = incomingPaths;
        return this;
    }

    public Company build() {
        return new Company(companyId, name, cnpj, types, configuration, outgoingPaths,
            incomingPaths, Set.of());
    }
}
