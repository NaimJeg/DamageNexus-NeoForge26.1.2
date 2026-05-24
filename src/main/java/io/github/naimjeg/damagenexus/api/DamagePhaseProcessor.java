package io.github.naimjeg.damagenexus.api;

import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;

public interface DamagePhaseProcessor {

    int PRIORITY_VANILLA = 0;
    int PRIORITY_MOD_STANDARD = 100;
    int PRIORITY_OVERRIDE = 1000;

    void apply(DamageNexusContext ctx);

    DamagePhase getPhase();

    default int getPriority() {
        return PRIORITY_MOD_STANDARD;
    }

    default boolean canHandle(DamageNexusContext ctx) {
        return ctx.isManaged();
    }

}