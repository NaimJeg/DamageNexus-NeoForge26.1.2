package io.github.naimjeg.damagenexus.builtin.rule.condition;

import com.mojang.serialization.MapCodec;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleCondition;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleConditionTypes;
import io.github.naimjeg.damagenexus.util.EntityConditionUtil;
import net.minecraft.resources.Identifier;

public record TargetIsBossCondition() implements DamageRuleCondition {

    public static final MapCodec<TargetIsBossCondition> CODEC =
            MapCodec.unit(new TargetIsBossCondition());

    @Override
    public Identifier type() {
        return DamageRuleConditionTypes.TARGET_IS_BOSS;
    }

    @Override
    public boolean test(DamageNexusContext ctx) {
        return EntityConditionUtil.isBoss(ctx.victim());
    }
}