package io.github.naimjeg.damagenexus.diagnostics.logging;

import io.github.naimjeg.damagenexus.ModConfig;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientDebugLogReceiverGuard {

    private static final Set<UUID> OPTED_IN =
            ConcurrentHashMap.newKeySet();

    private ClientDebugLogReceiverGuard() {}

    public static boolean canReceive(ServerPlayer player) {
        if (player == null) {
            return false;
        }

        if (!ModConfig.clientDebugLogForwardRequiresReceiverOptIn()) {
            return true;
        }

        return OPTED_IN.contains(player.getUUID());
    }

    public static boolean isOptedIn(ServerPlayer player) {
        return player != null && OPTED_IN.contains(player.getUUID());
    }

    public static void setOptIn(ServerPlayer player, boolean enabled) {
        if (player == null) {
            return;
        }

        if (enabled) {
            OPTED_IN.add(player.getUUID());
        } else {
            OPTED_IN.remove(player.getUUID());
        }
    }

    public static void clear(ServerPlayer player) {
        if (player != null) {
            OPTED_IN.remove(player.getUUID());
        }
    }
}