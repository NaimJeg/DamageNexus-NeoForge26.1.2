package io.github.naimjeg.damagenexus.event.neoforge;

import com.mojang.logging.LogUtils;
import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.ModConfig;
import io.github.naimjeg.damagenexus.bridge.vanilla.*;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusPipeline;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.slf4j.Logger;

@EventBusSubscriber(modid = DamageNexus.MODID)
public class IncomingDamageHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ThreadLocal<Integer> RECURSION_DEPTH = ThreadLocal.withInitial(() -> 0);
    private static final int MAX_RECURSION_DEPTH = 5;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {

        if (ModConfig.isDebugMode()) {
            LOGGER.info("[DN-Incoming] Caught Damage: " + event.getSource().type().msgId());
        }

        int depth = RECURSION_DEPTH.get();
        if (depth > MAX_RECURSION_DEPTH) {
            return;
        }

        LivingEntity victim = event.getEntity();
        if (victim == null || victim.level().isClientSide()) return;

        Entity rawAttacker = event.getSource().getEntity();
        LivingEntity attacker = (rawAttacker instanceof LivingEntity le) ? le : null;

        try {
            RECURSION_DEPTH.set(depth + 1);

            VanillaDamageCapture.OffensiveSnapshot vanillaSnapshot =
                    VanillaDamageCapture.consumeOffensiveSnapshot(
                            event.getSource(),
                            victim,
                            event.getOriginalAmount()
                    );

            VanillaBridgeLogger.logSnapshot(vanillaSnapshot);

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

            if (ModConfig.isDebugMode()) {

                String preEventInfo = vanillaSnapshot == null
                        ? "no_snapshot"
                        : "kind=" + vanillaSnapshot.preEventDelta().kind()
                          + " postEnchant=" + vanillaSnapshot.preEventDelta().postEnchantDamage()
                          + " eventOriginal=" + vanillaSnapshot.preEventDelta().eventOriginalDamage()
                          + " ratio=" + vanillaSnapshot.preEventDelta().ratio()
                          + " delta=" + vanillaSnapshot.preEventDelta().delta()
                          + " reason=" + vanillaSnapshot.preEventDelta().reason();

                LOGGER.info(
                        "[DN-Incoming] Vanilla bridge plan. eventOriginal={} initialBase={} rebuildMobEffect={} rebuildEnchant={} rebuildPreEvent={} strengthDelta={} observedWeaknessDelta={} observedMobEffectDelta={} enabledMobEffectDelta={} preEvent=[{}] reason={}",
                        event.getOriginalAmount(),
                        bridgePlan.initialBaseAmount(),
                        bridgePlan.rebuildOffensiveMobEffects(),
                        bridgePlan.rebuildOffensiveEnchantment(),
                        bridgePlan.rebuildPreEventDelta(),
                        mobEffectBreakdown.strengthDelta(),
                        mobEffectBreakdown.weaknessDelta(),
                        mobEffectBreakdown.observedDelta(),
                        bridgePlan.offensiveMobEffectDelta(),
                        preEventInfo,
                        bridgePlan.reason()
                );
            }

            DamageNexusContext ctx = new DamageNexusContext(
                    event,
                    attacker,
                    victim,

                    bridgePlan.initialBaseAmount(),
                    vanillaSnapshot,

                    bridgePlan.rebuildOffensiveMobEffects(),
                    bridgePlan.rebuildOffensiveEnchantment(),
                    bridgePlan.rebuildPreEventDelta(),
                    bridgePlan.offensiveMobEffectDelta(),

                    bridgePlan.initialBaseBucket(),
                    bridgePlan.offensiveMobEffectBucket(),
                    bridgePlan.offensiveEnchantmentBucket()
            );

            DamageNexusPipeline.execute(ctx);

        } finally {
            VanillaDamageCapture.clear();
            RECURSION_DEPTH.set(depth);
        }
    }
}