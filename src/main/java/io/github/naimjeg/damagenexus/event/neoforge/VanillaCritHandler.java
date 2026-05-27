package io.github.naimjeg.damagenexus.event.neoforge;

import io.github.naimjeg.damagenexus.DamageNexus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;

@EventBusSubscriber(modid = DamageNexus.MODID)
public final class VanillaCritHandler {

    private static final ThreadLocal<int[]> PENDING_CRIT_TARGET =
            ThreadLocal.withInitial(() -> new int[] {-1});

    private VanillaCritHandler() {}

    @SubscribeEvent
    public static void onVanillaCriticalHit(CriticalHitEvent event) {
        if (event.isVanillaCritical() && event.getTarget() != null) {
            setPendingTargetId(event.getTarget().getId());
        } else {
            clear();
        }

        event.setDamageMultiplier(1.0f);
    }

    public static int pendingTargetId() {
        return PENDING_CRIT_TARGET.get()[0];
    }

    public static void setPendingTargetId(int entityId) {
        PENDING_CRIT_TARGET.get()[0] = entityId;
    }

    public static void clear() {
        PENDING_CRIT_TARGET.get()[0] = -1;
    }
}
