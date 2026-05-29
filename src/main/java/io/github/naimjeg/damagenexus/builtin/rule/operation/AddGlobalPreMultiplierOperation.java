package io.github.naimjeg.damagenexus.builtin.rule.operation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleOperation;
import io.github.naimjeg.damagenexus.api.rule.RuleTraceIds;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.core.registry.PreMultiplierBucketRegistry;
import io.github.naimjeg.damagenexus.registry.PreMultiplierBuckets;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleOperationTypes;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public record AddGlobalPreMultiplierOperation(
        Optional<Identifier> preMultiplierBucketId,
        float value
) implements DamageRuleOperation {

    public static final MapCodec<AddGlobalPreMultiplierOperation> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Identifier.CODEC
                            .optionalFieldOf("preMultiplierBucketId")
                            .forGetter(AddGlobalPreMultiplierOperation::preMultiplierBucketId),

                    Codec.FLOAT
                            .fieldOf("value")
                            .forGetter(AddGlobalPreMultiplierOperation::value)
            ).apply(instance, AddGlobalPreMultiplierOperation::new));

    @Override
    public Identifier type() {
        return DamageRuleOperationTypes.ADD_GLOBAL_PRE_MULTIPLIER;
    }

    @Override
    public void apply(DamageNexusContext ctx) {
        int bucketId = preMultiplierBucketId
                .map(PreMultiplierBucketRegistry::getPreMultiplierBucketId)
                .orElse(PreMultiplierBuckets.GENERIC_DAMAGE);

        ctx.addGlobalPreMultiplier(
                bucketId,
                value,
                RuleTraceIds.ADD_GLOBAL_PRE_MULTIPLIER
        );
    }

    @Override
    public java.util.Set<DamagePhase> supportedPhases() {
        return java.util.Set.of(
                DamagePhase.CRITICAL_HIT,
                DamagePhase.CONDITIONAL_MULTI,
                DamagePhase.GLOBAL_ADJUSTMENT
        );
    }

    @Override
    public float stackingValue() {
        return value;
    }
}