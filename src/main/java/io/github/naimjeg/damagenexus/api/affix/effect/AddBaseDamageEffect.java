package io.github.naimjeg.damagenexus.api.affix.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.affix.AffixCodecs;
import io.github.naimjeg.damagenexus.api.affix.AffixEffect;
import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.affix.AffixEffectTypes;
import net.minecraft.resources.Identifier;

public record AddBaseDamageEffect(
        DamageChannel channel,
        float value
) implements AffixEffect {

    public static final MapCodec<AddBaseDamageEffect> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    AffixCodecs.DAMAGE_CHANNEL
                            .fieldOf("channel")
                            .forGetter(AddBaseDamageEffect::channel),

                    Codec.FLOAT
                            .fieldOf("value")
                            .forGetter(AddBaseDamageEffect::value)
            ).apply(instance, AddBaseDamageEffect::new));

    @Override
    public Identifier type() {
        return AffixEffectTypes.ADD_BASE_DAMAGE;
    }

    @Override
    public void apply(DamageNexusContext ctx) {
        ctx.addBaseDamage(channel, "affix:add_base_damage", value);
    }
}