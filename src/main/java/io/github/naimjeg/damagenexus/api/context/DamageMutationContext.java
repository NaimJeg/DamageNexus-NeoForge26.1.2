package io.github.naimjeg.damagenexus.api.context;

import io.github.naimjeg.damagenexus.api.enums.DamageApplicationBucket;
import io.github.naimjeg.damagenexus.api.enums.DamageChannel;

public interface DamageMutationContext {

    DamageMutationResult tryAddBaseDamage(
            DamageChannel channel,
            DamageApplicationBucket bucket,
            float value,
            String sourceId
    );

    DamageMutationResult tryAddBaseDamage(
            DamageChannel channel,
            float value,
            String sourceId
    );

    DamageMutationResult tryAddChannelPreMultiplier(
            DamageChannel channel,
            int modifierId,
            float value,
            String sourceId
    );

    DamageMutationResult tryAddApplicationPreMultiplier(
            DamageApplicationBucket bucket,
            int modifierId,
            float value,
            String sourceId
    );

    DamageMutationResult tryAddGlobalPreMultiplier(
            int modifierId,
            float value,
            String sourceId
    );

    DamageMutationResult tryAddChannelPostMultiplier(
            DamageChannel channel,
            float value,
            String sourceId
    );

    DamageMutationResult tryAddGlobalPostMultiplier(
            float value,
            String sourceId
    );

    DamageMutationResult tryConvertDamage(
            DamageChannel from,
            DamageChannel to,
            float ratio,
            String sourceId
    );

    DamageMutationResult tryGainExtraDamage(
            DamageChannel from,
            DamageChannel to,
            float ratio,
            String sourceId
    );

    DamageMutationResult tryAddTrueDamage(
            DamageChannel channel,
            float value,
            String sourceId
    );

    DamageMutationResult tryMultiplyArmorEffectiveness(
            float multiplier,
            String sourceId
    );

    DamageMutationResult tryAddTemporaryResistance(
            DamageChannel channel,
            float rating,
            String sourceId
    );

    DamageMutationResult tryAddChannelMitigation(
            DamageChannel channel,
            float reductionPercent,
            String sourceId
    );

    DamageMutationResult tryAddGlobalMitigation(
            float reductionPercent,
            String sourceId
    );

    DamageMutationResult tryOverrideFinalDamage(
            float amount,
            String sourceId
    );

    DamageMutationResult tryCancelDamage(String sourceId);
}
