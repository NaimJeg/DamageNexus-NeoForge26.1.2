package io.github.naimjeg.damagenexus.event.neoforge;

import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.ModConfig;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusPipeline;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

@EventBusSubscriber(modid = DamageNexus.MODID)
public class IncomingDamageHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ThreadLocal<Integer> RECURSION_DEPTH = ThreadLocal.withInitial(() -> 0);
    private static final int MAX_RECURSION_DEPTH = 5;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {

        if (ModConfig.isDebugMode()) { LOGGER.info("[DN-Incoming] Caught Damage: " + event.getSource().type().msgId()); }

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

            DamageNexusContext ctx = new DamageNexusContext(event, attacker, victim);
            DamageNexusPipeline.execute(ctx);

        } finally {
            RECURSION_DEPTH.set(depth);
        }
    }
}