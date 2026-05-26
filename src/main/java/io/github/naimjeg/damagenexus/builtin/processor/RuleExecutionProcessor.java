package io.github.naimjeg.damagenexus.builtin.processor;

import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleDefinition;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.rule.RuleExecutionContext;
import io.github.naimjeg.damagenexus.api.rule.RuntimeDamageRule;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.core.rule.DamageRuleExecutor;
import io.github.naimjeg.damagenexus.core.rule.DamageRuleStackingResolver;
import io.github.naimjeg.damagenexus.core.rule.DamageRuleStackingResult;
import io.github.naimjeg.damagenexus.core.rule.StackingTrace;
import io.github.naimjeg.damagenexus.registry.ModDataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RuleExecutionProcessor implements DamagePhaseProcessor {

    private final DamagePhase phase;

    public RuleExecutionProcessor(DamagePhase phase) {
        this.phase = phase;
    }

    @Override
    public void apply(DamageNexusContext ctx) {
        List<RuntimeDamageRule> rules = new ArrayList<>();

        collectWeaponAffixes(ctx, rules);
        collectArmorAffixes(ctx, rules);

        if (rules.isEmpty()) {
            return;
        }

        DamageRuleStackingResult result = DamageRuleStackingResolver.resolve(rules);
        rules = result.rules();

        if (ctx.debugger.enabled()) {
            for (StackingTrace trace : result.traces()) {
                ctx.debugger.logStackingDrop(trace);
            }
        }

        rules.sort(
                Comparator.comparingInt(
                        (RuntimeDamageRule rule) -> rule.definition().priority()
                ).reversed()
        );

        for (RuntimeDamageRule rule : rules) {
            DamageRuleExecutor.execute(ctx, phase, rule);
        }
    }

    private void collectWeaponAffixes(DamageNexusContext ctx, List<RuntimeDamageRule> out) {
        if (ctx.attacker == null) {
            return;
        }

        ItemStack mainHand = ctx.attacker.getMainHandItem();
        collectStackRules(
                ctx,
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
                out,
                offHand,
                RuleExecutionContext.weaponAffix(
                        ctx.attacker,
                        offHand,
                        EquipmentSlot.OFFHAND
                )
        );
    }

    private void collectArmorAffixes(DamageNexusContext ctx, List<RuntimeDamageRule> out) {
        if (ctx.victim == null) {
            return;
        }

        collectArmorSlot(ctx, out, EquipmentSlot.HEAD);
        collectArmorSlot(ctx, out, EquipmentSlot.CHEST);
        collectArmorSlot(ctx, out, EquipmentSlot.LEGS);
        collectArmorSlot(ctx, out, EquipmentSlot.FEET);
    }

    private void collectArmorSlot(DamageNexusContext ctx, List<RuntimeDamageRule> out, EquipmentSlot slot) {
        ItemStack stack = ctx.victim.getItemBySlot(slot);
        collectStackRules(
                ctx,
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
            List<RuntimeDamageRule> out,
            ItemStack stack,
            RuleExecutionContext exec
    ) {
        if (stack.isEmpty()) {
            return;
        }

        List<DamageRuleDefinition> rules = stack.getOrDefault(
                ModDataComponents.ITEM_DAMAGE_RULES.get(),
                List.of()
        );

        if (rules.isEmpty()) {
            return;
        }

        for (DamageRuleDefinition rule : rules) {
            if (rule.phase() != phase) {
//                ctx.debugger.logRulePhaseMismatch(
//                        phase,
//                        rule
//                );
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

    @Override
    public boolean canHandle(DamageNexusContext ctx) {
        return ctx.isManaged;
    }

    @Override
    public DamagePhase getPhase() {
        return phase;
    }

    @Override
    public int getPriority() {
        return switch (phase) {
            case MITIGATION_SETUP -> 1002;
            default -> 500;
        };
    }
}