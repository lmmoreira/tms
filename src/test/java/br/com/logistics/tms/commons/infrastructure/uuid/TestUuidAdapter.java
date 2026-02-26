package br.com.logistics.tms.commons.infrastructure.uuid;

import br.com.logistics.tms.commons.domain.id.UuidAdapter;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.UUIDUtil;

import java.util.UUID;

/**
 * Test implementation of UuidAdapter for use in all test types (unit, integration, e2e).
 * Uses UUID v7 (time-based) generation for deterministic ordering in tests.
 */
public class TestUuidAdapter implements UuidAdapter {

    @Override
    public UUID generate() {
        return Generators.timeBasedEpochGenerator().generate();
    }

    @Override
    public UUID fromString(final String value) {
        return UUIDUtil.uuid(value);
    }
}
