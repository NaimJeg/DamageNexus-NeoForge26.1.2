package io.github.naimjeg.damagenexus.builtin.rule.condition;

import com.mojang.serialization.MapCodec;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleCondition;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleConditionTypes;
import net.minecraft.resources.Identifier;

public record IsCriticalCondition() implements DamageRuleCondition {

    public static final MapCodec<IsCriticalCondition> CODEC =
            MapCodec.unit(new IsCriticalCondition());

    @Override
    public Identifier type() {
        return DamageRuleConditionTypes.IS_CRITICAL;
    }

    @Override
    public boolean test(DamageNexusContext ctx) {
        return ctx.isCritical();
    }
}