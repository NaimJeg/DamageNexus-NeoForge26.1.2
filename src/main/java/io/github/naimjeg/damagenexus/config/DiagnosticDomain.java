package io.github.naimjeg.damagenexus.config;

import io.github.naimjeg.damagenexus.diagnostics.logging.DamageNexusLogKind;

public enum DiagnosticDomain {
    OFF,
    COMPATIBILITY,
    SUMMARY,
    FULL_TRACE;

    public DiagnosticDomain max(DiagnosticDomain other) {
        if (other == null) {
            return this;
        }

        return ordinal() >= other.ordinal() ? this : other;
    }

    public boolean compatibilityEnabled() {
        return ordinal() >= COMPATIBILITY.ordinal();
    }

    public boolean summaryEnabled() {
        return ordinal() >= SUMMARY.ordinal();
    }

    public boolean fullTraceEnabled() {
        return this == FULL_TRACE;
    }

    public boolean allows(DamageNexusLogKind kind) {
        DamageNexusLogKind effectiveKind =
                kind == null ? DamageNexusLogKind.TRACE_DETAIL : kind;

        return switch (effectiveKind) {
            case WARNING, LIFECYCLE -> true;
            case COMPATIBILITY -> compatibilityEnabled();
            case TRACE_SUMMARY -> summaryEnabled();
            case TRACE_DETAIL -> fullTraceEnabled();
        };
    }
}
