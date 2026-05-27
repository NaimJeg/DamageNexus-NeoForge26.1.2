package io.github.naimjeg.damagenexus.event.neoforge;

import com.mojang.logging.LogUtils;
import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.ModConfig;
import io.github.naimjeg.damagenexus.bridge.vanilla.PreEventDeltaKind;
import io.github.naimjeg.damagenexus.bridge.vanilla.VanillaBridgeLogger;
import io.github.naimjeg.damagenexus.bridge.vanilla.VanillaBridgePlan;
import io.github.naimjeg.damagenexus.bridge.vanilla.VanillaDamageCapture;
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

            VanillaBridgePlan bridgePlan =
                    VanillaBridgePlan.from(event.getOriginalAmount(), vanillaSnapshot);

            if (ModConfig.isDebugMode()) {
                LOGGER.info(
                        "[DN-Incoming] Vanilla bridge plan. eventOriginal={} initialBase={} rebuildEnchant={} rebuildPreEvent={} reason={}",
                        event.getOriginalAmount(),
                        bridgePlan.initialBaseAmount(),
                        bridgePlan.rebuildOffensiveEnchantment(),
                        bridgePlan.rebuildPreEventDelta(),
                        bridgePlan.reason()
                );
            }

            DamageNexusContext ctx = new DamageNexusContext(
                    event,
                    attacker,
                    victim,
                    bridgePlan.initialBaseAmount(),
                    vanillaSnapshot,
                    bridgePlan.rebuildOffensiveEnchantment(),
                    bridgePlan.rebuildPreEventDelta()
            );

            DamageNexusPipeline.execute(ctx);

        } finally {
            VanillaDamageCapture.clear();
            RECURSION_DEPTH.set(depth);
        }
    }
}