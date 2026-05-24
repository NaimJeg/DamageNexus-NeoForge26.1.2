package io.github.naimjeg.damagenexus.api.rule.affix;

import net.minecraft.resources.Identifier;

/**
 * Concrete numeric value produced from a DamageAffixRollRange.
 */
public record DamageAffixRolledValue(
        Identifier key,
        float value,
        float minValue,
        float maxValue
) {
    public DamageAffixRolledValue {
        if (key == null) {
            throw new IllegalArgumentException("Damage affix rolled value key cannot be null");
        }

        if (!Float.isFinite(value)) {
            value = 0.0f;
        }

        if (!Float.isFinite(minValue)) {
            minValue = value;
        }

        if (!Float.isFinite(maxValue)) {
            maxValue = value;
        }
    }
}
