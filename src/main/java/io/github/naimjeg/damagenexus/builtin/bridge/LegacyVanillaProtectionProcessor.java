package io.github.naimjeg.damagenexus.builtin.bridge;

import io.github.naimjeg.damagenexus.ModConfig;
import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.core.DamageComponent;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.util.EnchantmentStackUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.enchantment.Enchantment;
import org.apache.commons.lang3.mutable.MutableFloat;

public class LegacyVanillaProtectionProcessor implements DamagePhaseProcessor {

    @Override
    public void apply(DamageNexusContext ctx) {
        if (ctx.victim == null) return;
        if (!(ctx.victim.level() instanceof ServerLevel serverLevel)) return;



        MutableFloat protectionScore = new MutableFloat(0.0f);
        boolean[] immune = {false};

        float ratingPerScore = ModConfig.ratingPerProtScore;

        EnchantmentStackUtil.forEachArmorEnchantment(
                ctx.victim,
                (armorStack, enchantHolder, level) -> {
                    Enchantment enchantment = enchantHolder.value();

                    if (!immune[0]
                            && enchantment.isImmuneToDamage(
                            serverLevel,
                            level,
                            ctx.victim,
                            ctx.source
                    )) {
                        immune[0] = true;
                    }

                    float before = protectionScore.floatValue();

                    enchantment.modifyDamageProtection(
                            serverLevel,
                            level,
                            armorStack,
                            ctx.victim,
                            ctx.source,
                            protectionScore
                    );

                    float delta = protectionScore.floatValue() - before;

                    if (delta > 0.0f && ctx.debugger.enabled()) {
                        String enchantmentId = enchantHolder.unwrapKey()
                                .map(key -> key.identifier().toString())
                                .orElse("unknown");



                        ctx.debugger.logEnchantmentProtection(
                                enchantmentId,
                                level,
                                delta,
                                delta * ratingPerScore
                        );
                    }
                }
        );



        float score = protectionScore.floatValue();

        if (immune[0]) {
            float rating = score * ratingPerScore;

            for (int i = 0; i < ctx.getActiveComponentCount(); i++) {
                DamageComponent component = ctx.getActiveComponent(i);

                ctx.addTemporaryResistance(
                        component.channel,
                        rating,
                        "vanilla:damage_protection"
                );
            }

            return;
        }


        if (score <= 0.0f) {
            return;
        }

        float rating = score * ratingPerScore;

        for (int i = 0; i < ctx.getActiveComponentCount(); i++) {
            DamageComponent component = ctx.getActiveComponent(i);

            ctx.addTemporaryResistance(
                    component.channel,
                    rating,
                    "vanilla:damage_protection"
            );
        }
    }

    

    @Override
    public DamagePhase getPhase() {
        return DamagePhase.MITIGATION_SETUP;
    }

    @Override
    public int getPriority() {
        return 1005;
    }
}