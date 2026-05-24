package io.github.naimjeg.damagenexus.core.pipeline;

import com.mojang.logging.LogUtils;
import io.github.naimjeg.damagenexus.ModConfig;
import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.diagnostics.logging.DamageNexusLifecycleLog;
import io.github.naimjeg.damagenexus.registry.DamagePhaseProcessorRegistry;
import io.github.naimjeg.damagenexus.registry.ModDamageProcessors;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DamageNexusPipeline {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Map<DamagePhase, List<PipelineEntry>> PHASE_PROCESSORS =
            new EnumMap<>(DamagePhase.class);

    private static final Set<String> LOGGED_PROCESSOR_FAILURES =
            ConcurrentHashMap.newKeySet();

    private static boolean isBuilt = false;

    private static int builtExternalProcessorVersion = -1;

    private record PipelineEntry(
            DamagePhaseProcessor processor,
            boolean external
    ) {
        String name() {
            return processor.getClass().getName();
        }

        String kind() {
            return external ? "external" : "internal";
        }
    }

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

        for (List<PipelineEntry> list : PHASE_PROCESSORS.values()) {
            list.sort((a, b) -> Integer.compare(
                    safePriority(b),
                    safePriority(a)
            ));
        }

        isBuilt = true;
        builtExternalProcessorVersion = externalVersion;

        if (ModConfig.shouldLogFullServerTrace()) {
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

        DamagePhase phase;

        try {
            phase = processor.getPhase();
        } catch (Throwable throwable) {
            handleProcessorFailure(
                    "getPhase",
                    processor,
                    external,
                    throwable
            );
            return;
        }

        if (phase == null) {
            LOGGER.error(
                    "[DamageNexus] {} processor {} returned null phase",
                    external ? "External" : "Internal",
                    processor.getClass().getName()
            );
            return;
        }

        List<PipelineEntry> list = PHASE_PROCESSORS.get(phase);

        if (list == null) {
            LOGGER.error(
                    "[DamageNexus] {} processor {} returned invalid phase {}",
                    external ? "External" : "Internal",
                    processor.getClass().getName(),
                    phase
            );
            return;
        }

        list.add(new PipelineEntry(processor, external));
    }

    private static int safePriority(PipelineEntry entry) {
        try {
            return entry.processor().getPriority();
        } catch (Throwable throwable) {
            handleProcessorFailure(
                    "getPriority",
                    entry.processor(),
                    entry.external(),
                    throwable
            );

            return Integer.MIN_VALUE;
        }
    }

    private static void logPipelineLayout() {
        for (DamagePhase phase : DamagePhase.values()) {
            DamageNexusLifecycleLog.pipelinePhase(phase);

            for (PipelineEntry entry : PHASE_PROCESSORS.get(phase)) {
                DamageNexusLifecycleLog.pipelineProcessor(
                        entry.processor().getClass().getSimpleName(),
                        safePriority(entry),
                        entry.kind()
                );
            }
        }
    }

    public static void clearCache() {
        isBuilt = false;
        builtExternalProcessorVersion = -1;
        PHASE_PROCESSORS.clear();
        LOGGED_PROCESSOR_FAILURES.clear();
    }

    public static void execute(DamageNexusContext ctx) {
        buildPipeline();

        if (!ctx.isManaged()) {
            return;
        }

        runPhase(DamagePhase.BASE_MODIFICATION, ctx);
        if (finishIfCancelled(ctx)) return;

        runPhase(DamagePhase.TYPE_SCALING, ctx);
        if (finishIfCancelled(ctx)) return;

        runPhase(DamagePhase.CRITICAL_HIT, ctx);
        if (finishIfCancelled(ctx)) return;

        runPhase(DamagePhase.CONDITIONAL_MULTI, ctx);
        if (finishIfCancelled(ctx)) return;

        runPhase(DamagePhase.GLOBAL_ADJUSTMENT, ctx);
        if (finishIfCancelled(ctx)) return;

        ctx.finalizeOffensiveDamage();

        runPhase(DamagePhase.MITIGATION_SETUP, ctx);
        if (finishIfCancelled(ctx)) return;

        ctx.calculateDefensiveDamage();

        runPhase(DamagePhase.FINAL_OVERRIDE, ctx);
        if (finishIfCancelled(ctx)) return;

        ctx.applyIncomingDamageToEvent();
    }

    private static boolean finishIfCancelled(DamageNexusContext ctx) {
        if (!ctx.isDamageCancelled()) {
            return false;
        }

        ctx.applyCancelledDamageToEvent();
        return true;
    }

    private static void runPhase(DamagePhase phase, DamageNexusContext ctx) {
        ctx.setCurrentProcessingPhase(phase);
        ctx.trace().pipeline().phase(phase);

        List<PipelineEntry> processors = PHASE_PROCESSORS.get(phase);

        if (processors == null || processors.isEmpty()) {
            return;
        }

        for (PipelineEntry entry : processors) {
            DamagePhaseProcessor processor = entry.processor();

            if (!safeCanHandle(entry, phase, ctx)) {
                ctx.trace().pipeline().processorSkip(
                        phase,
                        processor
                );
                continue;
            }

            ctx.trace().pipeline().processorRun(
                    phase,
                    processor
            );

            safeApply(entry, phase, ctx);

            if (ctx.isDamageCancelled()) {
                return;
            }
        }
    }

    private static boolean safeCanHandle(
            PipelineEntry entry,
            DamagePhase phase,
            DamageNexusContext ctx
    ) {
        try {
            return entry.processor().canHandle(ctx);
        } catch (Throwable throwable) {
            handleProcessorFailure(
                    "canHandle/" + phase,
                    entry.processor(),
                    entry.external(),
                    throwable
            );

            return false;
        }
    }

    private static void safeApply(
            PipelineEntry entry,
            DamagePhase phase,
            DamageNexusContext ctx
    ) {
        try {
            entry.processor().apply(ctx);
        } catch (Throwable throwable) {
            handleProcessorFailure(
                    "apply/" + phase,
                    entry.processor(),
                    entry.external(),
                    throwable
            );
        }
    }

    private static void handleProcessorFailure(
            String stage,
            DamagePhaseProcessor processor,
            boolean external,
            Throwable throwable
    ) {
        if (ModConfig.strictProcessorErrors()) {
            if (throwable instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }

            if (throwable instanceof Error error) {
                throw error;
            }

            throw new IllegalStateException(
                    "[DamageNexus] Processor failure at " + stage
                            + ": " + processor.getClass().getName(),
                    throwable
            );
        }

        String processorName = processor == null
                ? "<null>"
                : processor.getClass().getName();

        String key = stage + "|" + external + "|" + processorName + "|"
                + throwable.getClass().getName();

        if (LOGGED_PROCESSOR_FAILURES.add(key)) {
            LOGGER.error(
                    "[DamageNexus] {} processor failed at stage={} processor={}. "
                            + "The processor will be skipped for this call. "
                            + "Set strictProcessorErrors=true to fail fast.",
                    external ? "External" : "Internal",
                    stage,
                    processorName,
                    throwable
            );
        }
    }
}
