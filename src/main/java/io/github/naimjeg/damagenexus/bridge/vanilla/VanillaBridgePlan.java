package io.github.naimjeg.damagenexus.bridge.vanilla;

import org.jspecify.annotations.Nullable;

public record VanillaBridgePlan(
        float initialBaseAmount,
        boolean rebuildOffensiveEnchantment,
        boolean rebuildPreEventDelta,
        String reason
) {
    public static VanillaBridgePlan from(
            float eventOriginalAmount,
            VanillaDamageCapture.@org.jspecify.annotations.Nullable OffensiveSnapshot snapshot
    ) {
        if (snapshot == null) {
            return new VanillaBridgePlan(
                    eventOriginalAmount,
                    false,
                    false,
                    "no_snapshot"
            );
        }

        boolean hasEnchantDelta = snapshot.hasEnchantDelta();
        boolean hasPreEventDelta = snapshot.hasPreEventDelta();
        boolean preEventBridgeable =
                isBridgeablePreEventDelta(snapshot.preEventDelta().kind()) && isUsableRatio(snapshot);

        if (hasPreEventDelta && !preEventBridgeable) {
            return new VanillaBridgePlan(
                    eventOriginalAmount,
                    false,
                    false,
                    "fallback_unbridgeable_pre_event kind="
                            + snapshot.preEventDelta().kind()
            );
        }

        if (hasEnchantDelta) {
            return new VanillaBridgePlan(
                    snapshot.preEnchantDamage(),
                    true,
                    preEventBridgeable,
                    preEventBridgeable
                            ? "rebuild_enchant_and_pre_event"
                            : "rebuild_enchant"
            );
        }

        if (preEventBridgeable) {
            return new VanillaBridgePlan(
                    snapshot.postEnchantDamage(),
                    false,
                    true,
                    "rebuild_pre_event"
            );
        }

        return new VanillaBridgePlan(
                eventOriginalAmount,
                false,
                false,
                "no_rebuild_needed"
        );
    }

    private static boolean isUsableRatio(
            VanillaDamageCapture.OffensiveSnapshot snapshot
    ) {
        float ratio = snapshot.preEventDelta().ratio();

        return Float.isFinite(ratio)
                && Math.abs(ratio) > 0.0001f;
    }

    private static boolean isBridgeablePreEventDelta(
            PreEventDeltaKind kind
    ) {
        return switch (kind) {
            case DIFFICULTY_SCALING,
                 PLAYER_ATTACK_SCALING,
                 PROJECTILE_SCALING,
                 SPECIAL_ATTACK_SCALING,
                 SPEAR_STAB_SCALING,
                 SPEAR_CHARGE_SCALING,
                 SPEAR_ATTACK_SCALING -> true;

            case NONE,
                 UNKNOWN -> false;
        };
    }


}