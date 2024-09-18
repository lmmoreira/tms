package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.exception.ValidationException;
import java.util.Set;

public record Company(CompanyId companyId,
                      String name,
                      Cnpj cnpj,
                      Set<Configuration> configurations,
                      Set<Company> parents,
                      Set<Company> children) {

    public Company {
        if (companyId == null) {
            throw new ValidationException("Invalid companyId for Company");
        }
    }

    public Company(CompanyId unique, String name, Cnpj cnpj) {
        this(unique, name, cnpj, Set.of(), Set.of(), Set.of());
    }

    public static Company createCompany(final String name, final String cnpj) {
        return new Company(CompanyId.unique(), name, new Cnpj(cnpj));
    }

}
