package br.com.logistics.tms.company.domain.exception;

import br.com.logistics.tms.commons.domain.exception.DomainException;

public class CompanyNotFoundException extends DomainException {

    public CompanyNotFoundException(String message) {
        super(message);
    }

    public CompanyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
