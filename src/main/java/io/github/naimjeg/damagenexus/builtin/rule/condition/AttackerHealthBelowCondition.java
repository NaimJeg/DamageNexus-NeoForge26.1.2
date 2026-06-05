package io.github.naimjeg.damagenexus.builtin.rule.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.context.DamageRuleContext;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleCondition;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleConditionTypes;
import net.minecraft.resources.Identifier;

public record AttackerHealthBelowCondition(
        float threshold
) implements DamageRuleCondition {

    public static final MapCodec<AttackerHealthBelowCondition> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.floatRange(0.0f, 1.0f)
                            .fieldOf("threshold")
                            .forGetter(AttackerHealthBelowCondition::threshold)
            ).apply(instance, AttackerHealthBelowCondition::new));

    @Override
    public Identifier type() {
        return DamageRuleConditionTypes.ATTACKER_HEALTH_BELOW;
    }

    @Override
    public boolean test(DamageRuleContext ctx) {
        if (ctx.attacker() == null) {
            return false;
        }

        float maxHealth = ctx.attacker().getMaxHealth();

        if (maxHealth <= 0.0f) {
            return false;
        }

        float ratio = ctx.attacker().getHealth() / maxHealth;
        return ratio < threshold;
    }
}
