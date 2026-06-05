package io.github.naimjeg.damagenexus.core.config;

import io.github.naimjeg.damagenexus.config.DamageNexusConfig;
import io.github.naimjeg.damagenexus.config.DamageNexusConfigValues;
import io.github.naimjeg.damagenexus.config.VanillaReductionCompatibilityMode;

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
}
