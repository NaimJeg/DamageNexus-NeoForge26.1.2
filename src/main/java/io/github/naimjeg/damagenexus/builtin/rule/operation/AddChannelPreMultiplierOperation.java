package io.github.naimjeg.damagenexus.builtin.rule.operation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleCodecs;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleOperation;
import io.github.naimjeg.damagenexus.api.rule.RuleTraceIds;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.core.registry.DamageChannelRegistry;
import io.github.naimjeg.damagenexus.core.registry.PreMultiplierBucketRegistry;
import io.github.naimjeg.damagenexus.registry.PreMultiplierBuckets;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleOperationTypes;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public record AddChannelPreMultiplierOperation(
        Identifier channelId,
        Optional<Identifier> preMultiplierBucketId,
        float value
) implements DamageRuleOperation {

    public AddChannelPreMultiplierOperation(
            DamageChannel channel,
            Optional<Identifier> preMultiplierBucketId,
            float value
    ) {
        this(channel.id(), preMultiplierBucketId, value);
    }

    public static final MapCodec<AddChannelPreMultiplierOperation> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    DamageRuleCodecs.DAMAGE_CHANNEL_ID
                            .fieldOf("channel")
                            .forGetter(AddChannelPreMultiplierOperation::channelId),

                    DamageRuleCodecs.PRE_MULTIPLIER_BUCKET_ID
                            .optionalFieldOf("pre_multiplier_bucket")
                            .forGetter(AddChannelPreMultiplierOperation::preMultiplierBucketId),

                    DamageRuleCodecs.PRE_MULTIPLIER_BUCKET_ID
                            .optionalFieldOf("preMultiplierBucketId")
                            .forGetter(operation -> Optional.<Identifier>empty()),

                    DamageRuleCodecs.PRE_MULTIPLIER_BUCKET_ID
                            .optionalFieldOf("bucket")
                            .forGetter(operation -> Optional.<Identifier>empty()),

                    Codec.FLOAT
                            .fieldOf("value")
                            .forGetter(AddChannelPreMultiplierOperation::value)
            ).apply(instance, AddChannelPreMultiplierOperation::fromCodec));

    private static AddChannelPreMultiplierOperation fromCodec(
            Identifier channelId,
            Optional<Identifier> preMultiplierBucketId,
            Optional<Identifier> legacyCamelPreMultiplierBucketId,
            Optional<Identifier> legacyBucket,
            float value
    ) {
        return new AddChannelPreMultiplierOperation(
                channelId,
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

    public AddChannelPreMultiplierOperation {
        if (preMultiplierBucketId == null) {
            preMultiplierBucketId = Optional.empty();
        }
    }

    public DamageChannel channel() {
        return DamageChannelRegistry.getChannelOrUntyped(channelId);
    }

    @Override
    public Identifier type() {
        return DamageRuleOperationTypes.ADD_CHANNEL_PRE_MULTIPLIER;
    }

    @Override
    public void apply(DamageNexusContext ctx) {
        DamageChannel channel = channel();

        int bucketId = preMultiplierBucketId
                .map(PreMultiplierBucketRegistry::getPreMultiplierBucketId)
                .orElseGet(() -> PreMultiplierBuckets.forChannelDamage(channel));

        ctx.addChannelPreMultiplier(
                channel,
                bucketId,
                value,
                RuleTraceIds.ADD_CHANNEL_PRE_MULTIPLIER
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
    public float stackingValue() {
        return value;
    }
}