package io.github.naimjeg.damagenexus.builtin.bridge;

import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.bridge.vanilla.PreEventDeltaKind;
import io.github.naimjeg.damagenexus.bridge.vanilla.VanillaDamageCapture;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.PreMultiplierBuckets;

public final class VanillaDifficultyScalingProcessor implements DamagePhaseProcessor {

    private static final String TRACE_ID = "vanilla:difficulty_scaling";

    @Override
    public void apply(DamageNexusContext ctx) {
        VanillaDamageCapture.OffensiveSnapshot snapshot = ctx.getVanillaSnapshot();

        if (snapshot == null) {
            return;
        }

        float ratio = snapshot.preEventDelta().ratio();

        if (!Float.isFinite(ratio)) {
            return;
        }

        float additiveMultiplier = ratio - 1.0f;

        if (Math.abs(additiveMultiplier) < 0.0001f) {
            return;
        }

        ctx.addGlobalPreMultiplier(
                PreMultiplierBuckets.VANILLA_DIFFICULTY,
                additiveMultiplier,
                TRACE_ID
        );
    }

    @Override
    public boolean canHandle(DamageNexusContext ctx) {
        VanillaDamageCapture.OffensiveSnapshot snapshot =
                ctx.getVanillaSnapshot();

        return ctx.shouldRebuildVanillaPreEventDelta()
                && snapshot != null
                && snapshot.preEventDelta().kind() == PreEventDeltaKind.DIFFICULTY_SCALING;
    }

    @Override
    public DamagePhase getPhase() {
        return DamagePhase.GLOBAL_ADJUSTMENT;
    }

    @Override
    public int getPriority() {
        return 1000;
    }
}