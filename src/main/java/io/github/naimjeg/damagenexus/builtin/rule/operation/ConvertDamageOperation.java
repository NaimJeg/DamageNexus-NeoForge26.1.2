package io.github.naimjeg.damagenexus.builtin.rule.operation;

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

public record ConvertDamageOperation(
        Identifier fromChannel,
        Identifier toChannel,
        float ratio
) implements DamageRuleOperation {

    public static final MapCodec<ConvertDamageOperation> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    DamageRuleCodecs.DAMAGE_CHANNEL_ID
                            .fieldOf("from_channel")
                            .forGetter(ConvertDamageOperation::fromChannel),

                    DamageRuleCodecs.DAMAGE_CHANNEL_ID
                            .fieldOf("to_channel")
                            .forGetter(ConvertDamageOperation::toChannel),

                    DamageRuleCodecs.RATIO_0_TO_1
                            .fieldOf("ratio")
                            .forGetter(ConvertDamageOperation::ratio)
            ).apply(instance, ConvertDamageOperation::new));

    @Override
    public Identifier type() {
        return DamageRuleOperationTypes.CONVERT_DAMAGE;
    }

    @Override
    public void apply(DamageNexusContext ctx) {
        ctx.convertDamage(
                DamageChannelRegistry.getChannelOrUntyped(fromChannel),
                DamageChannelRegistry.getChannelOrUntyped(toChannel),
                ratio,
                RuleTraceIds.CONVERT_DAMAGE
        );
    }

    @Override
    public java.util.Set<DamagePhase> supportedPhases() {
        return java.util.Set.of(DamagePhase.TYPE_SCALING);
    }

    @Override
    public float stackingValue() {
        return ratio;
    }
}