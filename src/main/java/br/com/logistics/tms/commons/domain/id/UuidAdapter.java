package br.com.logistics.tms.commons.domain.id;

import java.util.UUID;

public interface UuidAdapter {

    UUID generate();

    UUID fromString(String value);

}
