package io.github.naimjeg.damagenexus.builtin.bridge;

import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.DamageProcessorPriorities;
import io.github.naimjeg.damagenexus.api.display.DamageContributionDescriptor;
import io.github.naimjeg.damagenexus.api.enums.DamageApplicationBucket;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.bridge.vanilla.PreEventDeltaKind;
import io.github.naimjeg.damagenexus.bridge.vanilla.VanillaDamageCapture;
import io.github.naimjeg.damagenexus.core.pipeline.DamageMutationResult;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import net.minecraft.resources.Identifier;

import java.util.Locale;

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

        DamageApplicationBucket bucket =
                DamageApplicationBucket.VANILLA_WEAPON_SPECIAL;

        String traceId = traceId(delta.kind());

        DamageMutationResult result = ctx.tryAddVanillaBaseReconstructedDamage(
                ctx.getInitialChannel(),
                bucket,
                delta.delta(),
                traceId
        );

        ctx.contributions().record(
                result,
                () -> DamageContributionDescriptor.vanillaBase(
                        Identifier.fromNamespaceAndPath(
                                DamageNexus.MODID,
                                "vanilla_spear_bonus/"
                                        + delta.kind().name().toLowerCase(Locale.ROOT)
                        ),
                        DamagePhase.BASE_MODIFICATION,
                        ctx.getInitialChannel().id(),
                        bucket,
                        delta.delta(),
                        traceId
                )
        );
    }

    @Override
    public DamagePhase getPhase() {
        return DamagePhase.BASE_MODIFICATION;
    }

    @Override
    public int getPriority() {
        return DamageProcessorPriorities.VANILLA_WEAPON_SPECIAL_BONUS;
    }

    private static boolean isSpearBonus(PreEventDeltaKind kind) {
        return kind == PreEventDeltaKind.SPEAR_STAB_BONUS
                || kind == PreEventDeltaKind.SPEAR_CHARGE_BONUS
                || kind == PreEventDeltaKind.SPEAR_ATTACK_BONUS;
    }

    private static String traceId(PreEventDeltaKind kind) {
        return switch (kind) {
            case SPEAR_STAB_BONUS -> "vanilla:special/spear_stab";
            case SPEAR_CHARGE_BONUS -> "vanilla:special/spear_charge";
            case SPEAR_ATTACK_BONUS -> "vanilla:special/spear_attack";
            default -> throw new IllegalArgumentException(
                    "Unsupported spear bonus delta kind: " + kind
            );
        };
    }
}