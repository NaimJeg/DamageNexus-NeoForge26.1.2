package io.github.naimjeg.damagenexus.api.affix.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.affix.AffixCodecs;
import io.github.naimjeg.damagenexus.api.affix.AffixCondition;
import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import io.github.naimjeg.damagenexus.core.DamageComponent;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.affix.AffixConditionTypes;
import net.minecraft.resources.Identifier;

public record DamageChannelIsCondition(
        DamageChannel channel
) implements AffixCondition {

    public static final MapCodec<DamageChannelIsCondition> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    AffixCodecs.DAMAGE_CHANNEL
                            .fieldOf("channel")
                            .forGetter(DamageChannelIsCondition::channel)
            ).apply(instance, DamageChannelIsCondition::new));

    @Override
    public Identifier type() {
        return AffixConditionTypes.DAMAGE_CHANNEL_IS;
    }

    @Override
    public boolean test(DamageNexusContext ctx) {
        for (int i = 0; i < ctx.getActiveComponentCount(); i++) {
            DamageComponent component = ctx.getActiveComponent(i);

            if (component.channel.equals(channel)
                    && component.getPostMitigationAmount() > 0.0f) {
                return true;
            }
        }

        return false;
    }
}