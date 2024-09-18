package br.com.logistics.tms.commons.domain;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.UUIDUtil;
import java.util.UUID;

public record Id() {

    public static UUID unique() {
        return Generators.timeBasedEpochGenerator().generate();
    }

    public static UUID with(final String value) {
        return UUIDUtil.uuid(value);
    }

}
