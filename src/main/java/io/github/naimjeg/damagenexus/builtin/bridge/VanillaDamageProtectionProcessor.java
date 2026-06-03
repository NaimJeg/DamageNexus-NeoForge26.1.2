package io.github.naimjeg.damagenexus.builtin.bridge;

import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.ModConfig;
import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.DamageProcessorPriorities;
import io.github.naimjeg.damagenexus.api.display.DamageContributionDescriptor;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.core.DamageComponent;
import io.github.naimjeg.damagenexus.core.pipeline.DamageMutationResult;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class VanillaDamageProtectionProcessor implements DamagePhaseProcessor {

    private static final String TRACE_ID = "vanilla:damage_protection";

    @Override
    public void apply(DamageNexusContext ctx) {
        if (ctx.victim() == null) return;
        if (!(ctx.victim().level() instanceof ServerLevel serverLevel)) return;

        if (ctx.source().is(DamageTypeTags.BYPASSES_EFFECTS)) return;
        if (ctx.source().is(DamageTypeTags.BYPASSES_ENCHANTMENTS)) return;

        float score = EnchantmentHelper.getDamageProtection(
                serverLevel,
                ctx.victim(),
                ctx.source()
        );

        if (score <= 0.0f) {
            return;
        }

        float rating = score * ModConfig.ratingPerProtScore;

        for (int i = 0; i < ctx.getActiveComponentCount(); i++) {
            DamageComponent component = ctx.getActiveComponent(i);

            DamageMutationResult result = ctx.tryAddTemporaryResistance(
                    component.channel,
                    rating,
                    TRACE_ID
            );

            ctx.contributions().record(
                    result,
                    () -> DamageContributionDescriptor.vanillaTemporaryResistance(
                            Identifier.fromNamespaceAndPath(
                                    DamageNexus.MODID,
                                    "vanilla_damage_protection/"
                                            + component.channel.id().getPath()
                            ),
                            DamagePhase.MITIGATION_SETUP,
                            component.channel.id(),
                            rating,
                            TRACE_ID
                    )
            );
        }

        if (ctx.trace().enabled()) {
            ctx.trace().calculation().enchantmentProtection(
                    TRACE_ID,
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