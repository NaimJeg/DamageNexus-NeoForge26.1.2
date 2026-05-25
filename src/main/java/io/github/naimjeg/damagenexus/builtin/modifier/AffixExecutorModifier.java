package io.github.naimjeg.damagenexus.builtin.modifier;

import io.github.naimjeg.damagenexus.api.IDamageModifier;
import io.github.naimjeg.damagenexus.api.affix.AffixEntry;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.rule.RuleExecutionContext;
import io.github.naimjeg.damagenexus.api.rule.RuntimeDamageRule;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.core.rule.DamageRuleStackingResolver;
import io.github.naimjeg.damagenexus.core.rule.DamageRuleStackingResult;
import io.github.naimjeg.damagenexus.core.rule.StackingTrace;
import io.github.naimjeg.damagenexus.registry.ModDataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AffixExecutorModifier implements IDamageModifier {

    private final DamagePhase phase;

    public AffixExecutorModifier(DamagePhase phase) {
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

        DamageRuleStackingResult result =
                DamageRuleStackingResolver.resolve(rules);

        rules = result.rules();

        if (ctx.debugger.enabled()) {
            for (StackingTrace trace : result.traces()) {
                ctx.debugger.logOperation(
                        trace.dropped().toString(),
                        phase,
                        "STACKING_DROP:" + trace.reason() + ":kept=" + trace.kept(),
                        trace.droppedValue()
                );
            }
        }

        rules.sort(
                Comparator.comparingInt(
                        (RuntimeDamageRule rule) -> rule.entry().priority()
                ).reversed()
        );

        if (ctx.debugger.enabled()) {
            ctx.debugger.logOperation(
                    "rule:executor",
                    phase,
                    "RULE_COUNT",
                    rules.size()
            );
        }

        for (RuntimeDamageRule rule : rules) {
            rule.entry().tryExecute(
                    ctx,
                    phase,
                    rule.executionContext()
            );
        }
    }

    private void collectWeaponAffixes(
            DamageNexusContext ctx,
            List<RuntimeDamageRule> out
    ) {
        if (ctx.attacker == null) {
            return;
        }

        ItemStack mainHand = ctx.attacker.getMainHandItem();

        collectStackAffixes(
                out,
                mainHand,
                RuleExecutionContext.weaponAffix(
                        ctx.attacker,
                        mainHand,
                        EquipmentSlot.MAINHAND
                )
        );

        ItemStack offHand = ctx.attacker.getOffhandItem();

        collectStackAffixes(
                out,
                offHand,
                RuleExecutionContext.weaponAffix(
                        ctx.attacker,
                        offHand,
                        EquipmentSlot.OFFHAND
                )
        );
    }

    private void collectArmorAffixes(
            DamageNexusContext ctx,
            List<RuntimeDamageRule> out
    ) {
        if (ctx.victim == null) {
            return;
        }

        collectArmorSlot(ctx, out, EquipmentSlot.HEAD);
        collectArmorSlot(ctx, out, EquipmentSlot.CHEST);
        collectArmorSlot(ctx, out, EquipmentSlot.LEGS);
        collectArmorSlot(ctx, out, EquipmentSlot.FEET);
    }

    private void collectArmorSlot(
            DamageNexusContext ctx,
            List<RuntimeDamageRule> out,
            EquipmentSlot slot
    ) {
        ItemStack stack = ctx.victim.getItemBySlot(slot);

        collectStackAffixes(
                out,
                stack,
                RuleExecutionContext.armorAffix(
                        ctx.victim,
                        stack,
                        slot
                )
        );
    }

    private void collectStackAffixes(
            List<RuntimeDamageRule> out,
            ItemStack stack,
            RuleExecutionContext exec
    ) {
        if (stack.isEmpty()) {
            return;
        }

        List<AffixEntry> affixes =
                stack.getOrDefault(
                        ModDataComponents.ITEM_AFFIXES.get(),
                        List.of()
                );

        if (affixes.isEmpty()) {
            return;
        }

        for (AffixEntry affix : affixes) {
            if (affix.phase() != phase) {
                continue;
            }

            if (!affix.role().canRunAs(exec.role())) {
                continue;
            }

            out.add(new RuntimeDamageRule(affix, exec));
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