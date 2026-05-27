package io.github.naimjeg.damagenexus.builtin.bridge;

import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class VanillaArmorEffectivenessProcessor implements DamagePhaseProcessor {

    @Override
    public void apply(DamageNexusContext ctx) {
        if (ctx.attacker == null || ctx.victim == null) return;
        if (!(ctx.victim.level() instanceof ServerLevel serverLevel)) return;

        ItemStack weaponStack = ctx.attacker.getWeaponItem();

        if (weaponStack.isEmpty()) {
            return;
        }

        float multiplier = EnchantmentHelper.modifyArmorEffectiveness(
                serverLevel,
                weaponStack,
                ctx.victim,
                ctx.source,
                1.0f
        );

        if (Float.isNaN(multiplier) || Float.isInfinite(multiplier)) {
            return;
        }

        multiplier = Math.max(0.0f, multiplier);

        if (multiplier != 1.0f) {
            ctx.multiplyArmorEffectiveness(
                    multiplier,
                    "vanilla:armor_effectiveness"
            );
        }
    }

    @Override
    public DamagePhase getPhase() {
        return DamagePhase.MITIGATION_SETUP;
    }

    @Override
    public int getPriority() {
        return 1010;
    }
}