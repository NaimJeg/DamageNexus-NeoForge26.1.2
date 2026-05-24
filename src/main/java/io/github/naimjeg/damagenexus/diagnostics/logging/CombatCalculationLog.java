package io.github.naimjeg.damagenexus.diagnostics.logging;

import io.github.naimjeg.damagenexus.ModConfig;
import io.github.naimjeg.damagenexus.api.enums.DamageApplicationBucket;

public interface CombatCalculationLog {

    void offenseStart();

    void channelResult(
            String channelId,
            float baseAmount,
            float result
    );

    void offensiveSummary(float total);

    void armor(
            String channelId,
            float damageBefore,
            float baseArmor,
            float armorEffectiveness,
            float effectiveArmor,
            float reductionPercent
    );

    void resistance(
            String channelId,
            float attributeRating,
            float temporaryRating,
            float totalRating,
            float reductionPercent
    );

    void enchantmentProtection(
            String enchantmentId,
            int level,
            float scoreDelta,
            float ratingDelta
    );

    void defensiveSummary(float total);

    void bucketResult(
            String channelId,
            DamageApplicationBucket applicationBucket,
            float baseAmount,
            float offensiveAmount,
            float postMitigationAmount,
            boolean affectedByMitigation
    );

    void vanillaReductionCompatibility(
            ModConfig.VanillaReductionCompatibilityMode mode,
            boolean suppressArmor,
            boolean suppressEnchantments,
            boolean suppressMobEffects,
            boolean suppressInnateResistance
    );
}
