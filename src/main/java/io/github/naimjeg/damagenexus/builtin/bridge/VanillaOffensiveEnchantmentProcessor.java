package io.github.naimjeg.damagenexus.builtin.bridge;

import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.bridge.vanilla.VanillaDamageCapture;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;

public final class VanillaOffensiveEnchantmentProcessor implements DamagePhaseProcessor {

    private static final float EPSILON = 0.0001f;
    private static final String TRACE_ID = "vanilla:offensive_enchantment";

    @Override
    public void apply(DamageNexusContext ctx) {
        VanillaDamageCapture.OffensiveSnapshot snapshot =
                ctx.getVanillaSnapshot();

        if (snapshot == null) {
            return;
        }

        float delta = snapshot.enchantDelta();

        if (!Float.isFinite(delta) || Math.abs(delta) <= EPSILON) {
            return;
        }

        ctx.addBaseDamage(
                ctx.getInitialChannel(),
                delta,
                TRACE_ID
        );
    }

    @Override
    public boolean canHandle(DamageNexusContext ctx) {
        VanillaDamageCapture.OffensiveSnapshot snapshot =
                ctx.getVanillaSnapshot();

        return ctx.shouldRebuildVanillaOffensiveEnchantment()
                && snapshot != null
                && snapshot.hasEnchantDelta();
    }

    @Override
    public DamagePhase getPhase() {
        return DamagePhase.BASE_MODIFICATION;
    }

    @Override
    public int getPriority() {
        return 1000;
    }
}