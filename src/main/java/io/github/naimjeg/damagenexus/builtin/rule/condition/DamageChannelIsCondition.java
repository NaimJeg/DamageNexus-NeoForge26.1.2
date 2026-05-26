package io.github.naimjeg.damagenexus.builtin.rule.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleCodecs;
import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleCondition;
import io.github.naimjeg.damagenexus.core.DamageComponent;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleConditionTypes;
import net.minecraft.resources.Identifier;

public record DamageChannelIsCondition(
        DamageChannel channel
) implements DamageRuleCondition {

    public static final MapCodec<DamageChannelIsCondition> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    DamageRuleCodecs.DAMAGE_CHANNEL
                            .fieldOf("channel")
                            .forGetter(DamageChannelIsCondition::channel)
            ).apply(instance, DamageChannelIsCondition::new));

    @Override
    public Identifier type() {
        return DamageRuleConditionTypes.DAMAGE_CHANNEL_IS;
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