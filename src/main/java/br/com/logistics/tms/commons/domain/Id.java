package br.com.logistics.tms.commons.domain;

import br.com.logistics.tms.commons.domain.id.DomainUuidProvider;

import java.util.UUID;

public record Id() {

    public static UUID unique() {
        return DomainUuidProvider.getUuidAdapter().generate();
    }

    public static UUID with(final String value) {
        return DomainUuidProvider.getUuidAdapter().fromString(value);
    }

}
