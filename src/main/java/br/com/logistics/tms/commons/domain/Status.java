package br.com.logistics.tms.commons.domain;

import br.com.logistics.tms.commons.domain.exception.ValidationException;

public record Status(char value) {
    public Status {
        if (value != 'A' && value != 'S' && value != 'D') {
            throw new ValidationException("Invalid company status. Must be 'A' (Active), 'S' (Suspended), or 'D' (Deleted)");
        }
    }

    public static Status active() {
        return new Status('A');
    }

    public static Status suspended() {
        return new Status('S');
    }

    public static Status deleted() {
        return new Status('D');
    }

    public static Status of(final char value) {
        return new Status(value);
    }

    public static Status of(final String value) {
        return new Status(value.charAt(0));
    }

    public boolean isActive() {
        return value == 'A';
    }

    public boolean isSuspended() {
        return value == 'S';
    }

    public boolean isDeleted() {
        return value == 'D';
    }

    public boolean isInactive() {
        return value == 'S' || value == 'D';
    }
}
