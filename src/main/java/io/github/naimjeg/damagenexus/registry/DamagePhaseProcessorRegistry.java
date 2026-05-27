package io.github.naimjeg.damagenexus.registry;

import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DamagePhaseProcessorRegistry {

    private static final List<DamagePhaseProcessor> EXTERNAL_PROCESSORS =
            new ArrayList<>();

    private static int version = 0;

    private DamagePhaseProcessorRegistry() {}

    public static synchronized void registerExternal(
            DamagePhaseProcessor processor
    ) {
        Objects.requireNonNull(
                processor,
                "DamagePhaseProcessor cannot be null"
        );

        if (EXTERNAL_PROCESSORS.contains(processor)) {
            DamageNexus.LOGGER.warn(
                    "[DamageNexus] Ignoring duplicate external damage phase processor instance: {}",
                    processor.getClass().getName()
            );
            return;
        }

        EXTERNAL_PROCESSORS.add(processor);
        version++;

        DamageNexus.LOGGER.info(
                "[DamageNexus] Registered external damage phase processor: {} phase={} priority={}",
                processor.getClass().getName(),
                processor.getPhase(),
                processor.getPriority()
        );
    }

    public static synchronized List<DamagePhaseProcessor> externalProcessors() {
        return List.copyOf(EXTERNAL_PROCESSORS);
    }

    public static synchronized int version() {
        return version;
    }
}