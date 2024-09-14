package br.com.logistics.tms.commons.domain;

import java.util.UUID;

public record Id() {

    public static UUID unique() {
        return UUID.randomUUID();
    }

    public static UUID with(final String value) {
        return UUID.fromString(value);
    }

}
