package io.github.naimjeg.damagenexus.core.pipeline;

import com.mojang.logging.LogUtils;
import io.github.naimjeg.damagenexus.ModConfig;
import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.registry.DamagePhaseProcessorRegistry;
import io.github.naimjeg.damagenexus.registry.ModDamageProcessors;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class DamageNexusPipeline {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Map<DamagePhase, List<DamagePhaseProcessor>> PHASE_PROCESSORS =
            new EnumMap<>(DamagePhase.class);

    private static boolean isBuilt = false;

    private static int builtExternalProcessorVersion = -1;

    private static void buildPipeline() {
        int externalVersion = DamagePhaseProcessorRegistry.version();

        if (isBuilt && builtExternalProcessorVersion == externalVersion) {
            return;
        }

        PHASE_PROCESSORS.clear();

        for (DamagePhase phase : DamagePhase.values()) {
            PHASE_PROCESSORS.put(phase, new ArrayList<>());
        }

        if (ModDamageProcessors.PROCESSOR_REGISTRY != null) {
            for (DamagePhaseProcessor processor : ModDamageProcessors.PROCESSOR_REGISTRY) {
                addProcessor(processor, false);
            }
        }

        for (DamagePhaseProcessor processor : DamagePhaseProcessorRegistry.externalProcessors()) {
            addProcessor(processor, true);
        }

        for (List<DamagePhaseProcessor> list : PHASE_PROCESSORS.values()) {
            list.sort((a, b) -> Integer.compare(
                    b.getPriority(),
                    a.getPriority()
            ));
        }

        isBuilt = true;
        builtExternalProcessorVersion = externalVersion;

        if (ModConfig.isDebugMode()) {
            logPipelineLayout();
        }
    }

    private static void addProcessor(
            DamagePhaseProcessor processor,
            boolean external
    ) {
        if (processor == null) {
            return;
        }

        DamagePhase phase = processor.getPhase();

        List<DamagePhaseProcessor> list = PHASE_PROCESSORS.get(phase);

        if (list == null) {
            LOGGER.error(
                    "[DamageNexus] {} processor {} returned invalid phase {}",
                    external ? "External" : "Internal",
                    processor.getClass().getName(),
                    phase
            );
            return;
        }

        list.add(processor);
    }

    private static void logPipelineLayout() {
        for (DamagePhase phase : DamagePhase.values()) {
            LOGGER.info("[DamageNexus] Pipeline phase {}:", phase);

            for (DamagePhaseProcessor processor : PHASE_PROCESSORS.get(phase)) {
                LOGGER.info(
                        "  - {} priority={}",
                        processor.getClass().getSimpleName(),
                        processor.getPriority()
                );
            }
        }
    }

    public static void clearCache() {
        isBuilt = false;
        builtExternalProcessorVersion = -1;
        PHASE_PROCESSORS.clear();
    }

    public static void execute(DamageNexusContext ctx) {
        buildPipeline();

        if (!ctx.isManaged) {
            return;
        }

        runPhase(DamagePhase.BASE_MODIFICATION, ctx);
        runPhase(DamagePhase.TYPE_SCALING, ctx);
        runPhase(DamagePhase.CRITICAL_HIT, ctx);
        runPhase(DamagePhase.CONDITIONAL_MULTI, ctx);
        runPhase(DamagePhase.GLOBAL_ADJUSTMENT, ctx);

        ctx.finalizeOffensiveDamage();

        runPhase(DamagePhase.MITIGATION_SETUP, ctx);

        ctx.calculateDefensiveDamage();

        runPhase(DamagePhase.FINAL_OVERRIDE, ctx);

        ctx.applyIncomingDamageToEvent();
    }

    private static void runPhase(DamagePhase phase, DamageNexusContext ctx) {
        ctx.setCurrentProcessingPhase(phase);
        ctx.debugger.logPhase(phase);

        List<DamagePhaseProcessor> processors = PHASE_PROCESSORS.get(phase);

        if (processors == null || processors.isEmpty()) {
            return;
        }

        for (DamagePhaseProcessor processor : processors) {
            if (!processor.canHandle(ctx)) {
                ctx.debugger.logProcessorSkip(
                        phase,
                        processor
                );
                continue;
            }

            ctx.debugger.logProcessorRun(
                    phase,
                    processor
            );

            processor.apply(ctx);
        }
    }
}