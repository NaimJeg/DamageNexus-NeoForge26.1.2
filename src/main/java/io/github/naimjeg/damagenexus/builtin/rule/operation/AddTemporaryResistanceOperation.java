package io.github.naimjeg.damagenexus.builtin.rule.operation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleCodecs;
import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleOperation;
import io.github.naimjeg.damagenexus.api.rule.RuleTraceIds;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleOperationTypes;
import net.minecraft.resources.Identifier;

public record AddTemporaryResistanceOperation(
        DamageChannel channel,
        float value
) implements DamageRuleOperation {

    public static final MapCodec<AddTemporaryResistanceOperation> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    DamageRuleCodecs.DAMAGE_CHANNEL
                            .fieldOf("channel")
                            .forGetter(AddTemporaryResistanceOperation::channel),

                    Codec.FLOAT
                            .fieldOf("value")
                            .forGetter(AddTemporaryResistanceOperation::value)
            ).apply(instance, AddTemporaryResistanceOperation::new));

    @Override
    public Identifier type() {
        return DamageRuleOperationTypes.ADD_TEMPORARY_RESISTANCE;
    }

    @Override
    public void apply(DamageNexusContext ctx) {
        ctx.addTemporaryResistance(
                channel,
                value,
                RuleTraceIds.ADD_TEMPORARY_RESISTANCE
        );
    }

    @Override
    public float stackingValue() {
        return value;
    }
}