package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.Status;
import br.com.logistics.tms.commons.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CompanyStatusTest {

    @Test
    void shouldCreateActiveStatus() {
        final Status status = Status.active();
        
        assertThat(status.value()).isEqualTo('A');
        assertThat(status.isActive()).isTrue();
        assertThat(status.isSuspended()).isFalse();
        assertThat(status.isDeleted()).isFalse();
        assertThat(status.isInactive()).isFalse();
    }

    @Test
    void shouldCreateSuspendedStatus() {
        final Status status = Status.suspended();
        
        assertThat(status.value()).isEqualTo('S');
        assertThat(status.isActive()).isFalse();
        assertThat(status.isSuspended()).isTrue();
        assertThat(status.isDeleted()).isFalse();
        assertThat(status.isInactive()).isTrue();
    }

    @Test
    void shouldCreateDeletedStatus() {
        final Status status = Status.deleted();
        
        assertThat(status.value()).isEqualTo('D');
        assertThat(status.isActive()).isFalse();
        assertThat(status.isSuspended()).isFalse();
        assertThat(status.isDeleted()).isTrue();
        assertThat(status.isInactive()).isTrue();
    }

    @Test
    void shouldCreateStatusFromChar() {
        assertThat(Status.of('A').isActive()).isTrue();
        assertThat(Status.of('S').isSuspended()).isTrue();
        assertThat(Status.of('D').isDeleted()).isTrue();
    }

    @Test
    void shouldCreateStatusFromString() {
        assertThat(Status.of("A").isActive()).isTrue();
        assertThat(Status.of("S").isSuspended()).isTrue();
        assertThat(Status.of("D").isDeleted()).isTrue();
    }

    @Test
    void shouldReturnActiveAsDefaultStatus() {
        final Status status = Status.active();
        
        assertThat(status.isActive()).isTrue();
    }

    @Test
    void shouldThrowValidationExceptionForInvalidStatus() {
        assertThatThrownBy(() -> Status.of('X'))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid company status");
    }

    @Test
    void shouldIdentifyInactiveStatusesProperly() {
        assertThat(Status.active().isInactive()).isFalse();
        assertThat(Status.suspended().isInactive()).isTrue();
        assertThat(Status.deleted().isInactive()).isTrue();
    }
}
