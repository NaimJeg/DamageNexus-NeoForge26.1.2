package io.github.naimjeg.damagenexus.config;

import io.github.naimjeg.damagenexus.diagnostics.logging.DamageNexusLogKind;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiagnosticsSettingsTest {

    @Test
    void derivesCompatibilityFromLegacyPostDamageDiagnostics() {
        DiagnosticsSettings settings = settings(
                DiagnosticDomain.OFF,
                false,
                true,
                ServerDebugLogVerbosity.WARNINGS_ONLY,
                ClientDebugLogForwardMode.OFF,
                ClientDebugLogForwardVerbosity.WARNINGS_ONLY
        );

        assertEquals(DiagnosticDomain.COMPATIBILITY, settings.diagnosticDomain());
        assertTrue(settings.transactionTrackingEnabled());
    }

    @Test
    void derivesFullTraceFromLegacyDebugFullVerbosity() {
        DiagnosticsSettings settings = settings(
                DiagnosticDomain.OFF,
                true,
                false,
                ServerDebugLogVerbosity.FULL,
                ClientDebugLogForwardMode.OFF,
                ClientDebugLogForwardVerbosity.WARNINGS_ONLY
        );

        assertEquals(DiagnosticDomain.FULL_TRACE, settings.diagnosticDomain());
        assertTrue(settings.fullTraceEnabled());
    }

    @Test
    void explicitCompatibilityModeServerRoutingMatrix() {
        DiagnosticsSettings settings = settings(
                DiagnosticDomain.COMPATIBILITY,
                false,
                false,
                ServerDebugLogVerbosity.WARNINGS_ONLY,
                ClientDebugLogForwardMode.OFF,
                ClientDebugLogForwardVerbosity.WARNINGS_ONLY
        );

        assertTrue(settings.shouldEmitServer(DamageNexusLogKind.WARNING));
        assertTrue(settings.shouldEmitServer(DamageNexusLogKind.COMPATIBILITY));
        assertFalse(settings.shouldEmitServer(DamageNexusLogKind.TRACE_SUMMARY));
        assertFalse(settings.shouldEmitServer(DamageNexusLogKind.TRACE_DETAIL));
    }

    @Test
    void explicitSummaryModeServerRoutingMatrix() {
        DiagnosticsSettings settings = settings(
                DiagnosticDomain.SUMMARY,
                false,
                false,
                ServerDebugLogVerbosity.WARNINGS_ONLY,
                ClientDebugLogForwardMode.OFF,
                ClientDebugLogForwardVerbosity.WARNINGS_ONLY
        );

        assertTrue(settings.shouldEmitServer(DamageNexusLogKind.WARNING));
        assertTrue(settings.shouldEmitServer(DamageNexusLogKind.COMPATIBILITY));
        assertTrue(settings.shouldEmitServer(DamageNexusLogKind.TRACE_SUMMARY));
        assertFalse(settings.shouldEmitServer(DamageNexusLogKind.TRACE_DETAIL));
    }

    @Test
    void explicitFullTraceModeServerRoutingMatrix() {
        DiagnosticsSettings settings = settings(
                DiagnosticDomain.FULL_TRACE,
                false,
                false,
                ServerDebugLogVerbosity.WARNINGS_ONLY,
                ClientDebugLogForwardMode.OFF,
                ClientDebugLogForwardVerbosity.WARNINGS_ONLY
        );

        assertTrue(settings.shouldEmitServer(DamageNexusLogKind.WARNING));
        assertTrue(settings.shouldEmitServer(DamageNexusLogKind.COMPATIBILITY));
        assertTrue(settings.shouldEmitServer(DamageNexusLogKind.TRACE_SUMMARY));
        assertTrue(settings.shouldEmitServer(DamageNexusLogKind.TRACE_DETAIL));
    }

    @Test
    void offModeSuppressesNonWarningDiagnostics() {
        DiagnosticsSettings settings = DiagnosticsSettings.defaults();

        assertTrue(settings.shouldEmitServer(DamageNexusLogKind.WARNING));
        assertFalse(settings.shouldEmitServer(DamageNexusLogKind.COMPATIBILITY));
        assertFalse(settings.shouldEmitServer(DamageNexusLogKind.TRACE_SUMMARY));
        assertFalse(settings.shouldEmitServer(DamageNexusLogKind.TRACE_DETAIL));
    }

    @Test
    void legacyAliasServerRoutingStillHonorsLegacyVerbosityCap() {
        DiagnosticsSettings settings = settings(
                DiagnosticDomain.OFF,
                false,
                true,
                ServerDebugLogVerbosity.WARNINGS_ONLY,
                ClientDebugLogForwardMode.OFF,
                ClientDebugLogForwardVerbosity.WARNINGS_ONLY
        );

        assertEquals(DiagnosticDomain.COMPATIBILITY, settings.diagnosticDomain());
        assertFalse(settings.shouldEmitServer(DamageNexusLogKind.COMPATIBILITY));
        assertTrue(settings.transactionTrackingEnabled());
    }

    @Test
    void clientForwardingUsesModeAndForwardingVerbosity() {
        DiagnosticsSettings settings = settings(
                DiagnosticDomain.SUMMARY,
                false,
                false,
                ServerDebugLogVerbosity.WARNINGS_ONLY,
                ClientDebugLogForwardMode.ALL_PLAYERS,
                ClientDebugLogForwardVerbosity.SUMMARY
        );

        assertTrue(settings.shouldForwardClient(DamageNexusLogKind.WARNING));
        assertTrue(settings.shouldForwardClient(DamageNexusLogKind.COMPATIBILITY));
        assertTrue(settings.shouldForwardClient(DamageNexusLogKind.TRACE_SUMMARY));
        assertFalse(settings.shouldForwardClient(DamageNexusLogKind.TRACE_DETAIL));
    }

    @Test
    void clientWarningsOnlyForwardingSuppressesCompatibilityEvenWhenModeAllowsIt() {
        DiagnosticsSettings settings = settings(
                DiagnosticDomain.COMPATIBILITY,
                false,
                false,
                ServerDebugLogVerbosity.WARNINGS_ONLY,
                ClientDebugLogForwardMode.ALL_PLAYERS,
                ClientDebugLogForwardVerbosity.WARNINGS_ONLY
        );

        assertTrue(settings.shouldForwardClient(DamageNexusLogKind.WARNING));
        assertFalse(settings.shouldForwardClient(DamageNexusLogKind.COMPATIBILITY));
        assertFalse(settings.shouldForwardClient(DamageNexusLogKind.TRACE_DETAIL));
    }

    private static DiagnosticsSettings settings(
            DiagnosticDomain mode,
            boolean debugMode,
            boolean postDamageDiagnostics,
            ServerDebugLogVerbosity serverVerbosity,
            ClientDebugLogForwardMode forwardMode,
            ClientDebugLogForwardVerbosity forwardVerbosity
    ) {
        return new DiagnosticsSettings(
                mode,
                debugMode,
                postDamageDiagnostics,
                serverVerbosity,
                forwardMode,
                forwardVerbosity,
                20,
                true
        );
    }
}
