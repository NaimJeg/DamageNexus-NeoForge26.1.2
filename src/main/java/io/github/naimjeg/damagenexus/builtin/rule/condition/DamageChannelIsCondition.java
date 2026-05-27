package io.github.naimjeg.damagenexus.builtin.rule.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleCodecs;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleCondition;
import io.github.naimjeg.damagenexus.core.DamageComponent;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.core.registry.DamageChannelRegistry;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleConditionTypes;
import net.minecraft.resources.Identifier;

public record DamageChannelIsCondition(
        Identifier channelId
) implements DamageRuleCondition {

    public DamageChannelIsCondition(DamageChannel channel) {
        this(channel.id());
    }

    public static final MapCodec<DamageChannelIsCondition> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    DamageRuleCodecs.DAMAGE_CHANNEL_ID
                            .fieldOf("channel")
                            .forGetter(DamageChannelIsCondition::channelId)
            ).apply(instance, DamageChannelIsCondition::new));

    public DamageChannel channel() {
        return DamageChannelRegistry.getChannelOrUntyped(channelId);
    }

    @Override
    public Identifier type() {
        return DamageRuleConditionTypes.DAMAGE_CHANNEL_IS;
    }

    @Override
    public boolean test(DamageNexusContext ctx) {
        for (int i = 0; i < ctx.getActiveComponentCount(); i++) {
            DamageComponent component = ctx.getActiveComponent(i);

            if (!component.channel.id().equals(channelId)) {
                continue;
            }

            return component.hasAnyPositiveDamage();
        }

        return false;
    }
}