package io.github.naimjeg.damagenexus.api.affix.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.naimjeg.damagenexus.api.affix.AffixCondition;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.affix.AffixConditionTypes;
import net.minecraft.resources.Identifier;

public record AlwaysCondition() implements AffixCondition {

    public static final MapCodec<AlwaysCondition> CODEC =
            MapCodec.unit(new AlwaysCondition());

    @Override
    public Identifier type() {
        return AffixConditionTypes.ALWAYS;
    }

    @Override
    public boolean test(DamageNexusContext ctx) {
        return true;
    }
}