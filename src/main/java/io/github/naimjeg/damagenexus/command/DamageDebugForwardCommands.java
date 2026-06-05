package io.github.naimjeg.damagenexus.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.naimjeg.damagenexus.diagnostics.logging.ClientDebugLogReceiverGuard;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class DamageDebugForwardCommands {

    private DamageDebugForwardCommands() {
    }

    public static void register(
            LiteralArgumentBuilder<CommandSourceStack> root
    ) {
        root.then(tree());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> tree() {
        return Commands.literal("debug_forward")
                .then(Commands.literal("on")
                        .executes(ctx -> setReceive(ctx.getSource(), true)))
                .then(Commands.literal("off")
                        .executes(ctx -> setReceive(ctx.getSource(), false)))
                .then(Commands.literal("status")
                        .executes(ctx -> status(ctx.getSource())));
    }

    private static int setReceive(
            CommandSourceStack source,
            boolean enabled
    ) {
        return CommandFeedback.requirePlayer(source)
                .map(player -> {
                    ClientDebugLogReceiverGuard.setOptIn(player, enabled);
                    return CommandFeedback.silentSuccess(
                            source,
                            "DamageNexus debug forwarding receive = " + enabled
                    );
                })
                .orElse(0);
    }

    private static int status(CommandSourceStack source) {
        return CommandFeedback.requirePlayer(source)
                .map(player -> {
                    boolean enabled = ClientDebugLogReceiverGuard.isOptedIn(player);
                    return CommandFeedback.silentSuccess(
                            source,
                            "DamageNexus debug forwarding receive = " + enabled
                    );
                })
                .orElse(0);
    }
}
