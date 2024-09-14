package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.exception.ValidationException;
import java.util.Set;

public record Company(CompanyId companyId,
                      String name,
                      Set<Configuration> configurations,
                      Set<Company> parents,
                      Set<Company> children) {

    public Company {
        if (companyId == null) {
            throw new ValidationException("Invalid companyId for Company");
        }
    }

    public static Company newCompany(String name,
        Set<Configuration> configurations,
        Set<Company> parents,
        Set<Company> children) {
        return new Company(null, name, configurations, parents, children);
    }

}
