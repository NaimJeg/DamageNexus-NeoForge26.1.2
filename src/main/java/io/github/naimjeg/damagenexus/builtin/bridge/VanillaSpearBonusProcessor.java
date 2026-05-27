package io.github.naimjeg.damagenexus.builtin.bridge;

import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.bridge.vanilla.PreEventDeltaKind;
import io.github.naimjeg.damagenexus.bridge.vanilla.VanillaDamageCapture;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;

public final class VanillaSpearBonusProcessor implements DamagePhaseProcessor {

    private static final float EPSILON = 0.0001f;

    @Override
    public void apply(DamageNexusContext ctx) {
        VanillaDamageCapture.OffensiveSnapshot snapshot =
                ctx.getVanillaSnapshot();

        if (snapshot == null) {
            return;
        }

        float speedBonusDamage = snapshot.preEventDelta().delta();

        if (!Float.isFinite(speedBonusDamage)
                || Math.abs(speedBonusDamage) <= EPSILON) {
            return;
        }

        PreEventDeltaKind kind = snapshot.preEventDelta().kind();

        String traceId = switch (kind) {
            case SPEAR_STAB_BONUS -> "vanilla:spear_stab_bonus";
            case SPEAR_CHARGE_BONUS -> "vanilla:spear_charge_bonus";
            case SPEAR_ATTACK_BONUS -> "vanilla:spear_attack_bonus";
            default -> "vanilla:spear_unknown_bonus";
        };

        ctx.addBaseDamage(
                ctx.getInitialChannel(),
                speedBonusDamage,
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
            case SPEAR_STAB_BONUS,
                 SPEAR_CHARGE_BONUS,
                 SPEAR_ATTACK_BONUS -> true;

            default -> false;
        };
    }

    @Override
    public DamagePhase getPhase() {
        return DamagePhase.BASE_MODIFICATION;
    }

    @Override
    public int getPriority() {
        return 990;
    }
}