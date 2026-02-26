package br.com.logistics.tms.commons.domain;

import br.com.logistics.tms.AbstractTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the UUID adapter initialization works correctly in all test types.
 */
class IdTest extends AbstractTestBase {

    @Test
    @DisplayName("Should generate unique UUIDs via Id.unique()")
    void shouldGenerateUniqueUuids() {
        final var id1 = Id.unique();
        final var id2 = Id.unique();
        final var id3 = Id.unique();

        assertThat(id1).isNotNull();
        assertThat(id2).isNotNull();
        assertThat(id3).isNotNull();
        
        assertThat(id1).isNotEqualTo(id2);
        assertThat(id2).isNotEqualTo(id3);
        assertThat(id1).isNotEqualTo(id3);
    }

    @Test
    @DisplayName("Should generate time-based UUIDs (version 7)")
    void shouldGenerateTimeBasedUuids() throws InterruptedException {
        final var id1 = Id.unique();
        Thread.sleep(2);
        final var id2 = Id.unique();

        // UUID v7 is time-based, so later IDs should be greater
        assertThat(id1.compareTo(id2)).isLessThan(0);
    }
}
