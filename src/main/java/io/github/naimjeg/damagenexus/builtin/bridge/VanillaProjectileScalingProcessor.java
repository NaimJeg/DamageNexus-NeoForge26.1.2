package io.github.naimjeg.damagenexus.builtin.bridge;

import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.enums.DamageApplicationBucket;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.bridge.vanilla.PreEventDeltaKind;
import io.github.naimjeg.damagenexus.bridge.vanilla.VanillaDamageCapture;
import io.github.naimjeg.damagenexus.bridge.vanilla.VanillaPreEventScalingBridge;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.PreMultiplierBuckets;

public final class VanillaProjectileScalingProcessor implements DamagePhaseProcessor {

    private static final String TRACE_ID = "vanilla:projectile_scaling";

    @Override
    public boolean canHandle(DamageNexusContext ctx) {
        return VanillaPreEventScalingBridge.canApply(
                ctx,
                PreEventDeltaKind.PROJECTILE_SCALING,
                true
        );
    }

    @Override
    public void apply(DamageNexusContext ctx) {
        VanillaDamageCapture.PreEventDelta delta =
                ctx.getVanillaSnapshot().preEventDelta();

        float value = delta.ratio() - 1.0f;

        ctx.addApplicationPreMultiplier(
                DamageApplicationBucket.VANILLA_PROJECTILE_BASE,
                PreMultiplierBuckets.VANILLA_PROJECTILE,
                value,
                TRACE_ID
        );

        ctx.addApplicationPreMultiplier(
                DamageApplicationBucket.VANILLA_PROJECTILE_ENCHANTMENT,
                PreMultiplierBuckets.VANILLA_PROJECTILE,
                value,
                TRACE_ID
        );
    }

    @Override
    public DamagePhase getPhase() {
        return DamagePhase.GLOBAL_ADJUSTMENT;
    }

    @Override
    public int getPriority() {
        return 960;
    }
}