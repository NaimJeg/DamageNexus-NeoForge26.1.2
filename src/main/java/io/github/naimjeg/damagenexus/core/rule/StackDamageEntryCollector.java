package io.github.naimjeg.damagenexus.core.rule;

import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleDefinition;
import io.github.naimjeg.damagenexus.api.rule.RuleExecutionContext;
import io.github.naimjeg.damagenexus.api.rule.RuntimeDamageRule;
import io.github.naimjeg.damagenexus.api.rule.affix.DamageAffixDefinition;
import io.github.naimjeg.damagenexus.api.rule.affix.DamageAffixValidator;
import io.github.naimjeg.damagenexus.api.rule.affix.RuntimeDamageAffix;
import io.github.naimjeg.damagenexus.api.rule.entry.DamageEntryDefinition;
import io.github.naimjeg.damagenexus.api.rule.entry.DamageEntryValidator;
import io.github.naimjeg.damagenexus.api.rule.entry.RuntimeDamageEntry;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.ModDataComponents;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class StackDamageEntryCollector {

    private StackDamageEntryCollector() {
    }

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

        collectEntryRules(
                ctx,
                phase,
                out,
                stack,
                exec,
                source
        );

        collectAffixRules(
                ctx,
                phase,
                out,
                stack,
                exec,
                source
        );
    }

    private static void collectEntryRules(
            DamageNexusContext ctx,
            DamagePhase phase,
            List<RuntimeDamageRule> out,
            ItemStack stack,
            RuleExecutionContext exec,
            String source
    ) {
        List<DamageEntryDefinition> entries =
                stack.getOrDefault(
                        ModDataComponents.DAMAGE_ENTRIES.get(),
                        List.of()
                );

        if (entries.isEmpty()) {
            return;
        }

        List<DamageEntryDefinition> validEntries =
                DamageEntryValidator.filterValid(
                        entries,
                        source + "/entries"
                );

        for (DamageEntryDefinition entry : validEntries) {
            RuntimeDamageEntry runtimeEntry =
                    new RuntimeDamageEntry(entry, exec);

            for (RuntimeDamageRule runtimeRule : runtimeEntry.expandRules()) {
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
}
