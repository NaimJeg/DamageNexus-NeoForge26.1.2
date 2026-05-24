package io.github.naimjeg.damagenexus.builtin.bridge;

import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.DamageProcessorPriorities;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;

public final class VanillaInitialBaseDamageProcessor implements DamagePhaseProcessor {

    private static final float EPSILON = 0.0001f;

    @Override
    public void apply(DamageNexusContext ctx) {
        ctx.tryAddBaseDamage(
                ctx.getInitialChannel(),
                ctx.getInitialBaseBucket(),
                ctx.getInitialBaseAmount(),
                "vanilla:initial_base/" + ctx.getInitialBaseBucket().name().toLowerCase()
        );
    }

    @Override
    public boolean canHandle(DamageNexusContext ctx) {
        return ctx.isManaged()
                && Float.isFinite(ctx.getInitialBaseAmount())
                && ctx.getInitialBaseAmount() > EPSILON;
    }

    @Override
    public DamagePhase getPhase() {
        return DamagePhase.BASE_MODIFICATION;
    }

    @Override
    public int getPriority() {
        return DamageProcessorPriorities.VANILLA_INITIAL_BASE;
    }
}