package io.github.naimjeg.damagenexus.event.neoforge;

import com.mojang.logging.LogUtils;
import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.ModConfig;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.slf4j.Logger;

@EventBusSubscriber(modid = DamageNexus.MODID)
public class PostDamageHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onLivingDamagePost(LivingDamageEvent.Post event) {
        float finalDamage = event.getNewDamage();

        if (ModConfig.isDebugMode()
                && event.getSource().getEntity() instanceof LivingEntity) {
            LOGGER.info(
                    "[DN-POST] victim={} actual_damage={}",
                    event.getEntity().getName().getString(),
                    finalDamage
            );
        }
    }
}