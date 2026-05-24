package io.github.naimjeg.damagenexus.builtin.rule.operation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleOperation;
import io.github.naimjeg.damagenexus.api.rule.RuleTraceIds;
import io.github.naimjeg.damagenexus.core.pipeline.DamageMutationResult;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleOperationTypes;
import net.minecraft.resources.Identifier;

import java.util.Set;

public record MultiplyArmorEffectivenessOperation(
        float value
) implements DamageRuleOperation {

    public static final MapCodec<MultiplyArmorEffectivenessOperation> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT
                            .fieldOf("value")
                            .forGetter(MultiplyArmorEffectivenessOperation::value)
            ).apply(instance, MultiplyArmorEffectivenessOperation::new));

    @Override
    public Identifier type() {
        return DamageRuleOperationTypes.MULTIPLY_ARMOR_EFFECTIVENESS;
    }

    @Override
    public void apply(DamageNexusContext ctx) {
        applyWithResult(ctx);
    }

    @Override
    public DamageMutationResult applyWithResult(DamageNexusContext ctx) {
        return ctx.tryMultiplyArmorEffectiveness(
                value,
                RuleTraceIds.MULTIPLY_ARMOR_EFFECTIVENESS
        );
    }

    @Override
    public Set<DamagePhase> supportedPhases() {
        return Set.of(DamagePhase.MITIGATION_SETUP);
    }

    @Override
    public float stackingValue() {
        return value;
    }
}