package io.github.naimjeg.damagenexus.config;

import io.github.naimjeg.damagenexus.ModConfig.ClientDebugLogForwardMode;
import io.github.naimjeg.damagenexus.ModConfig.ClientDebugLogForwardVerbosity;
import io.github.naimjeg.damagenexus.ModConfig.ServerDebugLogVerbosity;

public record DiagnosticsSettings(
        boolean debugMode,
        boolean postDamageDiagnostics,
        ServerDebugLogVerbosity serverLogVerbosity,
        ClientDebugLogForwardMode clientForwardMode,
        ClientDebugLogForwardVerbosity clientForwardVerbosity,
        int clientForwardMaxLinesPerTick,
        boolean clientForwardRequireReceiverOptIn
) {
    public static DiagnosticsSettings defaults() {
        return new DiagnosticsSettings(
                false,
                false,
                ServerDebugLogVerbosity.WARNINGS_ONLY,
                ClientDebugLogForwardMode.OFF,
                ClientDebugLogForwardVerbosity.WARNINGS_ONLY,
                20,
                true
        );
    }

    public boolean postDamageDiagnosticsEnabled() {
        return debugMode || postDamageDiagnostics;
    }

    public boolean shouldForwardDebugLogsToClient() {
        return debugMode && clientForwardMode != ClientDebugLogForwardMode.OFF;
    }

    public boolean shouldLogFullServerTrace() {
        return debugMode && serverLogVerbosity == ServerDebugLogVerbosity.FULL;
    }
}