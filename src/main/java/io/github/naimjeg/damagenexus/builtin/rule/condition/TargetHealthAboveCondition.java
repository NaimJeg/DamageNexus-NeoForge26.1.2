package io.github.naimjeg.damagenexus.builtin.rule.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleCondition;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleConditionTypes;
import net.minecraft.resources.Identifier;

public record TargetHealthAboveCondition(
        float threshold
) implements DamageRuleCondition {

    public static final MapCodec<TargetHealthAboveCondition> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT
                            .fieldOf("threshold")
                            .forGetter(TargetHealthAboveCondition::threshold)
            ).apply(instance, TargetHealthAboveCondition::new));

    @Override
    public Identifier type() {
        return DamageRuleConditionTypes.TARGET_HEALTH_ABOVE;
    }

    @Override
    public boolean test(DamageNexusContext ctx) {
        if (ctx.victim == null) {
            return false;
        }

        float max = ctx.victim.getMaxHealth();
        if (max <= 0.0f) {
            return false;
        }

        return ctx.victim.getHealth() / max > threshold;
    }
}