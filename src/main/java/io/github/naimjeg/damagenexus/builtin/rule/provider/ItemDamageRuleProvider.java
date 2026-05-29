package io.github.naimjeg.damagenexus.builtin.rule.provider;

import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.rule.*;
import io.github.naimjeg.damagenexus.bridge.vanilla.VanillaDamageSourceProfile;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.ModDataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class ItemDamageRuleProvider implements DamageRuleProvider {

    @Override
    public void collect(
            DamageNexusContext ctx,
            DamagePhase phase,
            List<RuntimeDamageRule> out
    ) {
        collectAttackerEquipmentRules(ctx, phase, out);
        collectVictimEquipmentRules(ctx, phase, out);
    }

    private void collectAttackerEquipmentRules(
            DamageNexusContext ctx,
            DamagePhase phase,
            List<RuntimeDamageRule> out
    ) {
        if (ctx.attacker == null) {
            return;
        }

        /*
         * Projectile attacks use the projectile source provider. Do not also
         * collect the shooter's current hands here, otherwise bow/crossbow rules
         * can execute twice or use the wrong item state at hit time.
         */
        VanillaDamageSourceProfile profile = ctx.vanillaSourceProfile();

        if (profile != null && profile.projectile()) {
            return;
        }

        collectEquipmentSlot(
                ctx,
                phase,
                out,
                ctx.attacker,
                EquipmentSlot.MAINHAND,
                RuleSourceLocation.ATTACKER_MAINHAND,
                DamageRuleRole.OFFENSIVE
        );

        collectEquipmentSlot(
                ctx,
                phase,
                out,
                ctx.attacker,
                EquipmentSlot.OFFHAND,
                RuleSourceLocation.ATTACKER_OFFHAND,
                DamageRuleRole.OFFENSIVE
        );
    }

    private void collectVictimEquipmentRules(
            DamageNexusContext ctx,
            DamagePhase phase,
            List<RuntimeDamageRule> out
    ) {
        if (ctx.victim == null) {
            return;
        }

        collectEquipmentSlot(
                ctx,
                phase,
                out,
                ctx.victim,
                EquipmentSlot.HEAD,
                RuleSourceLocation.VICTIM_HEAD,
                DamageRuleRole.DEFENSIVE
        );

        collectEquipmentSlot(
                ctx,
                phase,
                out,
                ctx.victim,
                EquipmentSlot.CHEST,
                RuleSourceLocation.VICTIM_CHEST,
                DamageRuleRole.DEFENSIVE
        );

        collectEquipmentSlot(
                ctx,
                phase,
                out,
                ctx.victim,
                EquipmentSlot.LEGS,
                RuleSourceLocation.VICTIM_LEGS,
                DamageRuleRole.DEFENSIVE
        );

        collectEquipmentSlot(
                ctx,
                phase,
                out,
                ctx.victim,
                EquipmentSlot.FEET,
                RuleSourceLocation.VICTIM_FEET,
                DamageRuleRole.DEFENSIVE
        );
    }

    private void collectEquipmentSlot(
            DamageNexusContext ctx,
            DamagePhase phase,
            List<RuntimeDamageRule> out,
            LivingEntity owner,
            EquipmentSlot slot,
            RuleSourceLocation location,
            DamageRuleRole role
    ) {
        ItemStack stack = owner.getItemBySlot(slot);

        RuleExecutionContext exec =
                RuleExecutionContext.itemEquipment(
                        location,
                        role,
                        owner,
                        stack,
                        slot
                );

        collectStackRules(
                ctx,
                phase,
                out,
                stack,
                exec
        );
    }

    private void collectStackRules(
            DamageNexusContext ctx,
            DamagePhase phase,
            List<RuntimeDamageRule> out,
            ItemStack stack,
            RuleExecutionContext exec
    ) {
        if (stack == null || stack.isEmpty()) {
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

        List<DamageRuleDefinition> validRules =
                DamageRuleValidator.filterValid(
                        rules,
                        "item_damage_rules/"
                                + exec.sourceLocation().name().toLowerCase()
                                + "/"
                                + stack.getHoverName().getString()
                );

        for (DamageRuleDefinition rule : validRules) {
            if (rule.phase() != phase) {
                continue;
            }

            if (!rule.role().canRunAs(exec.role())) {
                continue;
            }

            RuntimeDamageRule runtimeRule =
                    new RuntimeDamageRule(rule, exec);

            ctx.debugger.logRuleCollected(
                    phase,
                    rule,
                    exec
            );

            out.add(runtimeRule);
        }
    }
}
