package io.github.naimjeg.damagenexus.api.affix.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.affix.AffixEffect;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.affix.AffixEffectTypes;
import net.minecraft.resources.Identifier;

public record AddGlobalPostMultiplierEffect(
        float value
) implements AffixEffect {

    public static final MapCodec<AddGlobalPostMultiplierEffect> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT
                            .fieldOf("value")
                            .forGetter(AddGlobalPostMultiplierEffect::value)
            ).apply(instance, AddGlobalPostMultiplierEffect::new));

    @Override
    public Identifier type() {
        return AffixEffectTypes.ADD_GLOBAL_POST_MULTIPLIER;
    }

    @Override
    public void apply(DamageNexusContext ctx) {
        ctx.addGlobalPostMultiplier(
                value,
                "affix:add_global_post_multiplier"
        );
    }
}