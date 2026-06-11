package io.github.naimjeg.damagenexus.core.lifecycle;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DamageNexusLifecycleTest {

    @AfterEach
    void resetLifecycle() {
        DamageNexusLifecycle.resetForTesting();
    }

    @Test
    void registrationGuardRejectsBeforeRegisterEventWindow() {
        assertThrows(
                IllegalStateException.class,
                () -> DamageNexusLifecycle.requireRegistering("test")
        );
    }

    @Test
    void registrationGuardAllowsRegisterEventWindow() {
        DamageNexusLifecycle.beginRegistering();

        assertDoesNotThrow(
                () -> DamageNexusLifecycle.requireRegistering("test")
        );
        assertEquals(
                DamageNexusLifecycleState.REGISTERING,
                DamageNexusLifecycle.state()
        );
    }

    @Test
    void registrationGuardRejectsAfterFreeze() {
        DamageNexusLifecycle.beginRegistering();
        DamageNexusLifecycle.freezeRegistration();

        assertThrows(
                IllegalStateException.class,
                () -> DamageNexusLifecycle.requireRegistering("test")
        );
    }
}
