package io.github.naimjeg.damagenexus.builtin.rule.operation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleCodecs;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleOperation;
import io.github.naimjeg.damagenexus.api.rule.RuleTraceIds;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.core.registry.DamageChannelRegistry;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleOperationTypes;
import net.minecraft.resources.Identifier;

public record AddChannelMitigationOperation(
        Identifier channelId,
        float value
) implements DamageRuleOperation {

    public static final MapCodec<AddChannelMitigationOperation> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    DamageRuleCodecs.DAMAGE_CHANNEL_ID
                            .fieldOf("channel")
                            .forGetter(AddChannelMitigationOperation::channelId),

                    Codec.FLOAT
                            .fieldOf("value")
                            .forGetter(AddChannelMitigationOperation::value)
            ).apply(instance, AddChannelMitigationOperation::new));

    @Override
    public Identifier type() {
        return DamageRuleOperationTypes.ADD_CHANNEL_MITIGATION;
    }

    @Override
    public void apply(DamageNexusContext ctx) {
        ctx.addChannelMitigation(
                DamageChannelRegistry.getChannelOrUntyped(channelId),
                value,
                RuleTraceIds.ADD_CHANNEL_MITIGATION
        );
    }

    @Override
    public java.util.Set<DamagePhase> supportedPhases() {
        return java.util.Set.of(DamagePhase.MITIGATION_SETUP);
    }

    @Override
    public float stackingValue() {
        return value;
    }
}