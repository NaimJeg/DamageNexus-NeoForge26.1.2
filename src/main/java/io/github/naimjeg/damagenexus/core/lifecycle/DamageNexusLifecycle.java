package io.github.naimjeg.damagenexus.core.lifecycle;

public final class DamageNexusLifecycle {

    private static DamageNexusLifecycleState state =
            DamageNexusLifecycleState.CONSTRUCTING;

    private DamageNexusLifecycle() {
    }

    public static synchronized DamageNexusLifecycleState state() {
        return state;
    }

    public static synchronized void beginRegistering() {
        transitionTo(DamageNexusLifecycleState.REGISTERING);
    }

    public static synchronized void freezeRegistration() {
        transitionTo(DamageNexusLifecycleState.FROZEN);
    }

    public static synchronized void running() {
        transitionTo(DamageNexusLifecycleState.RUNNING);
    }

    public static synchronized void beginReloading() {
        transitionTo(DamageNexusLifecycleState.RELOADING);
    }

    public static synchronized void requireRegistering(String action) {
        if (state == DamageNexusLifecycleState.REGISTERING) {
            return;
        }

        throw new IllegalStateException(
                "DamageNexus registration is only allowed during "
                        + "DamageNexusRegisterEvent. action="
                        + action
                        + " state="
                        + state
        );
    }

    static synchronized void resetForTesting() {
        state = DamageNexusLifecycleState.CONSTRUCTING;
    }

    static synchronized void transitionTo(DamageNexusLifecycleState next) {
        state = next;
    }
}
