package io.github.naimjeg.damagenexus.diagnostics.logging;

import io.github.naimjeg.damagenexus.core.config.DamageNexusSettings;
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
        if (!shouldForwardKind(kind)) {
            return;
        }

        MinecraftServer server = resolveServer(attacker, victim);

        if (server == null) {
            return;
        }

        forward(server, attacker, victim, line, kind);
    }

    public static boolean shouldForward(
            Entity attacker,
            Entity victim,
            DamageNexusLogKind kind
    ) {
        if (!shouldForwardKind(kind)) {
            return false;
        }

        MinecraftServer server = resolveServer(attacker, victim);

        return server != null
                && !resolveEligibleRecipients(server, attacker, victim).isEmpty();
    }

    public static boolean shouldForward(DamageNexusLogKind kind) {
        return shouldForwardKind(kind);
    }

    public static void forwardAlreadyAccepted(
            Entity attacker,
            Entity victim,
            String line,
            DamageNexusLogKind kind
    ) {
        MinecraftServer server = resolveServer(attacker, victim);

        if (server != null) {
            forward(server, attacker, victim, line, kind);
        }
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

        if (!shouldForwardKind(kind)) {
            return;
        }

        Set<ServerPlayer> recipients = resolveEligibleRecipients(
                server,
                attacker,
                victim
        );

        if (recipients.isEmpty()) {
            return;
        }

        String sanitizedLine = sanitize(line);

        for (ServerPlayer player : recipients) {
            if (!tryConsumeBudget(server)) {
                return;
            }

            send(player, sanitizedLine);
        }
    }

    private static boolean shouldForwardKind(DamageNexusLogKind kind) {
        return DamageNexusSettings.shouldForwardClient(kind);
    }

    private static Set<ServerPlayer> resolveEligibleRecipients(
            MinecraftServer server,
            Entity attacker,
            Entity victim
    ) {
        Set<ServerPlayer> recipients =
                resolveRecipients(server, attacker, victim);

        recipients.removeIf(player -> player == null
                || player.isRemoved()
                || !ClientDebugLogReceiverGuard.canReceive(player));

        return recipients;
    }

    private static Set<ServerPlayer> resolveRecipients(
            MinecraftServer server,
            Entity attacker,
            Entity victim
    ) {
        Set<ServerPlayer> recipients = new HashSet<>();

        switch (DamageNexusSettings.current().diagnostics().clientForwardMode()) {
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

        if (forwardedThisTick >= DamageNexusSettings.current()
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
