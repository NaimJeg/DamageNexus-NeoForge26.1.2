package io.github.naimjeg.damagenexus.builtin.rule.operation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleCodecs;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleOperation;
import io.github.naimjeg.damagenexus.api.rule.RuleTraceIds;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.core.registry.DamageChannelRegistry;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleOperationTypes;
import net.minecraft.resources.Identifier;

public record AddBaseDamageOperation(
        Identifier channelId,
        float value
) implements DamageRuleOperation {

    public AddBaseDamageOperation(
            DamageChannel channel,
            float value
    ) {
        this(channel.id(), value);
    }

    public static final MapCodec<AddBaseDamageOperation> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    DamageRuleCodecs.DAMAGE_CHANNEL_ID
                            .fieldOf("channel")
                            .forGetter(AddBaseDamageOperation::channelId),

                    Codec.FLOAT
                            .fieldOf("value")
                            .forGetter(AddBaseDamageOperation::value)
            ).apply(instance, AddBaseDamageOperation::new));

    public DamageChannel channel() {
        return DamageChannelRegistry.getChannelOrUntyped(channelId);
    }

    @Override
    public Identifier type() {
        return DamageRuleOperationTypes.ADD_BASE_DAMAGE;
    }

    @Override
    public void apply(DamageNexusContext ctx) {
        ctx.addBaseDamage(
                channel(),
                value,
                RuleTraceIds.ADD_BASE_DAMAGE
        );
    }

    @Override
    public float stackingValue() {
        return value;
    }
}