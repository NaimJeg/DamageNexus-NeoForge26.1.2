package io.github.naimjeg.damagenexus.api.affix.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.affix.AffixEffect;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.affix.AffixEffectTypes;
import net.minecraft.resources.Identifier;

public record OverrideFinalDamageEffect(
        float value
) implements AffixEffect {

    public static final MapCodec<OverrideFinalDamageEffect> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT
                            .fieldOf("value")
                            .forGetter(OverrideFinalDamageEffect::value)
            ).apply(instance, OverrideFinalDamageEffect::new));

    @Override
    public Identifier type() {
        return AffixEffectTypes.OVERRIDE_FINAL_DAMAGE;
    }

    @Override
    public void apply(DamageNexusContext ctx) {
        ctx.overrideFinalDamage(value, "affix:override_final_damage");
    }
}