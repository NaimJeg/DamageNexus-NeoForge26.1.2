package io.github.naimjeg.damagenexus.builtin.rule.operation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleOperation;
import io.github.naimjeg.damagenexus.api.rule.RuleTraceIds;
import io.github.naimjeg.damagenexus.core.pipeline.DamageMutationResult;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleOperationTypes;
import net.minecraft.resources.Identifier;

public record CancelDamageOperation(
        String sourceId
) implements DamageRuleOperation {

    public static final MapCodec<CancelDamageOperation> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.STRING
                            .optionalFieldOf(
                                    "source",
                                    RuleTraceIds.CANCEL_DAMAGE
                            )
                            .forGetter(CancelDamageOperation::sourceId)
            ).apply(instance, CancelDamageOperation::new));

    public CancelDamageOperation() {
        this(RuleTraceIds.CANCEL_DAMAGE);
    }

    @Override
    public Identifier type() {
        return DamageRuleOperationTypes.CANCEL_DAMAGE;
    }

    @Override
    public void apply(DamageNexusContext ctx) {
        applyWithResult(ctx);
    }

    @Override
    public DamageMutationResult applyWithResult(DamageNexusContext ctx) {
        return ctx.tryCancelDamage(sourceId);
    }

    @Override
    public float stackingValue() {
        return 1.0f;
    }
}