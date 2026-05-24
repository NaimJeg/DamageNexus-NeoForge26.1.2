package io.github.naimjeg.damagenexus;

import io.github.naimjeg.damagenexus.config.*;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Compatibility facade.
 *
 * New code should prefer DamageNexusConfig.current().
 * Old code may continue using ModConfig.* during migration.
 */
public final class ModConfig {
    private ModConfig() {}

    public enum VanillaReductionCompatibilityMode {
        /**
         * DamageNexus fully owns final mitigation.
         *
         * Vanilla ARMOR, ENCHANTMENTS, MOB_EFFECTS, and INNATE_RESISTANCE
         * reductions are suppressed.
         */
        FULL_REPLACEMENT,

        /**
         * Use the four per-reduction suppression booleans.
         */
        CONFIGURABLE,

        /**
         * DamageNexus does not suppress vanilla reduction modifiers.
         *
         * This may cause double mitigation. Use only for compatibility testing.
         */
        COOPERATIVE
    }

    public enum ClientDebugLogForwardVerbosity {
        /**
         * Forward only warnings.
         */
        WARNINGS_ONLY,

        /**
         * Forward warnings plus transaction summaries and compatibility diagnostics.
         */
        SUMMARY,

        /**
         * Forward all DamageNexus debug lines.
         * Use only on private dev servers.
         */
        FULL
    }

    public enum ServerDebugLogVerbosity {
        /**
         * Write only warnings and suspicious states to the server log.
         * Recommended for public or long-running test servers.
         */
        WARNINGS_ONLY,

        /**
         * Write compact transaction summaries and compatibility diagnostics.
         */
        SUMMARY,

        /**
         * Write every DamageNexus trace line to the server log.
         * This is extremely noisy for lava, fire, suffocation, and mob farms.
         */
        FULL
    }

    public enum ClientDebugLogForwardMode {
        OFF,
        INVOLVED_PLAYERS,
        OPS,
        ALL_PLAYERS
    }

    public enum TooltipDebugLevel {
        OFF,

        /**
         * Show only affix-level debug sections.
         */
        AFFIX_SUMMARY,

        /**
         * Show affix-level debug and contained rule ids.
         */
        AFFIX_AND_RULES,

        /**
         * Show full debug details.
         */
        FULL
    }

    public static final ModConfigSpec SPEC = DamageNexusConfig.SPEC;

    // Legacy spec aliases.
    // These keep old direct references compiling if any code still uses them.
    public static final ModConfigSpec.BooleanValue ENABLE_TEST_COMMANDS =
            DeveloperConfigSpec.ENABLE_TEST_COMMANDS;
    public static final ModConfigSpec.BooleanValue STRICT_PROCESSOR_ERRORS =
            DeveloperConfigSpec.STRICT_PROCESSOR_ERRORS;
    public static final ModConfigSpec.BooleanValue STRICT_RULE_ERRORS =
            DeveloperConfigSpec.STRICT_RULE_ERRORS;

    public static final ModConfigSpec.BooleanValue DEBUG_MODE =
            DiagnosticsConfigSpec.DEBUG_MODE;
    public static final ModConfigSpec.BooleanValue ENABLE_POST_DAMAGE_DIAGNOSTICS =
            DiagnosticsConfigSpec.ENABLE_POST_DAMAGE_DIAGNOSTICS;
    public static final ModConfigSpec.EnumValue<ClientDebugLogForwardMode> CLIENT_DEBUG_LOG_FORWARD_MODE =
            DiagnosticsConfigSpec.CLIENT_DEBUG_LOG_FORWARD_MODE;
    public static final ModConfigSpec.IntValue CLIENT_DEBUG_LOG_FORWARD_MAX_LINES_PER_TICK =
            DiagnosticsConfigSpec.CLIENT_DEBUG_LOG_FORWARD_MAX_LINES_PER_TICK;
    public static final ModConfigSpec.EnumValue<ClientDebugLogForwardVerbosity> CLIENT_DEBUG_LOG_FORWARD_VERBOSITY =
            DiagnosticsConfigSpec.CLIENT_DEBUG_LOG_FORWARD_VERBOSITY;
    public static final ModConfigSpec.EnumValue<ServerDebugLogVerbosity> SERVER_DEBUG_LOG_VERBOSITY =
            DiagnosticsConfigSpec.SERVER_DEBUG_LOG_VERBOSITY;
    public static final ModConfigSpec.BooleanValue CLIENT_DEBUG_LOG_FORWARD_REQUIRE_RECEIVER_OPT_IN =
            DiagnosticsConfigSpec.CLIENT_DEBUG_LOG_FORWARD_REQUIRE_RECEIVER_OPT_IN;

    public static final ModConfigSpec.EnumValue<TooltipDebugLevel> TOOLTIP_DEBUG_LEVEL =
            TooltipConfigSpec.TOOLTIP_DEBUG_LEVEL;

    public static final ModConfigSpec.DoubleValue ASYMPTOTIC_K_VALUE =
            CombatFormulaConfigSpec.ASYMPTOTIC_K_VALUE;
    public static final ModConfigSpec.DoubleValue RESISTANCE_K_VALUE =
            CombatFormulaConfigSpec.RESISTANCE_K_VALUE;
    public static final ModConfigSpec.DoubleValue RATING_PER_PROT_SCORE =
            CombatFormulaConfigSpec.RATING_PER_PROT_SCORE;

    public static final ModConfigSpec.EnumValue<VanillaReductionCompatibilityMode> VANILLA_REDUCTION_COMPATIBILITY_MODE =
            VanillaCompatibilityConfigSpec.VANILLA_REDUCTION_COMPATIBILITY_MODE;
    public static final ModConfigSpec.BooleanValue SUPPRESS_VANILLA_ARMOR_REDUCTION =
            VanillaCompatibilityConfigSpec.SUPPRESS_VANILLA_ARMOR_REDUCTION;
    public static final ModConfigSpec.BooleanValue SUPPRESS_VANILLA_ENCHANTMENT_REDUCTION =
            VanillaCompatibilityConfigSpec.SUPPRESS_VANILLA_ENCHANTMENT_REDUCTION;
    public static final ModConfigSpec.BooleanValue SUPPRESS_VANILLA_MOB_EFFECT_REDUCTION =
            VanillaCompatibilityConfigSpec.SUPPRESS_VANILLA_MOB_EFFECT_REDUCTION;
    public static final ModConfigSpec.BooleanValue SUPPRESS_VANILLA_INNATE_RESISTANCE_REDUCTION =
            VanillaCompatibilityConfigSpec.SUPPRESS_VANILLA_INNATE_RESISTANCE_REDUCTION;

    // Legacy baked fields.
    // New code should not use these directly.
    public static volatile boolean debugMode = false;
    public static volatile boolean enableTestCommands = false;
    public static volatile boolean enablePostDamageDiagnostics = false;
    public static volatile float asymptoticKValue = 15.0f;
    public static volatile float resistanceKValue = 50.0f;
    public static volatile float ratingPerProtScore = 3.5f;
    public static volatile boolean strictProcessorErrors = false;
    public static volatile boolean strictRuleErrors = false;
    public static volatile boolean suppressVanillaArmorReduction = true;
    public static volatile boolean suppressVanillaEnchantmentReduction = true;
    public static volatile boolean suppressVanillaMobEffectReduction = true;
    public static volatile boolean suppressVanillaInnateResistanceReduction = true;
    public static volatile VanillaReductionCompatibilityMode vanillaReductionCompatibilityMode =
            VanillaReductionCompatibilityMode.FULL_REPLACEMENT;
    public static volatile ClientDebugLogForwardMode clientDebugLogForwardMode =
            ClientDebugLogForwardMode.OFF;
    public static volatile int clientDebugLogForwardMaxLinesPerTick = 20;
    public static volatile ClientDebugLogForwardVerbosity clientDebugLogForwardVerbosity =
            ClientDebugLogForwardVerbosity.WARNINGS_ONLY;
    public static volatile ServerDebugLogVerbosity serverDebugLogVerbosity =
            ServerDebugLogVerbosity.WARNINGS_ONLY;
    public static volatile boolean clientDebugLogForwardRequireReceiverOptIn = true;
    public static volatile boolean enableDebugTooltips = false;
    public static volatile TooltipDebugLevel tooltipDebugLevel = TooltipDebugLevel.OFF;

    public static void onLoad(final ModConfigEvent.Loading event) {
        DamageNexusConfig.onLoad(event);
    }

    public static void onReload(final ModConfigEvent.Reloading event) {
        DamageNexusConfig.onReload(event);
    }

    public static void bakeConfig() {
        DamageNexusConfig.bakeConfig();
    }

    public static void syncLegacyValues(DamageNexusConfigValues values) {
        debugMode = values.diagnostics().debugMode();
        enableTestCommands = values.developer().enableTestCommands();
        enablePostDamageDiagnostics = values.diagnostics().postDamageDiagnostics();

        asymptoticKValue = values.formulas().asymptoticKValue();
        resistanceKValue = values.formulas().resistanceKValue();
        ratingPerProtScore = values.formulas().ratingPerProtScore();

        strictProcessorErrors = values.developer().strictProcessorErrors();
        strictRuleErrors = values.developer().strictRuleErrors();

        vanillaReductionCompatibilityMode = values.vanillaCompatibility().mode();
        suppressVanillaArmorReduction = values.vanillaCompatibility().rawSuppressArmor();
        suppressVanillaEnchantmentReduction = values.vanillaCompatibility().rawSuppressEnchantments();
        suppressVanillaMobEffectReduction = values.vanillaCompatibility().rawSuppressMobEffects();
        suppressVanillaInnateResistanceReduction = values.vanillaCompatibility().rawSuppressInnateResistance();

        clientDebugLogForwardMode = values.diagnostics().clientForwardMode();
        clientDebugLogForwardMaxLinesPerTick = values.diagnostics().clientForwardMaxLinesPerTick();
        clientDebugLogForwardVerbosity = values.diagnostics().clientForwardVerbosity();
        serverDebugLogVerbosity = values.diagnostics().serverLogVerbosity();
        clientDebugLogForwardRequireReceiverOptIn = values.diagnostics().clientForwardRequireReceiverOptIn();

        tooltipDebugLevel = values.tooltips().debugLevel();
        enableDebugTooltips = values.tooltips().debugTooltipsEnabled();
    }

    public static DamageNexusConfigValues current() {
        return DamageNexusConfig.current();
    }

    public static boolean isDebugMode() {
        return current().diagnostics().debugMode();
    }

    public static boolean areTestCommandsEnabled() {
        return current().developer().enableTestCommands();
    }

    public static float getAsymptoticKValue() {
        return current().formulas().asymptoticKValue();
    }

    public static float getResistanceKValue() {
        return current().formulas().resistanceKValue();
    }

    public static float getRatingPerProtScore() {
        return current().formulas().ratingPerProtScore();
    }

    public static boolean strictProcessorErrors() {
        return current().developer().strictProcessorErrors();
    }

    public static boolean strictRuleErrors() {
        return current().developer().strictRuleErrors();
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

    public static VanillaReductionCompatibilityMode vanillaReductionCompatibilityMode() {
        return current().vanillaCompatibility().mode();
    }

    public static boolean postDamageDiagnosticsEnabled() {
        return current().diagnostics().postDamageDiagnosticsEnabled();
    }

    public static ClientDebugLogForwardMode clientDebugLogForwardMode() {
        return current().diagnostics().clientForwardMode();
    }

    public static int clientDebugLogForwardMaxLinesPerTick() {
        return current().diagnostics().clientForwardMaxLinesPerTick();
    }

    public static boolean shouldForwardDebugLogsToClient() {
        return current().diagnostics().shouldForwardDebugLogsToClient();
    }

    public static ClientDebugLogForwardVerbosity clientDebugLogForwardVerbosity() {
        return current().diagnostics().clientForwardVerbosity();
    }

    public static ServerDebugLogVerbosity serverDebugLogVerbosity() {
        return current().diagnostics().serverLogVerbosity();
    }

    public static boolean shouldLogFullServerTrace() {
        return current().diagnostics().shouldLogFullServerTrace();
    }

    public static boolean clientDebugLogForwardRequiresReceiverOptIn() {
        return current().diagnostics().clientForwardRequireReceiverOptIn();
    }

    public static TooltipDebugLevel tooltipDebugLevel() {
        return current().tooltips().debugLevel();
    }

    public static boolean debugTooltipsEnabled() {
        return current().tooltips().debugTooltipsEnabled();
    }

    public static boolean showAffixDebugTooltips() {
        return current().tooltips().showAffixDebugTooltips();
    }

    public static boolean showRuleDebugTooltips() {
        return current().tooltips().showRuleDebugTooltips();
    }

    public static boolean showFullTooltipTrace() {
        return current().tooltips().showFullTooltipTrace();
    }
}
