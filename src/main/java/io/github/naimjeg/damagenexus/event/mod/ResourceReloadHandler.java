package io.github.naimjeg.damagenexus.event.mod;

import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusPipeline;
import io.github.naimjeg.damagenexus.core.registry.DamageChannelRegistry;
import io.github.naimjeg.damagenexus.core.rule.DatapackDamageRuleReloadListener;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

@EventBusSubscriber(modid = DamageNexus.MODID)
public class ResourceReloadHandler {

    @SubscribeEvent
    public static void onAddServerReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(
                Identifier.fromNamespaceAndPath(DamageNexus.MODID, "channel_registry"),
                new DamageChannelRegistry()
        );

        event.addListener(
                Identifier.fromNamespaceAndPath(DamageNexus.MODID, "global_damage_rules"),
                new DatapackDamageRuleReloadListener()
        );

        DamageNexusPipeline.clearCache();
    }
}