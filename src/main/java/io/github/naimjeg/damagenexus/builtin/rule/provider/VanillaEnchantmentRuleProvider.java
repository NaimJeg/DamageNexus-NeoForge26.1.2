package io.github.naimjeg.damagenexus.builtin.rule.provider;

import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.rule.*;
import io.github.naimjeg.damagenexus.builtin.rule.condition.AlwaysCondition;
import io.github.naimjeg.damagenexus.builtin.rule.operation.AddBaseDamageOperation;
import io.github.naimjeg.damagenexus.builtin.rule.operation.AddTemporaryResistanceOperation;
import io.github.naimjeg.damagenexus.core.DamageComponent;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.core.registry.DamageChannelRegistry;
import io.github.naimjeg.damagenexus.util.EnchantmentStackUtil;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;
import java.util.Optional;

public final class VanillaEnchantmentRuleProvider implements DamageRuleProvider {

    private static final Identifier PROTECTION_ID =
            Identifier.fromNamespaceAndPath("minecraft", "protection");

    private static final Identifier PROTECTION_STACKING_GROUP =
            Identifier.fromNamespaceAndPath("minecraft", "damage_protection");

    private static final Identifier SHARPNESS_ID =
            Identifier.fromNamespaceAndPath("minecraft", "sharpness");

    @Override
    public void collect(
            DamageNexusContext ctx,
            DamagePhase phase,
            List<RuntimeDamageRule> out
    ) {
        switch (phase) {
            //case BASE_MODIFICATION -> collectSharpness(ctx, phase, out);
            case MITIGATION_SETUP -> collectProtection(ctx, phase, out);
            default -> {
            }
        }
    }

    private void collectProtection(
            DamageNexusContext ctx,
            DamagePhase phase,
            List<RuntimeDamageRule> out
    ) {
        if (ctx.victim == null) {
            return;
        }

        EnchantmentStackUtil.forEachArmorEnchantment(
                ctx.victim,
                (stack, enchantHolder, level) -> {
                    if (!hasId(enchantHolder, PROTECTION_ID)) {
                        return;
                    }

                    float scoreDelta = protectionScore(level);
                    float ratingDelta = protectionRating(scoreDelta);

                    ctx.debugger.logEnchantmentProtection(
                            PROTECTION_ID.toString(),
                            level,
                            scoreDelta,
                            ratingDelta
                    );

                    collectProtectionRulesForActiveChannels(
                            ctx,
                            phase,
                            out,
                            stack,
                            ratingDelta
                    );
                }
        );
    }

    private void collectSharpness(
            DamageNexusContext ctx,
            DamagePhase phase,
            List<RuntimeDamageRule> out
    ) {
        if (ctx.attacker == null) {
            return;
        }

        ItemStack stack = ctx.attacker.getMainHandItem();

        if (stack.isEmpty()) {
            return;
        }

        EnchantmentStackUtil.forEachEnchantment(
                stack,
                (sourceStack, enchantHolder, level) -> {
                    if (!hasId(enchantHolder, SHARPNESS_ID)) {
                        return;
                    }

                    float value = sharpnessDamage(level);

                    DamageRuleDefinition rule = new DamageRuleDefinition(
                            SHARPNESS_ID,
                            DamageRuleRole.OFFENSIVE,
                            DamagePhase.BASE_MODIFICATION,
                            500,
                            new DamageRuleDisplay(
                                    Optional.of("Sharpness"),
                                    Optional.of("Vanilla sharpness converted into physical base damage.")
                            ),
                            List.of(new AlwaysCondition()),
                            List.of(new AddBaseDamageOperation(
                                    DamageChannelRegistry.getPhysical(),
                                    value
                            )),
                            DamageRuleStacking.STACK,
                            Optional.of(SHARPNESS_ID),
                            Optional.of("Sharpness")
                    );

                    RuntimeDamageRule runtimeRule = new RuntimeDamageRule(
                            rule,
                            RuleExecutionContext.vanillaEnchantment(
                                    DamageRuleRole.OFFENSIVE,
                                    ctx.attacker,
                                    sourceStack,
                                    EquipmentSlot.MAINHAND
                            )
                    );

                    ctx.debugger.logRuleCollected(
                            phase,
                            rule,
                            runtimeRule.executionContext()
                    );

                    out.add(runtimeRule);
                }
        );
    }

    private void collectProtectionRulesForActiveChannels(
            DamageNexusContext ctx,
            DamagePhase phase,
            List<RuntimeDamageRule> out,
            ItemStack stack,
            float ratingDelta
    ) {
        if (ratingDelta == 0.0f) {
            return;
        }

        for (int i = 0; i < ctx.getActiveComponentCount(); i++) {
            DamageComponent component = ctx.getActiveComponent(i);

            DamageRuleDefinition rule = new DamageRuleDefinition(
                    Identifier.fromNamespaceAndPath(
                            "minecraft",
                            "protection_" + component.channel.id().getPath()
                    ),
                    DamageRuleRole.DEFENSIVE,
                    DamagePhase.MITIGATION_SETUP,
                    500,
                    new DamageRuleDisplay(
                            Optional.of("Protection"),
                            Optional.of("Vanilla protection converted into temporary resistance.")
                    ),
                    List.of(new AlwaysCondition()),
                    List.of(new AddTemporaryResistanceOperation(
                            component.channel,
                            ratingDelta
                    )),
                    DamageRuleStacking.STACK,
                    Optional.of(PROTECTION_STACKING_GROUP),
                    Optional.of("Protection")
            );

            RuntimeDamageRule runtimeRule = new RuntimeDamageRule(
                    rule,
                    RuleExecutionContext.vanillaEnchantment(
                            DamageRuleRole.DEFENSIVE,
                            ctx.victim,
                            stack,
                            null
                    )
            );

            ctx.debugger.logRuleCollected(
                    phase,
                    rule,
                    runtimeRule.executionContext()
            );

            out.add(runtimeRule);
        }
    }

    private static float protectionScore(int level) {
        /*
         * Keep this identical to the old VanillaProtectionProcessor logic.
         * Current trace indicates Protection IV produces score_delta=4.000.
         */
        return level;
    }

    private static float protectionRating(float scoreDelta) {
        /*
         * Keep this identical to the old VanillaProtectionProcessor logic.
         * Current trace indicates score 4.000 -> temp_rating_delta 14.000.
         */
        return scoreDelta * 3.5f;
    }

    private static float sharpnessDamage(int level) {
        if (level <= 0) {
            return 0.0f;
        }

        return 0.5f * level + 0.5f;
    }


    private static boolean hasId(
            Holder<Enchantment> holder,
            Identifier id
    ) {
        return holder.unwrapKey()
                .map(key -> key.identifier().equals(id))
                .orElse(false);
    }
}