package io.github.naimjeg.damagenexus.diagnostics.logging;

import io.github.naimjeg.damagenexus.config.DamageNexusConfig;
import io.github.naimjeg.damagenexus.config.DiagnosticsSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.HashSet;
import java.util.Set;

public final class ClientDebugLogForwarder {

    private static long currentTick = Long.MIN_VALUE;
    private static int forwardedThisTick = 0;

    private ClientDebugLogForwarder() {
    }

    public static void forward(
            Entity attacker,
            Entity victim,
            String line,
            DamageNexusLogKind kind
    ) {
        if (!DamageNexusConfig.current()
                .diagnostics()
                .shouldForwardDebugLogsToClient()) {
            return;
        }

        if (!shouldForwardKind(kind)) {
            return;
        }

        MinecraftServer server = resolveServer(attacker, victim);

        if (server == null) {
            return;
        }

        forward(server, attacker, victim, line, kind);
    }

    public static void forward(
            MinecraftServer server,
            Entity attacker,
            Entity victim,
            String line,
            DamageNexusLogKind kind
    ) {
        if (server == null || line == null || line.isBlank()) {
            return;
        }

        if (!DamageNexusConfig.current()
                .diagnostics()
                .shouldForwardDebugLogsToClient()) {
            return;
        }

        if (!shouldForwardKind(kind)) {
            return;
        }

        String sanitizedLine = sanitize(line);
        Set<ServerPlayer> recipients = resolveRecipients(
                server,
                attacker,
                victim
        );

        if (recipients.isEmpty()) {
            return;
        }

        for (ServerPlayer player : recipients) {
            if (!tryConsumeBudget(server)) {
                return;
            }

            if (!ClientDebugLogReceiverGuard.canReceive(player)) {
                continue;
            }

            send(player, sanitizedLine);
        }
    }

    private static boolean shouldForwardKind(DamageNexusLogKind kind) {
        if (kind == null) {
            kind = DamageNexusLogKind.TRACE_DETAIL;
        }

        DiagnosticsSettings diagnostics =
                DamageNexusConfig.current().diagnostics();

        return switch (diagnostics.clientForwardVerbosity()) {
            case WARNINGS_ONLY -> kind == DamageNexusLogKind.WARNING;

            case SUMMARY -> kind == DamageNexusLogKind.WARNING
                    || kind == DamageNexusLogKind.TRACE_SUMMARY
                    || kind == DamageNexusLogKind.COMPATIBILITY;

            case FULL -> true;
        };
    }

    private static Set<ServerPlayer> resolveRecipients(
            MinecraftServer server,
            Entity attacker,
            Entity victim
    ) {
        Set<ServerPlayer> recipients = new HashSet<>();

        switch (DamageNexusConfig.current().diagnostics().clientForwardMode()) {
            case OFF -> {
                return recipients;
            }

            case INVOLVED_PLAYERS -> {
                addIfServerPlayer(recipients, attacker);
                addIfServerPlayer(recipients, victim);
            }

            case OPS -> {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    if (server.getPlayerList().isOp(player.nameAndId())) {
                        recipients.add(player);
                    }
                }
            }

            case ALL_PLAYERS -> recipients.addAll(server.getPlayerList().getPlayers());
        }

        return recipients;
    }

    private static void addIfServerPlayer(
            Set<ServerPlayer> recipients,
            Entity entity
    ) {
        if (entity instanceof ServerPlayer player) {
            recipients.add(player);
        }
    }

    private static MinecraftServer resolveServer(
            Entity attacker,
            Entity victim
    ) {
        if (attacker != null && attacker.level().getServer() != null) {
            return attacker.level().getServer();
        }

        if (victim != null && victim.level().getServer() != null) {
            return victim.level().getServer();
        }

        return null;
    }

    private static boolean tryConsumeBudget(MinecraftServer server) {
        long tick = server.getTickCount();

        if (tick != currentTick) {
            currentTick = tick;
            forwardedThisTick = 0;
        }

        if (forwardedThisTick >= DamageNexusConfig.current()
                .diagnostics()
                .clientForwardMaxLinesPerTick()) {
            return false;
        }

        forwardedThisTick++;
        return true;
    }

    private static void send(ServerPlayer player, String line) {
        player.sendSystemMessage(Component.literal("[DN] " + line));
    }

    private static String sanitize(String line) {
        return line
                .replace('\r', ' ')
                .replace('\n', ' ')
                .strip();
    }
}
