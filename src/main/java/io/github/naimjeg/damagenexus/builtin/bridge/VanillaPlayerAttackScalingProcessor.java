package io.github.naimjeg.damagenexus.builtin.bridge;

import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.bridge.vanilla.PreEventDeltaKind;
import io.github.naimjeg.damagenexus.bridge.vanilla.VanillaDamageCapture;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.PreMultiplierBuckets;

public final class VanillaPlayerAttackScalingProcessor implements DamagePhaseProcessor {

    private static final float EPSILON = 0.0001f;
    private static final String TRACE_ID = "vanilla:player_attack_scaling";

    @Override
    public boolean canHandle(DamageNexusContext ctx) {
        VanillaDamageCapture.OffensiveSnapshot snapshot =
                ctx.getVanillaSnapshot();

        return ctx.shouldRebuildVanillaPreEventDelta()
                && snapshot != null
                && snapshot.preEventDelta().kind() == PreEventDeltaKind.PLAYER_ATTACK_SCALING;
    }

    @Override
    public void apply(DamageNexusContext ctx) {
        VanillaDamageCapture.OffensiveSnapshot snapshot =
                ctx.getVanillaSnapshot();

        if (snapshot == null) {
            return;
        }

        float ratio = snapshot.preEventDelta().ratio();

        if (!Float.isFinite(ratio)) {
            return;
        }

        /*
         * Safety guard:
         * PLAYER_ATTACK_SCALING should currently represent attack cooldown / attack strength
         * reduction, not vanilla critical or unknown positive bonus.
         */
        if (ratio > 1.0f + EPSILON) {
            return;
        }

        float additiveMultiplier = ratio - 1.0f;

        if (Math.abs(additiveMultiplier) <= EPSILON) {
            return;
        }

        ctx.addGlobalPreMultiplier(
                PreMultiplierBuckets.VANILLA_PLAYER_ATTACK,
                additiveMultiplier,
                TRACE_ID
        );
    }

    @Override
    public DamagePhase getPhase() {
        return DamagePhase.GLOBAL_ADJUSTMENT;
    }

    @Override
    public int getPriority() {
        return 970;
    }
}