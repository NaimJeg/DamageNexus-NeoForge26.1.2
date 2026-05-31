package io.github.naimjeg.damagenexus.builtin.bridge;

import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.DamageProcessorPriorities;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.core.DamageComponent;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public final class VanillaResistanceEffectProcessor implements DamagePhaseProcessor {

    @Override
    public void apply(DamageNexusContext ctx) {
        if (ctx.victim == null) return;

        if (ctx.source.is(DamageTypeTags.BYPASSES_EFFECTS)) return;
        if (ctx.source.is(DamageTypeTags.BYPASSES_RESISTANCE)) return;

        MobEffectInstance resistance = ctx.victim.getEffect(MobEffects.RESISTANCE);

        if (resistance == null) {
            return;
        }

        int level = resistance.getAmplifier() + 1;

        float reduction = Math.max(
                0.0f,
                Math.min(1.0f, level * 0.20f)
        );

        if (reduction <= 0.0f) {
            return;
        }

        for (int i = 0; i < ctx.getActiveComponentCount(); i++) {
            DamageComponent component = ctx.getActiveComponent(i);

            ctx.addChannelMitigation(
                    component.channel,
                    reduction,
                    "vanilla:mob_effect/resistance"
            );
        }
    }

    @Override
    public DamagePhase getPhase() {
        return DamagePhase.MITIGATION_SETUP;
    }

    @Override
    public int getPriority() {
        return DamageProcessorPriorities.VANILLA_RESISTANCE_EFFECT;
    }
}