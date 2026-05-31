package io.github.naimjeg.damagenexus.builtin.bridge;

import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.DamageProcessorPriorities;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;

public final class VanillaOffensiveMobEffectProcessor implements DamagePhaseProcessor {

    private static final float EPSILON = 0.0001f;
    private static final String TRACE_ID =
            "vanilla:mob_effect/strength_weakness";

    @Override
    public void apply(DamageNexusContext ctx) {
        float delta = ctx.getVanillaOffensiveMobEffectDelta();

        if (!Float.isFinite(delta) || Math.abs(delta) <= EPSILON) {
            return;
        }

        ctx.addBaseDamage(
                ctx.getInitialChannel(),
                ctx.getVanillaOffensiveMobEffectBucket(),
                delta,
                TRACE_ID
        );
    }

    @Override
    public boolean canHandle(DamageNexusContext ctx) {
        return ctx.shouldRebuildVanillaOffensiveMobEffects()
                && ctx.vanillaSourceProfile().shouldApplyMeleeOffensiveMobEffects()
                && Math.abs(ctx.getVanillaOffensiveMobEffectDelta()) > EPSILON;
    }

    @Override
    public DamagePhase getPhase() {
        return DamagePhase.BASE_MODIFICATION;
    }

    @Override
    public int getPriority() {
        return DamageProcessorPriorities.VANILLA_OFFENSIVE_MOB_EFFECT;
    }
}