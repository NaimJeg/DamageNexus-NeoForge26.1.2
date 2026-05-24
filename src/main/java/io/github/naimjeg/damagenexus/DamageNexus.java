package io.github.naimjeg.damagenexus;

import io.github.naimjeg.damagenexus.core.registry.DamageModifierRegistry;
import io.github.naimjeg.damagenexus.registry.ModAttachments;
import io.github.naimjeg.damagenexus.registry.ModConstants;
import io.github.naimjeg.damagenexus.registry.ModModifiers;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusPipeline;
import io.github.naimjeg.damagenexus.registry.ModAttributes;
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

        ModAttributes.register(modEventBus);
        ModModifiers.register(modEventBus);
        ModAttachments.ATTACHMENTS.register(modEventBus);

        modContainer.registerConfig(
                net.neoforged.fml.config.ModConfig.Type.COMMON,
                ModConfig.SPEC
        );
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModConstants.register();
            DamageModifierRegistry.freeze();

            DamageNexusPipeline.clearCache();

            LOGGER.info(
                    "[DamageNexus] Modifier registry frozen with {} pre-modifier slots.",
                    DamageModifierRegistry.preModifierCount()
            );
        });
    }
}
