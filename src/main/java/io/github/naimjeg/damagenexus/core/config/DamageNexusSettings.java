package io.github.naimjeg.damagenexus.core.config;

import io.github.naimjeg.damagenexus.config.DamageNexusConfig;
import io.github.naimjeg.damagenexus.config.DamageNexusConfigValues;
import io.github.naimjeg.damagenexus.config.DiagnosticDomain;
import io.github.naimjeg.damagenexus.config.VanillaReductionCompatibilityMode;
import io.github.naimjeg.damagenexus.diagnostics.logging.DamageNexusLogKind;

public final class DamageNexusSettings {

    private DamageNexusSettings() {
    }

    public static DamageNexusConfigValues current() {
        return DamageNexusConfig.current();
    }

    public static boolean debugMode() {
        return current().diagnostics().debugMode();
    }

    public static boolean testCommandsEnabled() {
        return current().developer().testCommandsEnabled();
    }

    public static boolean strictProcessorErrors() {
        return current().developer().strictProcessorErrors();
    }

    public static boolean strictRuleErrors() {
        return current().developer().strictRuleErrors();
    }

    public static VanillaReductionCompatibilityMode vanillaReductionMode() {
        return current().vanillaCompatibility().mode();
    }

    public static boolean suppressVanillaArmorReduction() {
        return current().vanillaCompatibility().shouldSuppressArmor();
    }

    public static boolean suppressVanillaEnchantmentReduction() {
        return current().vanillaCompatibility().shouldSuppressEnchantments();
    }

    public static boolean suppressVanillaMobEffectReduction() {
        return current().vanillaCompatibility().shouldSuppressMobEffects();
    }

    public static boolean suppressVanillaInnateResistanceReduction() {
        return current().vanillaCompatibility().shouldSuppressInnateResistance();
    }

    public static boolean postDamageDiagnosticsEnabled() {
        return current().diagnostics().postDamageDiagnosticsEnabled();
    }

    public static DiagnosticDomain diagnosticDomain() {
        return current().diagnostics().diagnosticDomain();
    }

    public static boolean compatibilityDiagnosticsEnabled() {
        return current().diagnostics().compatibilityDiagnosticsEnabled();
    }

    public static boolean summaryTraceEnabled() {
        return current().diagnostics().summaryTraceEnabled();
    }

    public static boolean fullTraceEnabled() {
        return current().diagnostics().fullTraceEnabled();
    }

    public static boolean transactionTrackingEnabled() {
        return current().diagnostics().transactionTrackingEnabled();
    }

    public static boolean shouldEmitServer(DamageNexusLogKind kind) {
        return current().diagnostics().shouldEmitServer(kind);
    }

    public static boolean shouldForwardClient(DamageNexusLogKind kind) {
        return current().diagnostics().shouldForwardClient(kind);
    }

    public static boolean shouldEmitOrForward(DamageNexusLogKind kind) {
        return shouldEmitServer(kind) || shouldForwardClient(kind);
    }
}
