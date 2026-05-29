package io.github.naimjeg.damagenexus.builtin.bridge;

import io.github.naimjeg.damagenexus.ModConfig;
import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.core.DamageComponent;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import io.github.naimjeg.damagenexus.api.DamageProcessorPriorities;

public final class VanillaDamageProtectionProcessor implements DamagePhaseProcessor {

    @Override
    public void apply(DamageNexusContext ctx) {
        if (ctx.victim == null) return;
        if (!(ctx.victim.level() instanceof ServerLevel serverLevel)) return;

        if (ctx.source.is(DamageTypeTags.BYPASSES_EFFECTS)) return;
        if (ctx.source.is(DamageTypeTags.BYPASSES_ENCHANTMENTS)) return;

        float score = EnchantmentHelper.getDamageProtection(
                serverLevel,
                ctx.victim,
                ctx.source
        );

        if (score <= 0.0f) {
            return;
        }

        float rating = score * ModConfig.ratingPerProtScore;

        for (int i = 0; i < ctx.getActiveComponentCount(); i++) {
            DamageComponent component = ctx.getActiveComponent(i);

            ctx.addTemporaryResistance(
                    component.channel,
                    rating,
                    "vanilla:damage_protection"
            );
        }

        if (ctx.debugger.enabled()) {
            ctx.debugger.logEnchantmentProtection(
                    "vanilla:damage_protection",
                    0,
                    score,
                    rating
            );
        }
    }

    @Override
    public DamagePhase getPhase() {
        return DamagePhase.MITIGATION_SETUP;
    }

    @Override
    public int getPriority() {
        return DamageProcessorPriorities.VANILLA_DAMAGE_PROTECTION;
    }
}