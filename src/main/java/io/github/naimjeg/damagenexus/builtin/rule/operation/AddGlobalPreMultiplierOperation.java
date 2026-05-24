package io.github.naimjeg.damagenexus.builtin.rule.operation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleCodecs;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleOperation;
import io.github.naimjeg.damagenexus.api.rule.PreMultiplierBucketReferencingOperation;
import io.github.naimjeg.damagenexus.api.rule.RuleTraceIds;
import io.github.naimjeg.damagenexus.core.pipeline.DamageMutationResult;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.core.registry.PreMultiplierBucketRegistry;
import io.github.naimjeg.damagenexus.registry.PreMultiplierBuckets;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleOperationTypes;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

public record AddGlobalPreMultiplierOperation(
        Optional<Identifier> preMultiplierBucketId,
        float value
) implements DamageRuleOperation, PreMultiplierBucketReferencingOperation {

    public static final MapCodec<AddGlobalPreMultiplierOperation> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    DamageRuleCodecs.PRE_MULTIPLIER_BUCKET_ID
                            .optionalFieldOf("pre_multiplier_bucket")
                            .forGetter(AddGlobalPreMultiplierOperation::preMultiplierBucketId),

                    DamageRuleCodecs.PRE_MULTIPLIER_BUCKET_ID
                            .optionalFieldOf("preMultiplierBucketId")
                            .forGetter(operation -> Optional.<Identifier>empty()),

                    DamageRuleCodecs.PRE_MULTIPLIER_BUCKET_ID
                            .optionalFieldOf("bucket")
                            .forGetter(operation -> Optional.<Identifier>empty()),

                    Codec.FLOAT
                            .fieldOf("value")
                            .forGetter(AddGlobalPreMultiplierOperation::value)
            ).apply(instance, AddGlobalPreMultiplierOperation::fromCodec));

    private static AddGlobalPreMultiplierOperation fromCodec(
            Optional<Identifier> preMultiplierBucketId,
            Optional<Identifier> legacyCamelPreMultiplierBucketId,
            Optional<Identifier> legacyBucket,
            float value
    ) {
        return new AddGlobalPreMultiplierOperation(
                firstPresent(
                        preMultiplierBucketId,
                        legacyCamelPreMultiplierBucketId,
                        legacyBucket
                ),
                value
        );
    }

    @SafeVarargs
    private static <T> Optional<T> firstPresent(Optional<T>... values) {
        for (Optional<T> value : values) {
            if (value.isPresent()) {
                return value;
            }
        }

        return Optional.empty();
    }

    public AddGlobalPreMultiplierOperation {
        if (preMultiplierBucketId == null) {
            preMultiplierBucketId = Optional.empty();
        }
    }

    @Override
    public Identifier type() {
        return DamageRuleOperationTypes.ADD_GLOBAL_PRE_MULTIPLIER;
    }

    @Override
    public void apply(DamageNexusContext ctx) {
        applyWithResult(ctx);
    }

    @Override
    public DamageMutationResult applyWithResult(DamageNexusContext ctx) {
        int bucketId = preMultiplierBucketId
                .map(PreMultiplierBucketRegistry::getPreMultiplierBucketId)
                .orElse(PreMultiplierBuckets.GENERIC_DAMAGE);

        return ctx.tryAddGlobalPreMultiplier(
                bucketId,
                value,
                RuleTraceIds.ADD_GLOBAL_PRE_MULTIPLIER
        );
    }

    @Override
    public java.util.Set<DamagePhase> supportedPhases() {
        return java.util.Set.of(
                DamagePhase.TYPE_SCALING,
                DamagePhase.CRITICAL_HIT,
                DamagePhase.CONDITIONAL_MULTI,
                DamagePhase.GLOBAL_ADJUSTMENT
        );
    }

    @Override
    public List<Identifier> referencedPreMultiplierBuckets() {
        return preMultiplierBucketId
                .map(List::of)
                .orElseGet(List::of);
    }

    @Override
    public float stackingValue() {
        return value;
    }
}