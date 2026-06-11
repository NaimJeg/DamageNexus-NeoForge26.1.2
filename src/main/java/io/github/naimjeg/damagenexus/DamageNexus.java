package io.github.naimjeg.damagenexus;

import com.mojang.logging.LogUtils;
import io.github.naimjeg.damagenexus.api.event.DamageNexusRegisterEvent;
import io.github.naimjeg.damagenexus.config.DamageNexusConfig;
import io.github.naimjeg.damagenexus.core.config.DamageNexusSettings;
import io.github.naimjeg.damagenexus.core.lifecycle.DamageNexusLifecycle;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusPipeline;
import io.github.naimjeg.damagenexus.core.registry.PreMultiplierBucketRegistry;
import io.github.naimjeg.damagenexus.diagnostics.DamageNexusStartupSelfCheck;
import io.github.naimjeg.damagenexus.diagnostics.logging.DamageNexusLifecycleLog;
import io.github.naimjeg.damagenexus.registry.*;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleProviders;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(DamageNexus.MODID)
public class DamageNexus {
    public static final String MODID = "damagenexus";
    public static final Logger LOGGER = LogUtils.getLogger();

    public DamageNexus(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        modEventBus.addListener(DamageNexusConfig::onLoad);
        modEventBus.addListener(DamageNexusConfig::onReload);


        ModAttributes.register(modEventBus);
        ModDamageProcessors.register(modEventBus);
        ModAttachments.ATTACHMENTS.register(modEventBus);
        ModDataComponents.register(modEventBus);

        modContainer.registerConfig(
                net.neoforged.fml.config.ModConfig.Type.COMMON,
                DamageNexusConfig.SPEC
        );
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            DamageNexusConfig.bakeConfig();
            DamageNexusLifecycle.beginRegistering();
            PreMultiplierBuckets.register();

            NeoForge.EVENT_BUS.post(new DamageNexusRegisterEvent());

            PreMultiplierBucketRegistry.freeze();
            DamageNexusLifecycle.freezeRegistration();

            DamageRuleProviders.bootstrap();

            DamageNexusStartupSelfCheck.run();

            DamageNexusPipeline.clearCache();
            DamageNexusLifecycle.running();

            DamageNexusLifecycleLog.commonSetupComplete(
                    DamageNexusSettings.diagnosticDomain(),
                    DamageNexusSettings.debugMode(),
                    DamageNexusSettings.testCommandsEnabled(),
                    PreMultiplierBucketRegistry.bucketCount()
            );
        });
    }
}

