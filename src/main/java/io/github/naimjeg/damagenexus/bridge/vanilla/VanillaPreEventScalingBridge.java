package io.github.naimjeg.damagenexus.bridge.vanilla;

import io.github.naimjeg.damagenexus.api.enums.DamageApplicationBucket;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;

public final class VanillaPreEventScalingBridge {

    private static final float EPSILON = 0.0001f;

    private VanillaPreEventScalingBridge() {}

    public static boolean canApply(
            DamageNexusContext ctx,
            PreEventDeltaKind expectedKind,
            boolean allowPositiveRatio
    ) {
        if (!ctx.shouldRebuildVanillaPreEventDelta()) {
            return false;
        }

        VanillaDamageCapture.OffensiveSnapshot snapshot =
                ctx.getVanillaSnapshot();

        if (snapshot == null) {
            return false;
        }

        VanillaDamageCapture.PreEventDelta delta =
                snapshot.preEventDelta();

        if (delta.kind() != expectedKind) {
            return false;
        }

        float ratio = delta.ratio();

        if (!Float.isFinite(ratio)) {
            return false;
        }

        if (Math.abs(ratio - 1.0f) <= EPSILON) {
            return false;
        }

        /*
         * Player attack scaling currently means vanilla attack-cooldown /
         * attack-strength penalty. It must not absorb positive bonuses such as
         * crit, enchantment, mace, spear, or unknown modded scaling.
         */
        if (!allowPositiveRatio && ratio > 1.0f + EPSILON) {
            return false;
        }

        if (ratio < 0.0f) {
            return false;
        }

        return true;
    }

    public static void applyApplicationPreMultiplier(
            DamageNexusContext ctx,
            PreEventDeltaKind expectedKind,
            DamageApplicationBucket applicationBucket,
            int preMultiplierBucket,
            String traceId,
            boolean allowPositiveRatio
    ) {
        if (!canApply(
                ctx,
                expectedKind,
                allowPositiveRatio
        )) {
            return;
        }

        float value = multiplierDelta(ctx);

        if (value == 0.0f) {
            return;
        }

        ctx.addApplicationPreMultiplier(
                applicationBucket,
                preMultiplierBucket,
                value,
                traceId
        );
    }

    public static void applyApplicationPreMultiplierToAll(
            DamageNexusContext ctx,
            PreEventDeltaKind expectedKind,
            int preMultiplierBucket,
            String traceId,
            boolean allowPositiveRatio,
            DamageApplicationBucket... applicationBuckets
    ) {
        if (!canApply(
                ctx,
                expectedKind,
                allowPositiveRatio
        )) {
            return;
        }

        float value = multiplierDelta(ctx);

        if (value == 0.0f) {
            return;
        }

        for (DamageApplicationBucket applicationBucket : applicationBuckets) {
            ctx.addApplicationPreMultiplier(
                    applicationBucket,
                    preMultiplierBucket,
                    value,
                    traceId
            );
        }
    }

    private static float multiplierDelta(DamageNexusContext ctx) {
        VanillaDamageCapture.OffensiveSnapshot snapshot =
                ctx.getVanillaSnapshot();

        if (snapshot == null) {
            return 0.0f;
        }

        float ratio = snapshot.preEventDelta().ratio();

        if (!Float.isFinite(ratio)) {
            return 0.0f;
        }

        float value = ratio - 1.0f;

        return Math.abs(value) > EPSILON ? value : 0.0f;
    }

    public static void applyGlobalPreMultiplier(
            DamageNexusContext ctx,
            PreEventDeltaKind expectedKind,
            int bucket,
            String traceId,
            boolean allowPositiveRatio
    ) {
        if (!canApply(ctx, expectedKind, allowPositiveRatio)) {
            return;
        }

        VanillaDamageCapture.PreEventDelta delta =
                ctx.getVanillaSnapshot().preEventDelta();

        float additiveMultiplier = delta.ratio() - 1.0f;

        ctx.addGlobalPreMultiplier(
                bucket,
                additiveMultiplier,
                traceId
        );
    }

    public static String describe(
            DamageNexusContext ctx
    ) {
        VanillaDamageCapture.OffensiveSnapshot snapshot =
                ctx.getVanillaSnapshot();

        if (snapshot == null) {
            return "no_snapshot";
        }

        VanillaDamageCapture.PreEventDelta delta =
                snapshot.preEventDelta();

        return "kind=" + delta.kind()
                + " postEnchant=" + delta.postEnchantDamage()
                + " eventOriginal=" + delta.eventOriginalDamage()
                + " ratio=" + delta.ratio()
                + " delta=" + delta.delta()
                + " reason=" + delta.reason();
    }
}