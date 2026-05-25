package io.github.naimjeg.damagenexus.api.affix.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.affix.AffixCodecs;
import io.github.naimjeg.damagenexus.api.affix.AffixEffect;
import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.ModConstants; // 需要确保能读取到 BASE_ADDITIVE 的 ID
import io.github.naimjeg.damagenexus.registry.affix.AffixEffectTypes;
import net.minecraft.resources.Identifier;

public record AddChannelPreMultiplierEffect(
        DamageChannel channel,
        float value
) implements AffixEffect {

    public static final MapCodec<AddChannelPreMultiplierEffect> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    AffixCodecs.DAMAGE_CHANNEL
                            .fieldOf("channel")
                            .forGetter(AddChannelPreMultiplierEffect::channel),

                    Codec.FLOAT
                            .fieldOf("value")
                            .forGetter(AddChannelPreMultiplierEffect::value)
            ).apply(instance, AddChannelPreMultiplierEffect::new));

    @Override
    public Identifier type() {
        return AffixEffectTypes.ADD_CHANNEL_PRE_MULTIPLIER;
    }

    @Override
    public void apply(DamageNexusContext ctx) {
        ctx.addChannelPreModifier(channel, ModConstants.BASE_ADDITIVE, value, "affix:add_channel_pre_mult");
    }
}