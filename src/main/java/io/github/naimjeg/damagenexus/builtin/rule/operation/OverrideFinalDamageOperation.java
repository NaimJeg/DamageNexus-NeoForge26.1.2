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

public record OverrideFinalDamageOperation(
        float value
) implements DamageRuleOperation {

    public static final MapCodec<OverrideFinalDamageOperation> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT
                            .fieldOf("value")
                            .forGetter(OverrideFinalDamageOperation::value)
            ).apply(instance, OverrideFinalDamageOperation::new));

    @Override
    public Identifier type() {
        return DamageRuleOperationTypes.OVERRIDE_FINAL_DAMAGE;
    }

    @Override
    public void apply(DamageNexusContext ctx) {
        applyWithResult(ctx);
    }

    @Override
    public DamageMutationResult applyWithResult(DamageNexusContext ctx) {
        return ctx.tryOverrideFinalDamage(
                value,
                RuleTraceIds.OVERRIDE_FINAL_DAMAGE
        );
    }

    @Override
    public java.util.Set<DamagePhase> supportedPhases() {
        return java.util.Set.of(DamagePhase.FINAL_OVERRIDE);
    }

    @Override
    public float stackingValue() {
        return value;
    }
}