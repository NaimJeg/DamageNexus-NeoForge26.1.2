package io.github.naimjeg.damagenexus.builtin.bridge;

import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.DamageProcessorPriorities;
import io.github.naimjeg.damagenexus.api.display.DamageContributionDescriptor;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.core.pipeline.DamageMutationResult;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class VanillaArmorEffectivenessProcessor implements DamagePhaseProcessor {

    private static final String TRACE_ID = "vanilla:armor_effectiveness";

    @Override
    public void apply(DamageNexusContext ctx) {
        if (ctx.attacker() == null || ctx.victim() == null) return;
        if (!(ctx.victim().level() instanceof ServerLevel serverLevel)) return;

        ItemStack weaponStack = ctx.attacker().getWeaponItem();

        if (weaponStack.isEmpty()) {
            return;
        }

        float rawMultiplier = EnchantmentHelper.modifyArmorEffectiveness(
                serverLevel,
                weaponStack,
                ctx.victim(),
                ctx.source(),
                1.0f
        );

        if (!Float.isFinite(rawMultiplier)) {
            return;
        }

        final float multiplier = Math.max(0.0f, rawMultiplier);

        if (multiplier != 1.0f) {
            DamageMutationResult result = ctx.tryMultiplyArmorEffectiveness(
                    multiplier,
                    TRACE_ID
            );

            ctx.contributions().record(
                    result,
                    () -> DamageContributionDescriptor.vanillaArmorEffectiveness(
                            Identifier.fromNamespaceAndPath(
                                    DamageNexus.MODID,
                                    "vanilla_armor_effectiveness"
                            ),
                            DamagePhase.MITIGATION_SETUP,
                            multiplier,
                            TRACE_ID
                    )
            );
        }
    }

    @Override
    public DamagePhase getPhase() {
        return DamagePhase.MITIGATION_SETUP;
    }

    @Override
    public int getPriority() {
        return DamageProcessorPriorities.VANILLA_ARMOR_EFFECTIVENESS;
    }
}