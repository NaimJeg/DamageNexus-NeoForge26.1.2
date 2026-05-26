package io.github.naimjeg.damagenexus.core.pipeline;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import io.github.naimjeg.damagenexus.ModConfig;
import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;

import com.mojang.logging.LogUtils;
import io.github.naimjeg.damagenexus.registry.ModDamageProcessors;
import org.slf4j.Logger;

public class DamageNexusPipeline {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<DamagePhase, List<DamagePhaseProcessor>> PHASE_PROCESSORS =
            new EnumMap<>(DamagePhase.class);

    private static boolean isBuilt = false;

    private static void buildPipeline() {
        if (isBuilt) return;

        for (DamagePhase phase : DamagePhase.values()) {
            PHASE_PROCESSORS.put(phase, new ArrayList<>());
        }

        if (ModDamageProcessors.PROCESSOR_REGISTRY != null) {
            for (DamagePhaseProcessor mod : ModDamageProcessors.PROCESSOR_REGISTRY) {
                DamagePhase phase = mod.getPhase();

                List<DamagePhaseProcessor> list = PHASE_PROCESSORS.get(phase);

                if (list == null) {
                    LOGGER.error("Modifier {} returned invalid phase {}", mod.getClass().getName(), phase);
                    continue;
                }

                list.add(mod);
            }
        }

        for (List<DamagePhaseProcessor> list : PHASE_PROCESSORS.values()) {
            list.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
        }

        isBuilt = true;

        if (ModConfig.isDebugMode()) {
            for (DamagePhase phase : DamagePhase.values()) {
                LOGGER.info("[DamageNexus] Pipeline phase {}:", phase);

                for (DamagePhaseProcessor modifier : PHASE_PROCESSORS.get(phase)) {
                    LOGGER.info(
                            "  - {} priority={}",
                            modifier.getClass().getSimpleName(),
                            modifier.getPriority()
                    );
                }
            }
        }
    }

    public static void clearCache() {
        isBuilt = false;
        PHASE_PROCESSORS.clear();
    }

    public static void execute(DamageNexusContext ctx) {
        if (!isBuilt) buildPipeline();
        if (!ctx.isManaged) return;

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
        if (processors == null || processors.isEmpty()) return;

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