package br.com.logistics.tms.company.domain.exception;

import br.com.logistics.tms.commons.domain.exception.DomainException;

public class AgreementNotFoundException extends DomainException {

    public AgreementNotFoundException(final String message) {
        super(message);
    }

    public AgreementNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
