package io.github.naimjeg.damagenexus.bridge.vanilla;

import org.jspecify.annotations.Nullable;

public record VanillaBridgePlan(
        float initialBaseAmount,
        boolean rebuildOffensiveMobEffects,
        boolean rebuildOffensiveEnchantment,
        boolean rebuildPreEventDelta,
        float offensiveMobEffectDelta,
        String reason
) {
    private static final float EPSILON = 0.0001f;

    public static VanillaBridgePlan from(
            float eventOriginalAmount,
            VanillaDamageCapture.@Nullable OffensiveSnapshot snapshot,
            float offensiveMobEffectDelta
    ) {
        boolean hasMobEffectDelta =
                Float.isFinite(offensiveMobEffectDelta)
                        && Math.abs(offensiveMobEffectDelta) > EPSILON;

        if (snapshot == null) {
            if (hasMobEffectDelta) {
                return new VanillaBridgePlan(
                        removeMobEffectDelta(
                                eventOriginalAmount,
                                offensiveMobEffectDelta
                        ),
                        true,
                        false,
                        false,
                        offensiveMobEffectDelta,
                        "rebuild_mob_effect_no_snapshot"
                );
            }

            return new VanillaBridgePlan(
                    eventOriginalAmount,
                    false,
                    false,
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
                    false,
                    false,
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
                            offensiveMobEffectDelta
                    ),
                    hasMobEffectDelta,
                    true,
                    preEventBridgeable,
                    offensiveMobEffectDelta,
                    reason(hasMobEffectDelta, true, preEventBridgeable)
            );
        }

        if (preEventBridgeable) {
            return new VanillaBridgePlan(
                    removeMobEffectDelta(
                            snapshot.postEnchantDamage(),
                            offensiveMobEffectDelta
                    ),
                    hasMobEffectDelta,
                    false,
                    true,
                    offensiveMobEffectDelta,
                    reason(hasMobEffectDelta, false, true)
            );
        }

        if (hasMobEffectDelta) {
            return new VanillaBridgePlan(
                    removeMobEffectDelta(
                            eventOriginalAmount,
                            offensiveMobEffectDelta
                    ),
                    true,
                    false,
                    false,
                    offensiveMobEffectDelta,
                    reason(true, false, false)
            );
        }

        return new VanillaBridgePlan(
                eventOriginalAmount,
                false,
                false,
                false,
                0.0f,
                "no_rebuild_needed"
        );
    }

    private static float removeMobEffectDelta(
            float amount,
            float mobEffectDelta
    ) {
        float result = amount - mobEffectDelta;

        if (!Float.isFinite(result)) {
            return amount;
        }

        return Math.max(0.0f, result);
    }

    private static String reason(
            boolean mobEffect,
            boolean enchant,
            boolean preEvent
    ) {
        StringBuilder sb = new StringBuilder("rebuild");

        if (mobEffect) {
            sb.append("_mob_effect");
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
}