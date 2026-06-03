package io.github.naimjeg.damagenexus.builtin.bridge;

import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.DamageProcessorPriorities;
import io.github.naimjeg.damagenexus.api.display.DamageContributionDescriptor;
import io.github.naimjeg.damagenexus.api.enums.DamageApplicationBucket;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.bridge.vanilla.VanillaDamageCapture;
import io.github.naimjeg.damagenexus.core.pipeline.DamageMutationResult;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import net.minecraft.resources.Identifier;

public final class VanillaProjectileCriticalBridgeProcessor
        implements DamagePhaseProcessor {

    private static final float EPSILON = 0.0001f;
    private static final String TRACE_ID =
            "vanilla:projectile_critical_bonus";

    @Override
    public boolean canHandle(DamageNexusContext ctx) {
        if (!ctx.shouldRebuildVanillaPreEventDelta()) {
            return false;
        }

        VanillaDamageCapture.OffensiveSnapshot snapshot =
                ctx.getVanillaSnapshot();

        return snapshot != null
                && Float.isFinite(snapshot.projectileCriticalBonus())
                && snapshot.projectileCriticalBonus() > EPSILON;
    }

    @Override
    public void apply(DamageNexusContext ctx) {
        VanillaDamageCapture.OffensiveSnapshot snapshot =
                ctx.getVanillaSnapshot();

        DamageApplicationBucket bucket =
                DamageApplicationBucket.VANILLA_PROJECTILE_CRIT_BONUS;

        float bonus = snapshot.projectileCriticalBonus();

        DamageMutationResult result = ctx.tryAddVanillaCriticalBonusDamage(
                ctx.getInitialChannel(),
                bucket,
                bonus,
                TRACE_ID
        );

        ctx.contributions().record(
                result,
                () -> DamageContributionDescriptor.vanillaBase(
                        Identifier.fromNamespaceAndPath(
                                DamageNexus.MODID,
                                "vanilla_projectile_critical_bonus"
                        ),
                        DamagePhase.CRITICAL_HIT,
                        ctx.getInitialChannel().id(),
                        bucket,
                        bonus,
                        TRACE_ID
                )
        );
    }
    @Override
    public DamagePhase getPhase() {
        return DamagePhase.CRITICAL_HIT;
    }

    @Override
    public int getPriority() {
        return DamageProcessorPriorities.VANILLA_PROJECTILE_CRITICAL_BONUS;
    }
}