package io.github.naimjeg.damagenexus.diagnostics.logging;

import com.mojang.logging.LogUtils;
import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.config.DamageNexusConfig;
import io.github.naimjeg.damagenexus.config.DiagnosticDomain;
import io.github.naimjeg.damagenexus.config.VanillaReductionCompatibilityMode;
import org.slf4j.Logger;

public final class DamageNexusLifecycleLog {

    private static final Logger LOGGER = LogUtils.getLogger();

    private DamageNexusLifecycleLog() {
    }

    public static void commonSetupComplete(
            DiagnosticDomain diagnosticDomain,
            boolean debugMode,
            boolean testCommandsEnabled,
            int preMultiplierBucketCount
    ) {
        LOGGER.info(
                "[DamageNexus] diagnosticsMode={}, legacyDebugMode={}, testCommands={}",
                diagnosticDomain,
                debugMode,
                testCommandsEnabled
        );

        LOGGER.info(
                "[DamageNexus] Damage processor registry frozen with {} pre-multiplier bucket slots.",
                preMultiplierBucketCount
        );
    }

    public static void configBaked(
            DiagnosticDomain diagnosticDomain,
            boolean debugMode,
            boolean enableTestCommands,
            boolean postDamageDiagnosticsEnabled,
            boolean strictProcessorErrors,
            boolean strictRuleErrors,
            VanillaReductionCompatibilityMode vanillaReductionCompatibilityMode,
            float asymptoticKValue,
            float resistanceKValue,
            float ratingPerProtScore
    ) {
        LOGGER.info(
                "[DamageNexus] Config baked: diagnosticsMode={}, debugMode={}, testCommands={}, postDamageDiagnostics={}, strictProcessorErrors={}, strictRuleErrors={}, vanillaReductionMode={}, ArmorK={}, ResK={}, ProtScoreRatio={}",
                diagnosticDomain,
                debugMode,
                enableTestCommands,
                postDamageDiagnosticsEnabled,
                strictProcessorErrors,
                strictRuleErrors,
                vanillaReductionCompatibilityMode,
                asymptoticKValue,
                resistanceKValue,
                ratingPerProtScore
        );
    }

    public static void startupSelfCheckPassed() {
        LOGGER.info("[DamageNexus] Startup self-check passed.");
    }

    public static void channelsLoaded(int channelCount) {
        LOGGER.info(
                "Successfully loaded {} DamageNexus channels.",
                channelCount
        );
    }

    public static void datapackRulesLoaded(int accepted, int rejected) {
        LOGGER.info(
                "[DamageNexus] Loaded {} global datapack damage rules. rejected={}",
                accepted,
                rejected
        );
    }

    public static void externalProcessorRegistered(DamagePhaseProcessor processor) {
        LOGGER.info(
                "[DamageNexus] Registered external damage phase processor: {} phase={} priority={}",
                processor.getClass().getName(),
                processor.phase(),
                processor.getPriority()
        );
    }

    public static void pipelinePhase(Object phase) {
        if (!DamageNexusConfig.current()
                .diagnostics()
                .shouldLogFullServerTrace()) {
            return;
        }

        LOGGER.info("[DamageNexus] Pipeline phase {}:", phase);
    }

    public static void pipelineProcessor(
            String processorName,
            int priority,
            String kind
    ) {
        if (!DamageNexusConfig.current()
                .diagnostics()
                .shouldLogFullServerTrace()) {
            return;
        }

        LOGGER.info(
                "  - {} priority={} kind={}",
                processorName,
                priority,
                kind
        );
    }

}

