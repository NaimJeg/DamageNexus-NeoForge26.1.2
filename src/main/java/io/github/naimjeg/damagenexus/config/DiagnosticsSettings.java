package io.github.naimjeg.damagenexus.config;

import io.github.naimjeg.damagenexus.diagnostics.logging.DamageNexusLogKind;

public record DiagnosticsSettings(
        DiagnosticDomain configuredMode,
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
                DiagnosticDomain.OFF,
                false,
                false,
                ServerDebugLogVerbosity.WARNINGS_ONLY,
                ClientDebugLogForwardMode.OFF,
                ClientDebugLogForwardVerbosity.WARNINGS_ONLY,
                20,
                true
        );
    }

    public DiagnosticDomain diagnosticDomain() {
        DiagnosticDomain configured = configuredMode == null
                ? DiagnosticDomain.OFF
                : configuredMode;

        return configured.max(legacyDiagnosticDomain());
    }

    public boolean postDamageDiagnosticsEnabled() {
        return transactionTrackingEnabled();
    }

    public boolean shouldForwardDebugLogsToClient() {
        return clientForwardMode != ClientDebugLogForwardMode.OFF
                && diagnosticDomain() != DiagnosticDomain.OFF;
    }

    public boolean shouldLogFullServerTrace() {
        return shouldEmitServer(DamageNexusLogKind.TRACE_DETAIL);
    }

    public boolean compatibilityDiagnosticsEnabled() {
        return diagnosticDomain().compatibilityEnabled();
    }

    public boolean summaryTraceEnabled() {
        return diagnosticDomain().summaryEnabled();
    }

    public boolean fullTraceEnabled() {
        return diagnosticDomain().fullTraceEnabled();
    }

    public boolean transactionTrackingEnabled() {
        return compatibilityDiagnosticsEnabled();
    }

    public boolean shouldEmitServer(DamageNexusLogKind kind) {
        DamageNexusLogKind effectiveKind = effectiveKind(kind);

        if (effectiveKind == DamageNexusLogKind.WARNING
                || effectiveKind == DamageNexusLogKind.LIFECYCLE) {
            return true;
        }

        if (!diagnosticDomain().allows(effectiveKind)) {
            return false;
        }

        if (configuredModeActive()) {
            return true;
        }

        return legacyServerVerbosityAllows(effectiveKind);
    }

    public boolean shouldForwardClient(DamageNexusLogKind kind) {
        DamageNexusLogKind effectiveKind = effectiveKind(kind);

        if (effectiveKind == DamageNexusLogKind.LIFECYCLE
                || clientForwardMode == ClientDebugLogForwardMode.OFF
                || diagnosticDomain() == DiagnosticDomain.OFF) {
            return false;
        }

        return diagnosticDomain().allows(effectiveKind)
                && clientVerbosityAllows(effectiveKind);
    }

    private DiagnosticDomain legacyDiagnosticDomain() {
        DiagnosticDomain domain = postDamageDiagnostics
                ? DiagnosticDomain.COMPATIBILITY
                : DiagnosticDomain.OFF;

        if (!debugMode) {
            return domain;
        }

        domain = domain.max(fromServerVerbosity(serverLogVerbosity));

        if (clientForwardMode != ClientDebugLogForwardMode.OFF) {
            domain = domain.max(fromClientVerbosity(clientForwardVerbosity));
        }

        return domain;
    }

    private boolean configuredModeActive() {
        return configuredMode != null && configuredMode != DiagnosticDomain.OFF;
    }

    private boolean legacyServerVerbosityAllows(DamageNexusLogKind kind) {
        return switch (serverLogVerbosity) {
            case WARNINGS_ONLY -> false;

            case SUMMARY -> kind == DamageNexusLogKind.COMPATIBILITY
                    || kind == DamageNexusLogKind.TRACE_SUMMARY;

            case FULL -> true;
        };
    }

    private static DiagnosticDomain fromServerVerbosity(
            ServerDebugLogVerbosity verbosity
    ) {
        if (verbosity == ServerDebugLogVerbosity.FULL) {
            return DiagnosticDomain.FULL_TRACE;
        }

        return DiagnosticDomain.SUMMARY;
    }

    private static DiagnosticDomain fromClientVerbosity(
            ClientDebugLogForwardVerbosity verbosity
    ) {
        if (verbosity == ClientDebugLogForwardVerbosity.FULL) {
            return DiagnosticDomain.FULL_TRACE;
        }

        return DiagnosticDomain.SUMMARY;
    }

    private boolean clientVerbosityAllows(DamageNexusLogKind kind) {
        return switch (clientForwardVerbosity) {
            case WARNINGS_ONLY -> kind == DamageNexusLogKind.WARNING;

            case SUMMARY -> kind == DamageNexusLogKind.WARNING
                    || kind == DamageNexusLogKind.COMPATIBILITY
                    || kind == DamageNexusLogKind.TRACE_SUMMARY;

            case FULL -> kind != DamageNexusLogKind.LIFECYCLE;
        };
    }

    private static DamageNexusLogKind effectiveKind(DamageNexusLogKind kind) {
        return kind == null ? DamageNexusLogKind.TRACE_DETAIL : kind;
    }
}
