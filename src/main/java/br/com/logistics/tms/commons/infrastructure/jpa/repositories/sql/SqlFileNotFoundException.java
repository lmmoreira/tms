package br.com.logistics.tms.commons.infrastructure.jpa.repositories.sql;

public class SqlFileNotFoundException extends RuntimeException {

    public SqlFileNotFoundException(String message) {
        super(message, null, true, false);
    }

    public SqlFileNotFoundException(String message, Throwable cause) {
        super(message, cause, true, false);
    }

}
