package io.github.naimjeg.damagenexus.builtin.bridge;

import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.util.EnchantmentStackUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.enchantment.Enchantment;
import org.apache.commons.lang3.mutable.MutableFloat;

public class VanillaArmorEffectivenessProcessor implements DamagePhaseProcessor {

    @Override
    public void apply(DamageNexusContext ctx) {
        if (ctx.attacker == null || ctx.victim == null) return;
        if (!(ctx.victim.level() instanceof ServerLevel serverLevel)) return;

        MutableFloat armorEffectiveness = new MutableFloat(1.0f);

        EnchantmentStackUtil.forEachWeaponEnchantment(
                ctx.attacker,
                (weaponStack, enchantHolder, level) -> {
                    Enchantment enchantment = enchantHolder.value();

                    enchantment.modifyArmorEffectivness(
                            serverLevel,
                            level,
                            weaponStack,
                            ctx.victim,
                            ctx.source,
                            armorEffectiveness
                    );
                }
        );

        float multiplier = armorEffectiveness.floatValue();

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