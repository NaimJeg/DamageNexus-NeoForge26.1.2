package io.github.naimjeg.damagenexus.builtin.rule.operation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleCodecs;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleOperation;
import io.github.naimjeg.damagenexus.api.rule.RuleTraceIds;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.core.registry.PreMultiplierBucketRegistry;
import io.github.naimjeg.damagenexus.registry.PreMultiplierBuckets;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleOperationTypes;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public record AddChannelPreMultiplierOperation(
        DamageChannel channel,
        Optional<Identifier> bucket,
        float value
) implements DamageRuleOperation {

    public static final MapCodec<AddChannelPreMultiplierOperation> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    DamageRuleCodecs.DAMAGE_CHANNEL
                            .fieldOf("channel")
                            .forGetter(AddChannelPreMultiplierOperation::channel),

                    Identifier.CODEC
                            .optionalFieldOf("bucket")
                            .forGetter(AddChannelPreMultiplierOperation::bucket),

                    Codec.FLOAT
                            .fieldOf("value")
                            .forGetter(AddChannelPreMultiplierOperation::value)
            ).apply(instance, AddChannelPreMultiplierOperation::new));

    @Override
    public Identifier type() {
        return DamageRuleOperationTypes.ADD_CHANNEL_PRE_MULTIPLIER;
    }

    @Override
    public void apply(DamageNexusContext ctx) {
        int bucketId = bucket
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
    public float stackingValue() {
        return value;
    }
}