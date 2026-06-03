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

public final class VanillaSpecialAttackScalingProcessor implements DamagePhaseProcessor {

    private static final float EPSILON = 0.0001f;
    private static final String TRACE_ID = "vanilla:special_attack_bonus";

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

        return delta.kind() == PreEventDeltaKind.SPECIAL_ATTACK_SCALING
                && Float.isFinite(delta.delta())
                && Math.abs(delta.delta()) > EPSILON;
    }

    @Override
    public void apply(DamageNexusContext ctx) {
        VanillaDamageCapture.PreEventDelta delta =
                ctx.getVanillaSnapshot().preEventDelta();

        DamageApplicationBucket bucket =
                DamageApplicationBucket.VANILLA_WEAPON_SPECIAL;

        DamageMutationResult result = ctx.tryAddVanillaBaseReconstructedDamage(
                ctx.getInitialChannel(),
                bucket,
                delta.delta(),
                TRACE_ID
        );

        ctx.contributions().record(
                result,
                () -> DamageContributionDescriptor.vanillaBase(
                        Identifier.fromNamespaceAndPath(
                                DamageNexus.MODID,
                                "vanilla_special_attack/"
                                        + bucket.name().toLowerCase(Locale.ROOT)
                        ),
                        DamagePhase.BASE_MODIFICATION,
                        ctx.getInitialChannel().id(),
                        bucket,
                        delta.delta(),
                        TRACE_ID
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
}