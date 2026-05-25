package io.github.naimjeg.damagenexus.api.affix.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.affix.AffixCondition;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.affix.AffixConditionTypes;
import net.minecraft.resources.Identifier;

public record AttackerHealthBelowCondition(
        float threshold
) implements AffixCondition {

    public static final MapCodec<AttackerHealthBelowCondition> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.floatRange(0.0f, 1.0f)
                            .fieldOf("threshold")
                            .forGetter(AttackerHealthBelowCondition::threshold)
            ).apply(instance, AttackerHealthBelowCondition::new));

    @Override
    public Identifier type() {
        return AffixConditionTypes.ATTACKER_HEALTH_BELOW;
    }

    @Override
    public boolean test(DamageNexusContext ctx) {
        if (ctx.attacker == null) {
            ctx.debugger.logOperation(
                    "condition:attacker_health_below",
                    ctx.getCurrentProcessingPhase(),
                    "NO_ATTACKER",
                    0.0f
            );
            return false;
        }

        float maxHealth = ctx.attacker.getMaxHealth();

        if (maxHealth <= 0.0f) {
            return false;
        }

        float ratio = ctx.attacker.getHealth() / maxHealth;

        ctx.debugger.logOperation(
                "condition:attacker_health_below",
                ctx.getCurrentProcessingPhase(),
                "HEALTH_RATIO",
                ratio
        );

        return ratio < threshold;
    }
}