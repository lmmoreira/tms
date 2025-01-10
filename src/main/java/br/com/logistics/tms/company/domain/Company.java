package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.exception.ValidationException;
import java.util.Set;

public record Company(CompanyId companyId,
                      String name,
                      Cnpj cnpj,
                      Set<Type> types,
                      Configuration configuration,
                      Set<Relationship> outgoingPaths,
                      Set<Relationship> incomingPaths) {

    public Company {
        if (companyId == null) {
            throw new ValidationException("Invalid companyId for Company");
        }

        if ((name == null) || (name.isBlank())) {
            throw new ValidationException("Invalid name for Company");
        }

        if (cnpj == null) {
            throw new ValidationException("Invalid cnpj for Company");
        }

        if ((types == null) || (types.isEmpty())) {
            throw new ValidationException("Invalid type for Company");
        }
    }

    public static Company createCompany(final String name, final String cnpj, final Set<Type> types) {
        return new Company(CompanyId.unique(), name, new Cnpj(cnpj), types, null, null,
            null);
    }

    public void addRelationship(Company to, Company source, Configuration configuration) {
        final Relationship path = new Relationship(this, to, source, configuration);
        this.outgoingPaths.add(path);
        to.incomingPaths.add(path);
    }

}
