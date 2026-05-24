package io.github.naimjeg.damagenexus.api.rule.affix;

import io.github.naimjeg.damagenexus.api.rule.DamageRuleDefinition;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * One rollable tier inside a generated-affix blueprint.
 */
public record DamageAffixTier(
        Identifier id,
        int minItemLevel,
        int maxItemLevel,
        int weight,
        List<DamageAffixRollRange> rolls,
        List<DamageRuleDefinition> rules
) {
    public DamageAffixTier {
        if (id == null) {
            throw new IllegalArgumentException("Damage affix tier id cannot be null");
        }

        minItemLevel = Math.max(0, minItemLevel);
        maxItemLevel = Math.max(minItemLevel, maxItemLevel);
        weight = Math.max(0, weight);
        rolls = rolls == null ? List.of() : List.copyOf(rolls);
        rules = rules == null ? List.of() : List.copyOf(rules);
    }
}
