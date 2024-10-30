package br.com.logistics.tms.commons.domain.exception;

public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message, null, true, false);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause, true, false);
    }

}
