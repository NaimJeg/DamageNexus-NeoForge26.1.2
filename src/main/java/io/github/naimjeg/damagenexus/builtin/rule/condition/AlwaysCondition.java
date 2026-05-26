package io.github.naimjeg.damagenexus.builtin.rule.condition;

import com.mojang.serialization.MapCodec;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleCondition;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleConditionTypes;
import net.minecraft.resources.Identifier;

public record AlwaysCondition() implements DamageRuleCondition {

    public static final MapCodec<AlwaysCondition> CODEC =
            MapCodec.unit(new AlwaysCondition());

    @Override
    public Identifier type() {
        return DamageRuleConditionTypes.ALWAYS;
    }

    @Override
    public boolean test(DamageNexusContext ctx) {
        return true;
    }
}