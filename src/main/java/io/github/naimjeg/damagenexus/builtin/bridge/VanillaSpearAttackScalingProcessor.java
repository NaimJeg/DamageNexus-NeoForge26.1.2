package io.github.naimjeg.damagenexus.builtin.bridge;

import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.bridge.vanilla.PreEventDeltaKind;
import io.github.naimjeg.damagenexus.bridge.vanilla.VanillaDamageCapture;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.PreMultiplierBuckets;

public final class VanillaSpearAttackScalingProcessor implements DamagePhaseProcessor {

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

        PreEventDeltaKind kind = snapshot.preEventDelta().kind();

        int bucket = switch (kind) {
            case SPEAR_STAB_SCALING -> PreMultiplierBuckets.VANILLA_SPEAR_STAB;
            case SPEAR_CHARGE_SCALING -> PreMultiplierBuckets.VANILLA_SPEAR_CHARGE;
            case SPEAR_ATTACK_SCALING -> PreMultiplierBuckets.VANILLA_SPEAR_ATTACK;
            default -> -1;
        };

        String traceId = switch (kind) {
            case SPEAR_STAB_SCALING -> "vanilla:spear_stab_scaling";
            case SPEAR_CHARGE_SCALING -> "vanilla:spear_charge_scaling";
            case SPEAR_ATTACK_SCALING -> "vanilla:spear_attack_scaling";
            default -> "vanilla:spear_unknown_scaling";
        };

        if (bucket < 0) {
            return;
        }

        ctx.addGlobalPreMultiplier(
                bucket,
                additiveMultiplier,
                traceId
        );
    }

    @Override
    public boolean canHandle(DamageNexusContext ctx) {
        VanillaDamageCapture.OffensiveSnapshot snapshot =
                ctx.getVanillaSnapshot();

        if (!ctx.shouldRebuildVanillaPreEventDelta()
                || snapshot == null) {
            return false;
        }

        return switch (snapshot.preEventDelta().kind()) {
            case SPEAR_STAB_SCALING,
                 SPEAR_CHARGE_SCALING,
                 SPEAR_ATTACK_SCALING -> true;
            default -> false;
        };
    }

    @Override
    public DamagePhase getPhase() {
        return DamagePhase.GLOBAL_ADJUSTMENT;
    }

    @Override
    public int getPriority() {
        return 980;
    }
}