package io.github.naimjeg.damagenexus.core.pipeline;

import io.github.naimjeg.damagenexus.bridge.vanilla.*;
import io.github.naimjeg.damagenexus.core.config.DamageNexusSettings;
import io.github.naimjeg.damagenexus.diagnostics.logging.VanillaBridgeDiagnosticsLog;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Factory for constructing DamageNexusContext instances from NeoForge damage events.
 *
 * <p>This class owns the vanilla bridge setup required before the pipeline can run:
 * attacker/victim extraction, vanilla offensive snapshot consumption, source profile
 * creation, mob-effect bridge analysis, bridge-plan construction, and diagnostics
 * logging.</p>
 *
 * <p>The event handler remains responsible for recursion protection and final
 * VanillaDamageCapture cleanup.</p>
 */
public final class DamageNexusContextFactory {

    private DamageNexusContextFactory() {
    }

    /**
     * Creates a context for a server-side LivingIncomingDamageEvent.
     *
     * @return a DamageNexusContext, or {@code null} when the event should not be
     * processed by DamageNexus.
     */
    public static DamageNexusContext tryCreate(
            LivingIncomingDamageEvent event
    ) {
        if (event == null) {
            return null;
        }

        LivingEntity victim = event.getEntity();
        if (victim == null || victim.level().isClientSide()) {
            return null;
        }

        if (!DamageSourcePolicy.shouldManage(event.getSource())) {
            return null;
        }

        if (DamageNexusSettings.fullTraceEnabled()) {
            VanillaBridgeDiagnosticsLog.incomingCaught(
                    event.getSource().type().msgId()
            );
        }

        Entity rawAttacker = event.getSource().getEntity();
        LivingEntity attacker = rawAttacker instanceof LivingEntity livingAttacker
                ? livingAttacker
                : null;

        return createServerContext(
                event,
                attacker,
                victim
        );
    }

    private static DamageNexusContext createServerContext(
            LivingIncomingDamageEvent event,
            LivingEntity attacker,
            LivingEntity victim
    ) {
        VanillaDamageCapture.OffensiveSnapshot vanillaSnapshot =
                VanillaDamageCapture.consumeOffensiveSnapshot(
                        event.getSource(),
                        victim,
                        event.getOriginalAmount()
                );

        if (DamageNexusSettings.fullTraceEnabled()) {
            VanillaBridgeLogger.logSnapshot(vanillaSnapshot);
        }

        VanillaDamageSourceProfile sourceProfile =
                VanillaDamageSourceProfile.create(
                        event.getSource(),
                        attacker,
                        victim
                );

        VanillaMobEffectBridge.OffensiveMobEffectBreakdown mobEffectBreakdown =
                VanillaMobEffectBridge.computeOffensiveBreakdown(sourceProfile);

        VanillaBridgePlan bridgePlan =
                VanillaBridgePlan.from(
                        event.getOriginalAmount(),
                        sourceProfile,
                        vanillaSnapshot,
                        mobEffectBreakdown.observedDelta(),
                        mobEffectBreakdown.enabledDelta()
                );

        if (DamageNexusSettings.fullTraceEnabled()) {
            VanillaBridgeDiagnosticsLog.bridgePlan(
                    event.getOriginalAmount(),
                    vanillaSnapshot,
                    mobEffectBreakdown,
                    bridgePlan
            );
        }

        return createFromBridgePlan(
                event,
                attacker,
                victim,
                sourceProfile,
                vanillaSnapshot,
                bridgePlan
        );
    }

    private static DamageNexusContext createFromBridgePlan(
            LivingIncomingDamageEvent event,
            LivingEntity attacker,
            LivingEntity victim,
            VanillaDamageSourceProfile sourceProfile,
            VanillaDamageCapture.OffensiveSnapshot vanillaSnapshot,
            VanillaBridgePlan bridgePlan
    ) {
        return new DamageNexusContext(DamageNexusContextSpec.of(
                event,
                attacker,
                victim,
                sourceProfile,
                bridgePlan.initialBaseAmount(),
                vanillaSnapshot,
                bridgePlan.rebuildOffensiveMobEffects(),
                bridgePlan.rebuildOffensiveEnchantment(),
                bridgePlan.rebuildPreEventDelta(),
                bridgePlan.offensiveMobEffectDelta(),
                bridgePlan.initialBaseBucket(),
                bridgePlan.offensiveMobEffectBucket(),
                bridgePlan.offensiveEnchantmentBucket()
        ));
    }
}


