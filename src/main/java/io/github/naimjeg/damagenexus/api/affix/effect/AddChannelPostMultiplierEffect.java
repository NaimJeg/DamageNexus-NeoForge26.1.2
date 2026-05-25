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

public record AddChannelPostMultiplierEffect(
        DamageChannel channel,
        float value
) implements AffixEffect {

    public static final MapCodec<AddChannelPostMultiplierEffect> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    AffixCodecs.DAMAGE_CHANNEL
                            .fieldOf("channel")
                            .forGetter(AddChannelPostMultiplierEffect::channel),

                    Codec.FLOAT
                            .fieldOf("value")
                            .forGetter(AddChannelPostMultiplierEffect::value)
            ).apply(instance, AddChannelPostMultiplierEffect::new));

    @Override
    public Identifier type() {
        return AffixEffectTypes.ADD_CHANNEL_POST_MULTIPLIER;
    }

    @Override
    public void apply(DamageNexusContext ctx) {
        ctx.addChannelPostMultiplier(channel, value, "affix:add_channel_post_multiplier");
    }
}