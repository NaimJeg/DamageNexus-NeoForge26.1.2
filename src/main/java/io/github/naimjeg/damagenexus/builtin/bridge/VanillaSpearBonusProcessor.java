package io.github.naimjeg.damagenexus.builtin.bridge;

import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.enums.DamageApplicationBucket;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.bridge.vanilla.PreEventDeltaKind;
import io.github.naimjeg.damagenexus.bridge.vanilla.VanillaDamageCapture;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;

public final class VanillaSpearBonusProcessor implements DamagePhaseProcessor {

    private static final float EPSILON = 0.0001f;

    @Override
    public boolean canHandle(DamageNexusContext ctx) {
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

        return isSpearBonus(delta.kind())
                && Float.isFinite(delta.delta())
                && Math.abs(delta.delta()) > EPSILON;
    }

    @Override
    public void apply(DamageNexusContext ctx) {
        VanillaDamageCapture.PreEventDelta delta =
                ctx.getVanillaSnapshot().preEventDelta();

        ctx.addVanillaReconstructedDamage(
                ctx.getInitialChannel(),
                DamageApplicationBucket.VANILLA_WEAPON_SPECIAL,
                delta.delta(),
                "vanilla:spear_bonus"
        );
    }

    @Override
    public DamagePhase getPhase() {
        return DamagePhase.BASE_MODIFICATION;
    }

    @Override
    public int getPriority() {
        return 985;
    }

    private static boolean isSpearBonus(PreEventDeltaKind kind) {
        return kind == PreEventDeltaKind.SPEAR_STAB_BONUS
                || kind == PreEventDeltaKind.SPEAR_CHARGE_BONUS
                || kind == PreEventDeltaKind.SPEAR_ATTACK_BONUS;
    }

    private static String traceId(PreEventDeltaKind kind) {
        return switch (kind) {
            case SPEAR_STAB_BONUS -> "vanilla:spear_stab_bonus";
            case SPEAR_CHARGE_BONUS -> "vanilla:spear_charge_bonus";
            case SPEAR_ATTACK_BONUS -> "vanilla:spear_attack_bonus";
            default -> "vanilla:spear_bonus";
        };
    }
}