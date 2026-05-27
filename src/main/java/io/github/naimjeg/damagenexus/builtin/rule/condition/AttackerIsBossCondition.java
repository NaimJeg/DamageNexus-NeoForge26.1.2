package io.github.naimjeg.damagenexus.builtin.rule.condition;

import com.mojang.serialization.MapCodec;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleCondition;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleConditionTypes;
import io.github.naimjeg.damagenexus.util.EntityConditionUtil;
import net.minecraft.resources.Identifier;

public record AttackerIsBossCondition() implements DamageRuleCondition {

    public static final MapCodec<AttackerIsBossCondition> CODEC =
            MapCodec.unit(new AttackerIsBossCondition());

    @Override
    public Identifier type() {
        return DamageRuleConditionTypes.ATTACKER_IS_BOSS;
    }

    @Override
    public boolean test(DamageNexusContext ctx) {
        return EntityConditionUtil.isBoss(ctx.attacker);
    }
}