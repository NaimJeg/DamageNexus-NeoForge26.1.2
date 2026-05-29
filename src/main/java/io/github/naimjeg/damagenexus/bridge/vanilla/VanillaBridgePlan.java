package io.github.naimjeg.damagenexus.bridge.vanilla;

import io.github.naimjeg.damagenexus.api.enums.DamageApplicationBucket;
import org.jspecify.annotations.Nullable;

public record VanillaBridgePlan(
        float initialBaseAmount,
        DamageApplicationBucket initialBaseBucket,

        boolean rebuildOffensiveMobEffects,
        DamageApplicationBucket offensiveMobEffectBucket,

        boolean rebuildOffensiveEnchantment,
        DamageApplicationBucket offensiveEnchantmentBucket,

        boolean rebuildPreEventDelta,
        float offensiveMobEffectDelta,

        String reason
) {
    private static final float EPSILON = 0.0001f;

    public static VanillaBridgePlan from(
            float eventOriginalAmount,
            VanillaDamageSourceProfile profile,
            VanillaDamageCapture.@Nullable OffensiveSnapshot snapshot,
            float observedOffensiveMobEffectDelta,
            float enabledOffensiveMobEffectDelta
    ) {
        DamageApplicationBucket initialBaseBucket =
                initialBaseBucket(profile);

        DamageApplicationBucket mobEffectBucket =
                DamageApplicationBucket.VANILLA_MELEE_BASE;

        DamageApplicationBucket enchantmentBucket =
                offensiveEnchantmentBucket(profile);

        boolean hasObservedMobEffectDelta =
                Float.isFinite(observedOffensiveMobEffectDelta)
                        && Math.abs(observedOffensiveMobEffectDelta) > EPSILON;

        boolean hasEnabledMobEffectDelta =
                Float.isFinite(enabledOffensiveMobEffectDelta)
                        && Math.abs(enabledOffensiveMobEffectDelta) > EPSILON;

        if (snapshot == null) {
            if (hasObservedMobEffectDelta || hasEnabledMobEffectDelta) {
                return new VanillaBridgePlan(
                        removeMobEffectDelta(
                                eventOriginalAmount,
                                observedOffensiveMobEffectDelta
                        ),
                        initialBaseBucket,

                        hasEnabledMobEffectDelta,
                        mobEffectBucket,

                        false,
                        enchantmentBucket,

                        false,
                        hasEnabledMobEffectDelta
                                ? enabledOffensiveMobEffectDelta
                                : 0.0f,

                        hasEnabledMobEffectDelta
                                ? "rebuild_mob_effect_no_snapshot"
                                : "rebuild_observed_mob_effect_base_only_no_snapshot"
                );
            }

            return new VanillaBridgePlan(
                    eventOriginalAmount,
                    initialBaseBucket,

                    false,
                    mobEffectBucket,

                    false,
                    enchantmentBucket,

                    false,
                    0.0f,

                    "no_snapshot"
            );
        }

        boolean hasEnchantDelta = snapshot.hasEnchantDelta();
        boolean hasPreEventDelta = snapshot.hasPreEventDelta();

        boolean preEventBridgeable =
                isBridgeablePreEventDelta(snapshot.preEventDelta().kind())
                        && isUsableRatio(snapshot);

        /*
         * Important:
         * If pre-event delta is unbridgeable, preserve vanilla amount as-is.
         * Do not partially rebuild mob effects, otherwise the unknown vanilla
         * multiplier/order may change final damage.
         */
        if (hasPreEventDelta && !preEventBridgeable) {
            return new VanillaBridgePlan(
                    eventOriginalAmount,
                    initialBaseBucket,

                    false,
                    mobEffectBucket,

                    false,
                    enchantmentBucket,

                    false,
                    0.0f,

                    "fallback_unbridgeable_pre_event kind="
                            + snapshot.preEventDelta().kind()
            );
        }

        if (hasEnchantDelta) {
            return new VanillaBridgePlan(
                    removeMobEffectDelta(
                            snapshot.preEnchantDamage(),
                            observedOffensiveMobEffectDelta
                    ),
                    initialBaseBucket,

                    hasEnabledMobEffectDelta,
                    mobEffectBucket,

                    true,
                    enchantmentBucket,

                    preEventBridgeable,
                    hasEnabledMobEffectDelta
                            ? enabledOffensiveMobEffectDelta
                            : 0.0f,

                    reason(
                            hasObservedMobEffectDelta,
                            hasEnabledMobEffectDelta,
                            true,
                            preEventBridgeable
                    )
            );
        }

        if (preEventBridgeable) {
            return new VanillaBridgePlan(
                    removeMobEffectDelta(
                            snapshot.postEnchantDamage(),
                            observedOffensiveMobEffectDelta
                    ),
                    initialBaseBucket,

                    hasEnabledMobEffectDelta,
                    mobEffectBucket,

                    false,
                    enchantmentBucket,

                    true,
                    hasEnabledMobEffectDelta
                            ? enabledOffensiveMobEffectDelta
                            : 0.0f,

                    reason(
                            hasObservedMobEffectDelta,
                            hasEnabledMobEffectDelta,
                            false,
                            true
                    )
            );
        }

        if (hasObservedMobEffectDelta || hasEnabledMobEffectDelta) {
            return new VanillaBridgePlan(
                    removeMobEffectDelta(
                            eventOriginalAmount,
                            observedOffensiveMobEffectDelta
                    ),
                    initialBaseBucket,

                    hasEnabledMobEffectDelta,
                    mobEffectBucket,

                    false,
                    enchantmentBucket,

                    false,
                    hasEnabledMobEffectDelta
                            ? enabledOffensiveMobEffectDelta
                            : 0.0f,

                    reason(
                            hasObservedMobEffectDelta,
                            hasEnabledMobEffectDelta,
                            false,
                            false
                    )
            );
        }

        return new VanillaBridgePlan(
                eventOriginalAmount,
                initialBaseBucket,

                false,
                mobEffectBucket,

                false,
                enchantmentBucket,

                false,
                0.0f,

                "no_rebuild_needed"
        );
    }

    private static float removeMobEffectDelta(
            float amount,
            float observedMobEffectDelta
    ) {
        float result = amount - observedMobEffectDelta;

        if (!Float.isFinite(result)) {
            return amount;
        }

        return Math.max(0.0f, result);
    }

    private static String reason(
            boolean observedMobEffect,
            boolean enabledMobEffect,
            boolean enchant,
            boolean preEvent
    ) {
        StringBuilder sb = new StringBuilder("rebuild");

        if (enabledMobEffect) {
            sb.append("_mob_effect");
        } else if (observedMobEffect) {
            sb.append("_observed_mob_effect_base_only");
        }

        if (enchant) {
            sb.append("_enchant");
        }

        if (preEvent) {
            sb.append("_pre_event");
        }

        if (sb.length() == "rebuild".length()) {
            return "no_rebuild_needed";
        }

        return sb.toString();
    }

    private static boolean isUsableRatio(
            VanillaDamageCapture.OffensiveSnapshot snapshot
    ) {
        float ratio = snapshot.preEventDelta().ratio();

        return Float.isFinite(ratio)
                && Math.abs(ratio) > EPSILON;
    }

    private static boolean isBridgeablePreEventDelta(
            PreEventDeltaKind kind
    ) {
        return switch (kind) {
            case DIFFICULTY_SCALING,
                 PLAYER_ATTACK_SCALING,
                 PROJECTILE_SCALING,
                 SPECIAL_ATTACK_SCALING,
                 SPEAR_STAB_BONUS,
                 SPEAR_CHARGE_BONUS,
                 SPEAR_ATTACK_BONUS -> true;

            case NONE,
                 UNKNOWN -> false;
        };
    }

    private static DamageApplicationBucket initialBaseBucket(
            VanillaDamageSourceProfile profile
    ) {
        if (profile == null) {
            return DamageApplicationBucket.DN_RULE_BASE;
        }

        if (profile.projectile()) {
            return DamageApplicationBucket.VANILLA_PROJECTILE_BASE;
        }

        if (profile.shouldApplyMeleeOffensiveMobEffects()
                || profile.directLivingAttack()) {
            return DamageApplicationBucket.VANILLA_MELEE_BASE;
        }

        /*
         * Non-offensive vanilla damage:
         * fall, fire tick, magic, explosion, environmental, etc.
         *
         * With the current preMultiplierBucketId table there is no VANILLA_OTHER_BASE.
         * DN_RULE_BASE is acceptable as a mitigated, non-cooldown, non-crit preMultiplierBucketId.
         */
        return DamageApplicationBucket.DN_RULE_BASE;
    }

    private static DamageApplicationBucket offensiveEnchantmentBucket(
            VanillaDamageSourceProfile profile
    ) {
        if (profile != null && profile.projectile()) {
            return DamageApplicationBucket.VANILLA_PROJECTILE_ENCHANTMENT;
        }

        return DamageApplicationBucket.VANILLA_MELEE_ENCHANTMENT;
    }
}