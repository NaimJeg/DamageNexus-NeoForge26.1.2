package io.github.naimjeg.damagenexus.builtin.rule.provider;

import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.rule.*;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.ModDataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class ItemDamageRuleProvider implements DamageRuleProvider {

    @Override
    public void collect(
            DamageNexusContext ctx,
            DamagePhase phase,
            List<RuntimeDamageRule> out
    ) {
        collectWeaponRules(ctx, phase, out);
        collectArmorRules(ctx, phase, out);
    }

    private void collectWeaponRules(
            DamageNexusContext ctx,
            DamagePhase phase,
            List<RuntimeDamageRule> out
    ) {
        if (ctx.attacker == null) {
            return;
        }

        ItemStack mainHand = ctx.attacker.getMainHandItem();

        collectStackRules(
                ctx,
                phase,
                out,
                mainHand,
                RuleExecutionContext.weaponAffix(
                        ctx.attacker,
                        mainHand,
                        EquipmentSlot.MAINHAND
                )
        );

        ItemStack offHand = ctx.attacker.getOffhandItem();

        collectStackRules(
                ctx,
                phase,
                out,
                offHand,
                RuleExecutionContext.weaponAffix(
                        ctx.attacker,
                        offHand,
                        EquipmentSlot.OFFHAND
                )
        );
    }

    private void collectArmorRules(
            DamageNexusContext ctx,
            DamagePhase phase,
            List<RuntimeDamageRule> out
    ) {
        if (ctx.victim == null) {
            return;
        }

        collectArmorSlot(ctx, phase, out, EquipmentSlot.HEAD);
        collectArmorSlot(ctx, phase, out, EquipmentSlot.CHEST);
        collectArmorSlot(ctx, phase, out, EquipmentSlot.LEGS);
        collectArmorSlot(ctx, phase, out, EquipmentSlot.FEET);
    }

    private void collectArmorSlot(
            DamageNexusContext ctx,
            DamagePhase phase,
            List<RuntimeDamageRule> out,
            EquipmentSlot slot
    ) {
        ItemStack stack = ctx.victim.getItemBySlot(slot);

        collectStackRules(
                ctx,
                phase,
                out,
                stack,
                RuleExecutionContext.armorAffix(
                        ctx.victim,
                        stack,
                        slot
                )
        );
    }

    private void collectStackRules(
            DamageNexusContext ctx,
            DamagePhase phase,
            List<RuntimeDamageRule> out,
            ItemStack stack,
            RuleExecutionContext exec
    ) {
        if (stack.isEmpty()) {
            return;
        }

        List<DamageRuleDefinition> rules =
                stack.getOrDefault(
                        ModDataComponents.DAMAGE_RULES.get(),
                        List.of()
                );

        if (rules.isEmpty()) {
            return;
        }

        for (DamageRuleDefinition rule : rules) {
            if (!DamageRuleValidator.validate(
                    rule,
                    "item_component/" + exec.providerType() + "/" + exec.equipmentSlot(),
                    DamageRuleValidator.Policy.WARN
            )) {
                continue;
            }

            /*
             * Phase mismatch here is normal filtering.
             * Do not log it, otherwise every phase will spam skip lines.
             */
            if (rule.phase() != phase) {
                continue;
            }

            if (!rule.role().canRunAs(exec.role())) {
                ctx.debugger.logRuleRoleMismatch(
                        phase,
                        rule,
                        exec
                );
                continue;
            }

            ctx.debugger.logRuleCollected(
                    phase,
                    rule,
                    exec
            );

            out.add(new RuntimeDamageRule(rule, exec));
        }
    }
}