package io.github.naimjeg.damagenexus.builtin.bridge;

import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.DamageProcessorPriorities;
import io.github.naimjeg.damagenexus.api.enums.DamageApplicationBucket;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.bridge.vanilla.PreEventDeltaKind;
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
        VanillaPreEventScalingBridge.applyApplicationPreMultiplierToAll(
                ctx,
                PreEventDeltaKind.PROJECTILE_SCALING,
                PreMultiplierBuckets.VANILLA_PROJECTILE,
                PreMultiplierBuckets.VANILLA_PROJECTILE_ID,
                TRACE_ID,
                true,
                DamageApplicationBucket.VANILLA_PROJECTILE_BASE,
                DamageApplicationBucket.VANILLA_PROJECTILE_ENCHANTMENT
        );
    }

    @Override
    public DamagePhase getPhase() {
        return DamagePhase.GLOBAL_ADJUSTMENT;
    }

    @Override
    public int getPriority() {
        return DamageProcessorPriorities.VANILLA_PROJECTILE_SCALING;
    }
}