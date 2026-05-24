package io.github.naimjeg.damagenexus.core.pipeline;

public final class DamagePipelineResult {

    private float offensiveTotal = 0.0f;
    private float finalEventDamage = 0.0f;

    private boolean damageCancelled = false;
    private String cancelSourceId = null;

    public float offensiveTotal() {
        return offensiveTotal;
    }

    public float finalEventDamage() {
        return finalEventDamage;
    }

    public boolean damageCancelled() {
        return damageCancelled;
    }

    public String cancelSourceId() {
        return cancelSourceId;
    }

    void setOffensiveTotal(float amount) {
        this.offensiveTotal = sanitize(amount);
    }

    void setFinalEventDamage(float amount) {
        this.finalEventDamage = sanitize(amount);
    }

    void cancel(String sourceId) {
        this.damageCancelled = true;
        this.cancelSourceId = sourceId;
        this.offensiveTotal = 0.0f;
        this.finalEventDamage = 0.0f;
    }

    private static float sanitize(float value) {
        if (!Float.isFinite(value)) {
            return 0.0f;
        }

        return Math.max(0.0f, value);
    }
}