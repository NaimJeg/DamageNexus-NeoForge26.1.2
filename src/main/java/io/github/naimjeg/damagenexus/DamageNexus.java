package io.github.naimjeg.damagenexus;

import io.github.naimjeg.damagenexus.core.registry.PreMultiplierBucketRegistry;
import io.github.naimjeg.damagenexus.registry.*;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusPipeline;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(DamageNexus.MODID)
public class DamageNexus {
    public static final String MODID = "damagenexus";
    public static final Logger LOGGER = LogUtils.getLogger();

    public DamageNexus(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        modEventBus.addListener(ModConfig::onLoad);
        modEventBus.addListener(ModConfig::onReload);


        ModAttributes.register(modEventBus);
        ModDamageProcessors.register(modEventBus);
        ModAttachments.ATTACHMENTS.register(modEventBus);
        ModDataComponents.register(modEventBus);

        modContainer.registerConfig(
                net.neoforged.fml.config.ModConfig.Type.COMMON,
                ModConfig.SPEC
        );
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModConfig.bakeConfig();
            PreMultiplierBuckets.register();
            PreMultiplierBucketRegistry.freeze();
            DamageNexusPipeline.clearCache();

            LOGGER.info(
                    "[DamageNexus] debugMode={}",
                    ModConfig.isDebugMode()
            );

            LOGGER.info(
                    "[DamageNexus] Damage processor registry frozen with {} pre-bucket slots.",
                    PreMultiplierBucketRegistry.bucketCount()
            );
        });
    }
}
