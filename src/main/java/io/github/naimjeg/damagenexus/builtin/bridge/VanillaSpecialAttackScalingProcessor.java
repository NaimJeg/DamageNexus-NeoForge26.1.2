package io.github.naimjeg.damagenexus.builtin.bridge;

import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.enums.DamageApplicationBucket;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.bridge.vanilla.PreEventDeltaKind;
import io.github.naimjeg.damagenexus.bridge.vanilla.VanillaPreEventScalingBridge;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.PreMultiplierBuckets;

public final class VanillaSpecialAttackScalingProcessor implements DamagePhaseProcessor {

    private static final String TRACE_ID = "vanilla:special_attack_scaling";

    @Override
    public boolean canHandle(DamageNexusContext ctx) {
        return VanillaPreEventScalingBridge.canApply(
                ctx,
                PreEventDeltaKind.SPECIAL_ATTACK_SCALING,
                true
        );
    }

    @Override
    public void apply(DamageNexusContext ctx) {
        VanillaPreEventScalingBridge.applyApplicationPreMultiplier(
                ctx,
                PreEventDeltaKind.SPECIAL_ATTACK_SCALING,
                DamageApplicationBucket.VANILLA_WEAPON_SPECIAL,
                PreMultiplierBuckets.VANILLA_SPECIAL_ATTACK,
                TRACE_ID,
                true
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