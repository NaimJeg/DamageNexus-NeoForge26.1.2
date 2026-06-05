package io.github.naimjeg.damagenexus.event.neoforge;

import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.bridge.vanilla.VanillaDamageCapture;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContextFactory;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusPipeline;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = DamageNexus.MODID)
public class IncomingDamageHandler {

    private static final ThreadLocal<Integer> RECURSION_DEPTH =
            ThreadLocal.withInitial(() -> 0);

    private static final int MAX_RECURSION_DEPTH = 5;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        int depth = RECURSION_DEPTH.get();
        if (depth >= MAX_RECURSION_DEPTH) {
            return;
        }

        try {
            RECURSION_DEPTH.set(depth + 1);

            DamageNexusContext ctx =
                    DamageNexusContextFactory.tryCreate(event);

            if (ctx == null) {
                return;
            }

            DamageNexusPipeline.execute(ctx);

        } finally {
            VanillaDamageCapture.clear();
            RECURSION_DEPTH.set(depth);
        }
    }
}

