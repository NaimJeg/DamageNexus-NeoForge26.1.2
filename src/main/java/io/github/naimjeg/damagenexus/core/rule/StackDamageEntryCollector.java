package io.github.naimjeg.damagenexus.core.rule;

import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleDefinition;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleValidator;
import io.github.naimjeg.damagenexus.api.rule.RuleExecutionContext;
import io.github.naimjeg.damagenexus.api.rule.RuntimeDamageRule;
import io.github.naimjeg.damagenexus.api.rule.affix.DamageAffixDefinition;
import io.github.naimjeg.damagenexus.api.rule.affix.DamageAffixValidator;
import io.github.naimjeg.damagenexus.api.rule.affix.RuntimeDamageAffix;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.ModDataComponents;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class StackDamageEntryCollector {

    private StackDamageEntryCollector() {}

    public static void collectStackEntries(
            DamageNexusContext ctx,
            DamagePhase phase,
            List<RuntimeDamageRule> out,
            ItemStack stack,
            RuleExecutionContext exec,
            String source
    ) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        collectAffixRules(
                ctx,
                phase,
                out,
                stack,
                exec,
                source
        );

        collectStandaloneRules(
                ctx,
                phase,
                out,
                stack,
                exec,
                source
        );
    }

    private static void collectAffixRules(
            DamageNexusContext ctx,
            DamagePhase phase,
            List<RuntimeDamageRule> out,
            ItemStack stack,
            RuleExecutionContext exec,
            String source
    ) {
        List<DamageAffixDefinition> affixes =
                stack.getOrDefault(
                        ModDataComponents.DAMAGE_AFFIXES.get(),
                        List.of()
                );

        if (affixes.isEmpty()) {
            return;
        }

        List<DamageAffixDefinition> validAffixes =
                DamageAffixValidator.filterValid(
                        affixes,
                        source + "/affixes"
                );

        for (DamageAffixDefinition affix : validAffixes) {
            RuntimeDamageAffix runtimeAffix =
                    new RuntimeDamageAffix(affix, exec);

            for (RuntimeDamageRule runtimeRule : runtimeAffix.expandRules()) {
                DamageRuleDefinition rule = runtimeRule.definition();

                if (rule.phase() != phase) {
                    continue;
                }

                if (!rule.role().canRunAs(exec.role())) {
                    continue;
                }

                ctx.trace().rules().collected(
                        phase,
                        rule,
                        exec
                );

                out.add(runtimeRule);
            }
        }
    }

    private static void collectStandaloneRules(
            DamageNexusContext ctx,
            DamagePhase phase,
            List<RuntimeDamageRule> out,
            ItemStack stack,
            RuleExecutionContext exec,
            String source
    ) {
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
                        source + "/rules"
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

            ctx.trace().rules().collected(
                    phase,
                    rule,
                    exec
            );

            out.add(runtimeRule);
        }
    }
}