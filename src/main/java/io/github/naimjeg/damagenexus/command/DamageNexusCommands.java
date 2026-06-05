package io.github.naimjeg.damagenexus.command;

import com.mojang.brigadier.CommandDispatcher;
import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.config.DamageNexusConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = DamageNexus.MODID)
public final class DamageNexusCommands {

    private DamageNexusCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        var config = DamageNexusConfig.current();

        boolean debugMode = config.diagnostics().debugMode();
        boolean testCommands = config.developer().testCommandsEnabled();

        if (!debugMode && !testCommands) {
            return;
        }

        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        var root = Commands.literal("damagenexus");

        if (testCommands) {
            DamageTestCommands.register(root);
            DamageItemCommands.register(root);
            DamageDamageCommands.register(root);
            DamageBypassCommands.register(root);
        }

        if (debugMode) {
            DamageMobCommands.register(root);
            DamageEffectCommands.register(root);
            DamageAttributeCommands.register(root);
            DamageDebugForwardCommands.register(root);
            DamageCleanupCommands.register(root);
        }

        dispatcher.register(root);
    }
}
