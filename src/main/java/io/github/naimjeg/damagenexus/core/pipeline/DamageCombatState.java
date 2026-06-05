package io.github.naimjeg.damagenexus.core.pipeline;

/**
 * Mutable per-transaction combat flags that are not part of the damage packet
 * math, source classification, event writeback, or diagnostics lifecycle.
 */
final class DamageCombatState {

    private boolean critical = false;
    private boolean armorHandled = false;
    private float armorEffectivenessMultiplier = 1.0f;

    void markCritical() {
        this.critical = true;
    }

    boolean critical() {
        return critical;
    }

    void markArmorHandled() {
        this.armorHandled = true;
    }

    boolean armorHandled() {
        return armorHandled;
    }

    float armorEffectivenessMultiplier() {
        return armorEffectivenessMultiplier;
    }

    void multiplyArmorEffectiveness(float multiplier) {
        float safeMultiplier = Float.isFinite(multiplier)
                ? Math.max(0.0f, multiplier)
                : 0.0f;

        armorEffectivenessMultiplier *= safeMultiplier;

        if (!Float.isFinite(armorEffectivenessMultiplier)) {
            armorEffectivenessMultiplier = 0.0f;
        }

        armorEffectivenessMultiplier =
                Math.max(0.0f, armorEffectivenessMultiplier);
    }
}

