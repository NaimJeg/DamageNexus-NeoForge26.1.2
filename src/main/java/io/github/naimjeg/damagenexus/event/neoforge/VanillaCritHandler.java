package io.github.naimjeg.damagenexus.event.neoforge;

import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.registry.ModAttachments;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;

@EventBusSubscriber(modid = DamageNexus.MODID)
public class VanillaCritHandler {

//    @SubscribeEvent
//    public static void onVanillaCriticalHit(CriticalHitEvent event) {
//        Player player = event.getEntity();
//
//        if (event.isVanillaCritical()) {
//            player.setData(ModAttachments.PENDING_JUMP_CRIT, true);
//        } else {
//            player.setData(ModAttachments.PENDING_JUMP_CRIT, false);
//        }
//
//        event.setDamageMultiplier(1.0f);
//    }

    public static final ThreadLocal<Integer> PENDING_CRIT_TARGET = ThreadLocal.withInitial(() -> -1);

    @SubscribeEvent
    public static void onVanillaCriticalHit(CriticalHitEvent event) {
        if (event.isVanillaCritical() && event.getTarget() != null) {
            PENDING_CRIT_TARGET.set(event.getTarget().getId());
        } else {
            PENDING_CRIT_TARGET.set(-1);
        }
        event.setDamageMultiplier(1.0f);
    }
}