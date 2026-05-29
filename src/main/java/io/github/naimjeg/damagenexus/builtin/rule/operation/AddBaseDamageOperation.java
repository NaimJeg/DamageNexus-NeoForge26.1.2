package io.github.naimjeg.damagenexus.builtin.rule.operation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.enums.DamageApplicationBucket;
import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleCodecs;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleOperation;
import io.github.naimjeg.damagenexus.api.rule.RuleTraceIds;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.core.registry.DamageChannelRegistry;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleOperationTypes;
import net.minecraft.resources.Identifier;

public record AddBaseDamageOperation(
        Identifier channelId,
        DamageApplicationBucket applicationBucket,
        float value
) implements DamageRuleOperation {

    public AddBaseDamageOperation(
            Identifier channelId,
            float value
    ) {
        this(channelId, DamageApplicationBucket.DN_RULE_BASE, value);
    }

    public AddBaseDamageOperation(
            DamageChannel channel,
            float value
    ) {
        this(
                channel == null ? DamageChannel.UNTYPED_ID : channel.id(),
                DamageApplicationBucket.DN_RULE_BASE,
                value
        );
    }

    public AddBaseDamageOperation(
            DamageChannel channel,
            DamageApplicationBucket applicationBucket,
            float value
    ) {
        this(
                channel == null ? DamageChannel.UNTYPED_ID : channel.id(),
                applicationBucket,
                value
        );
    }

    public static final MapCodec<AddBaseDamageOperation> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    DamageRuleCodecs.DAMAGE_CHANNEL_ID
                            .fieldOf("channel")
                            .forGetter(AddBaseDamageOperation::channelId),

                    DamageRuleCodecs.DAMAGE_APPLICATION_BUCKET
                            .optionalFieldOf(
                                    "application_bucket",
                                    DamageApplicationBucket.DN_RULE_BASE
                            )
                            .forGetter(AddBaseDamageOperation::applicationBucket),

                    Codec.FLOAT
                            .fieldOf("value")
                            .forGetter(AddBaseDamageOperation::value)
            ).apply(instance, AddBaseDamageOperation::new));

    public AddBaseDamageOperation {
        if (channelId == null) {
            channelId = DamageChannel.UNTYPED_ID;
        }

        if (applicationBucket == null) {
            applicationBucket = DamageApplicationBucket.DN_RULE_BASE;
        }
    }

    @Override
    public Identifier type() {
        return DamageRuleOperationTypes.ADD_BASE_DAMAGE;
    }

    @Override
    public void apply(DamageNexusContext ctx) {
        ctx.addBaseDamage(
                DamageChannelRegistry.getChannelOrUntyped(channelId),
                applicationBucket,
                value,
                RuleTraceIds.ADD_BASE_DAMAGE
        );
    }

    @Override
    public java.util.Set<DamagePhase> supportedPhases() {
        return java.util.Set.of(DamagePhase.BASE_MODIFICATION);
    }

    @Override
    public float stackingValue() {
        return value;
    }
}