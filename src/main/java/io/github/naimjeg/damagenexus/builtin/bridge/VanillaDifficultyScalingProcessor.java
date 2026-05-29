package io.github.naimjeg.damagenexus.builtin.bridge;

import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.enums.DamageApplicationBucket;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.bridge.vanilla.PreEventDeltaKind;
import io.github.naimjeg.damagenexus.bridge.vanilla.VanillaPreEventScalingBridge;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.PreMultiplierBuckets;

public final class VanillaDifficultyScalingProcessor implements DamagePhaseProcessor {

    private static final String TRACE_ID = "vanilla:difficulty_scaling";

    @Override
    public boolean canHandle(DamageNexusContext ctx) {
        return VanillaPreEventScalingBridge.canApply(
                ctx,
                PreEventDeltaKind.DIFFICULTY_SCALING,
                true
        );
    }

    @Override
    public void apply(DamageNexusContext ctx) {
        VanillaPreEventScalingBridge.applyApplicationPreMultiplierToAll(
                ctx,
                PreEventDeltaKind.DIFFICULTY_SCALING,
                PreMultiplierBuckets.VANILLA_DIFFICULTY,
                "vanilla:difficulty_scaling",
                true,
                DamageApplicationBucket.VANILLA_MELEE_BASE,
                DamageApplicationBucket.VANILLA_MELEE_ENCHANTMENT,
                DamageApplicationBucket.VANILLA_WEAPON_SPECIAL,
                DamageApplicationBucket.VANILLA_PROJECTILE_BASE,
                DamageApplicationBucket.VANILLA_PROJECTILE_ENCHANTMENT,
                DamageApplicationBucket.VANILLA_PROJECTILE_CRIT_BONUS
        );
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