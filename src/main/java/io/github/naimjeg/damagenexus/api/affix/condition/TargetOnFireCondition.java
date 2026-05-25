package io.github.naimjeg.damagenexus.api.affix.condition;

import com.mojang.serialization.MapCodec;
import io.github.naimjeg.damagenexus.api.affix.AffixCondition;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.affix.AffixConditionTypes;
import net.minecraft.resources.Identifier;


public record TargetOnFireCondition() implements AffixCondition {

    public static final MapCodec<TargetOnFireCondition> CODEC =
            MapCodec.unit(new TargetOnFireCondition());

    @Override
    public Identifier type() {
        return AffixConditionTypes.TARGET_ON_FIRE;
    }

    @Override
    public boolean test(DamageNexusContext ctx) {
        return ctx.victim != null && ctx.victim.isOnFire();
    }
}