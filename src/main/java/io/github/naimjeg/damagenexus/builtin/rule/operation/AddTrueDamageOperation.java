package io.github.naimjeg.damagenexus.builtin.rule.operation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.rule.ChannelReferencingOperation;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleCodecs;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleOperation;
import io.github.naimjeg.damagenexus.api.rule.RuleTraceIds;
import io.github.naimjeg.damagenexus.core.pipeline.DamageMutationResult;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.core.registry.DamageChannelRegistry;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleOperationTypes;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Set;

public record AddTrueDamageOperation(
        Identifier channelId,
        float value
) implements DamageRuleOperation, ChannelReferencingOperation {

    public AddTrueDamageOperation(
            DamageChannel channel,
            float value
    ) {
        this(channel == null ? DamageChannel.UNTYPED_ID : channel.id(), value);
    }

    public static final MapCodec<AddTrueDamageOperation> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    DamageRuleCodecs.DAMAGE_CHANNEL_ID
                            .fieldOf("channel")
                            .forGetter(AddTrueDamageOperation::channelId),

                    Codec.FLOAT
                            .fieldOf("value")
                            .forGetter(AddTrueDamageOperation::value)
            ).apply(instance, AddTrueDamageOperation::new));

    public AddTrueDamageOperation {
        if (channelId == null) {
            channelId = DamageChannel.UNTYPED_ID;
        }
    }

    @Override
    public Identifier type() {
        return DamageRuleOperationTypes.ADD_TRUE_DAMAGE;
    }

    @Override
    public void apply(DamageNexusContext ctx) {
        applyWithResult(ctx);
    }

    @Override
    public DamageMutationResult applyWithResult(DamageNexusContext ctx) {
        return ctx.tryAddTrueDamage(
                DamageChannelRegistry.getChannelOrUntyped(channelId),
                value,
                RuleTraceIds.ADD_TRUE_DAMAGE
        );
    }

    @Override
    public Set<DamagePhase> supportedPhases() {
        return Set.of(DamagePhase.BASE_MODIFICATION);
    }

    @Override
    public List<Identifier> referencedChannels() {
        return List.of(channelId);
    }

    @Override
    public float stackingValue() {
        return value;
    }
}